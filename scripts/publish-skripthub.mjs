#!/usr/bin/env node
// Publishes the generated SkriptHub documentation to skripthub.net.
//
//   ./gradlew gendocs
//   SKRIPTHUB_TOKEN=<token> node scripts/publish-skripthub.mjs [--dry-run]
//
// The API publishes syntax one element at a time. This script compares the generated JSON with
// SkriptHub, updates changed entries, creates new ones, and reports entries it leaves alone.
// Examples still need the dashboard's JSON import.
//
// This script does not:
//
//   - Delete entries missing from the generated file. They may have been renamed or added by hand;
//     remove them in SkriptHub after review.
//   - Write examples. The API handles them separately, so differences are reported for dashboard import.
//   - Set supporting plugins. The generated file does not include that information.
//
// Read the token from SKRIPTHUB_TOKEN without printing it. SKRIPTHUB_ADDON overrides the addon
// selected by repository URL, so a dashboard rename does not break publication.

import { appendFileSync, readFileSync } from 'node:fs'

const API = 'https://skripthub.net/api/v1'
const DOCUMENT = process.env.SKRIPTHUB_DOCS ?? 'build/skripthub/skript-orm.json'
const REPOSITORY = process.env.GITHUB_REPOSITORY ?? 'heyhey123-git/skript-orm'
const TOKEN = process.env.SKRIPTHUB_TOKEN
const dryRun = process.argv.includes('--dry-run')

/** Plural categories in the generated file; the API uses their singular forms. */
const KINDS = ['events', 'conditions', 'effects', 'expressions', 'types', 'functions', 'sections', 'structures']

const failures = []
const lines = []

const say = (line = '') => {
  console.log(line)
  lines.push(line)
}

const headers = () => ({
  Authorization: 'Token ' + TOKEN,
  Accept: '*/*',
  'Content-Type': 'application/json'
})

/**
 * Reads a response as JSON.
 *
 * Strips an HTML comment if SkriptHub appends one. Returns a non-JSON response as text so the
 * caller can include it in an error.
 */
const readJson = async (response) => {
  const text = await response.text()
  for (const candidate of [text, text.replace(/<!--[\s\S]*?-->/g, '')]) {
    try {
      return JSON.parse(candidate)
    } catch {
      // Try the next form; return the original text if neither parses.
    }
  }
  return text
}

const request = async (method, path, body) => {
  const response = await fetch(API + path, {
    method,
    headers: headers(),
    body: body === undefined ? undefined : JSON.stringify(body)
  })
  const payload = await readJson(response)
  if (!response.ok) {
    const detail = typeof payload === 'object' ? JSON.stringify(payload) : String(payload)
    throw new Error(method + ' ' + path + ' answered ' + response.status + ': ' + detail.slice(0, 400))
  }
  return payload
}

const list = (payload) => (Array.isArray(payload) ? payload : (payload.results ?? []))

/** Generated syntax entries keyed by their SkriptHub titles. */
const readDocument = () => {
  const document = JSON.parse(readFileSync(DOCUMENT, 'utf8'))
  const entries = new Map()
  for (const kind of KINDS) {
    for (const entry of document[kind] ?? []) {
      entries.set(entry.name, {
        title: entry.name,
        syntaxType: kind.replace(/s$/, ''),
        description: (entry.description ?? []).join('\n'),
        pattern: (entry.patterns ?? []).join('\n'),
        since: (entry.since ?? [])[0],
        examples: (entry.examples ?? []).map(withoutBlankEdges)
      })
    }
  }
  return { version: document.metadata?.version ?? 'unknown', entries }
}

/** Removes blank lines around an example. */
function withoutBlankEdges(example) {
  const kept = String(example).replace(/\r\n/g, '\n').split('\n')
  while (kept.length > 1 && kept[0].trim() === '') kept.shift()
  while (kept.length > 1 && kept[kept.length - 1].trim() === '') kept.pop()
  return kept.join('\n')
}

/**
 * Finds the SkriptHub addon by repository URL, which remains stable if its dashboard name changes.
 */
const resolveAddon = async () => {
  if (process.env.SKRIPTHUB_ADDON) return process.env.SKRIPTHUB_ADDON

  const addons = await request('GET', '/addon/')
  const slug = REPOSITORY.split('/').pop().toLowerCase()
  const match = addons.find((addon) => (addon.url ?? '').toLowerCase().includes(slug))
  if (!match) {
    throw new Error(
      'no SkriptHub addon links to ' + REPOSITORY + '. Register the addon in the dashboard, or set ' +
      'SKRIPTHUB_ADDON to the name SkriptHub knows it by.'
    )
  }
  return match.name
}

/** Combines generated fields with metadata preserved from an existing SkriptHub entry. */
const bodyFor = (entry, row, addon) => {
  const body = {
    title: entry.title,
    description: entry.description,
    syntax_pattern: entry.pattern,
    syntax_type: entry.syntaxType,
    // Preserve supporting plugins on updates; new entries start without them.
    required_plugins: (row?.required_plugins ?? []).map((plugin) => (typeof plugin === 'string' ? plugin : plugin.name)),
    // A new entry needs the addon resolved for this run; updates keep their existing association.
    addon: row?.addon ?? addon
  }
  if (entry.since) body.compatible_addon_version = entry.since
  if (row?.compatible_minecraft_version != null) body.compatible_minecraft_version = row.compatible_minecraft_version
  if (row?.type_usage != null) body.type_usage = row.type_usage
  if (row?.return_type != null) body.return_type = row.return_type
  if (row?.event_values != null) body.event_values = row.event_values
  if (row?.event_cancellable != null) body.event_cancellable = row.event_cancellable
  if (row?.keywords != null) body.keywords = row.keywords
  return body
}

const same = (left, right) => (left ?? '').trim() === (right ?? '').trim()

/** Reads and normalizes the examples SkriptHub stores for one element. */
const examplesOf = async (id) => {
  const payload = await request('GET', '/syntaxexample/?syntax=' + id)
  return list(payload).map((example) => withoutBlankEdges(example.example_code ?? ''))
}

const compare = (document, rows) => {
  const updates = []
  const creates = []
  for (const entry of document.entries.values()) {
    const row = rows.find((candidate) => candidate.title === entry.title)
    if (!row) {
      creates.push(entry)
      continue
    }
    const changed = !same(row.syntax_pattern, entry.pattern) ||
      !same(row.description, entry.description) ||
      (entry.since !== undefined && !same(row.compatible_addon_version, entry.since))
    if (changed) updates.push({ entry, row })
  }
  const removed = rows.filter((row) => !document.entries.has(row.title))
  return { updates, creates, removed }
}

const differencesIn = (before, after) => {
  const changed = []
  for (const [field, left, right] of [
    ['pattern', before.syntax_pattern, after.syntax_pattern],
    ['description', before.description, after.description],
    ['since', before.compatible_addon_version, after.compatible_addon_version]
  ]) {
    if (!same(left, right)) changed.push(field)
  }
  return changed
}

if (!TOKEN) {
  console.error('SKRIPTHUB_TOKEN is not set. The token is on the SkriptHub API documentation page.')
  process.exit(1)
}

// Collect failures so the summary still reports successful and unsuccessful updates.
try {
  const document = readDocument()
  const addon = await resolveAddon()
  const rows = list(await request('GET', '/syntax/?addon=' + encodeURIComponent(addon)))
  const plan = compare(document, rows)

  say('### SkriptHub documentation ' + document.version)
  say()
  say('| | |')
  say('| --- | --- |')
  say('| Addon | `' + addon + '` |')
  say('| Elements in the generated file | ' + document.entries.size + ' |')
  say('| To update | ' + plan.updates.length + ' |')
  say('| To create | ' + plan.creates.length + ' |')
  say('| Unchanged | ' + (document.entries.size - plan.updates.length - plan.creates.length) + ' |')
  say('| On SkriptHub only, left alone | ' + plan.removed.length + ' |')
  say('| Mode | ' + (dryRun ? 'dry run, nothing was written' : '**published**') + ' |')
  say()

  for (const { entry, row } of plan.updates) {
    say('- `' + entry.title + '` (id ' + row.id + '): ' + differencesIn(row, {
      syntax_pattern: entry.pattern,
      description: entry.description,
      compatible_addon_version: entry.since ?? row.compatible_addon_version
    }).join(', '))
  }
  for (const entry of plan.creates) say('- `' + entry.title + '`: new, will be created')
  for (const row of plan.removed) say('- `' + row.title + '` (id ' + row.id + '): not in the generated file, left as it is')

  if (!dryRun) {
    for (const { entry, row } of plan.updates) {
      try {
        await request('PUT', '/syntax/' + row.id + '/', bodyFor(entry, row, addon))
      } catch (error) {
        failures.push(entry.title + ': ' + error.message)
      }
    }

    if (plan.creates.length) {
      // Create all new entries in one API request.
      const body = plan.creates.map((entry) => bodyFor(entry, null, addon))
      try {
        await request('POST', '/syntax/', body)
      } catch (error) {
        failures.push('creating ' + plan.creates.length + ' element(s): ' + error.message)
      }
    }

    // Read back entries to catch fields the API accepted but did not save.
    const after = list(await request('GET', '/syntax/?addon=' + encodeURIComponent(addon)))
    const remaining = compare(document, after)
    for (const { entry, row } of remaining.updates) {
      failures.push(entry.title + ' still differs after the write: ' + differencesIn(row, {
        syntax_pattern: entry.pattern,
        description: entry.description,
        compatible_addon_version: entry.since ?? row.compatible_addon_version
      }).join(', '))
    }
  }

  // Ignore blank-line differences introduced when SkriptHub joins multiple examples.
  const join = (examples) => examples.map((example) => example.trim()).join('\n').replace(/\n\s*\n/g, '\n')
  for (const entry of document.entries.values()) {
    const row = rows.find((candidate) => candidate.title === entry.title)
    if (!row) continue
    try {
      if (join(await examplesOf(row.id)) !== join(entry.examples)) {
        say('- `' + entry.title + '` (id ' + row.id + '): its examples differ, which this script does not write — use the dashboard JSON import')
      }
    } catch (error) {
      failures.push('reading the examples of ' + entry.title + ': ' + error.message)
    }
  }
} catch (error) {
  failures.push(error instanceof Error ? error.message : String(error))
}

if (failures.length) {
  say()
  say('**' + failures.length + ' failure(s)**')
  for (const failure of failures) say('- ' + failure)
  // Write failures to stderr as well, so CI annotations show the cause.
  console.error(failures.length + ' SkriptHub failure(s):')
  for (const failure of failures) console.error('- ' + failure)
}

// Publish a self-contained summary for the workflow run.
if (process.env.GITHUB_STEP_SUMMARY) {
  appendFileSync(process.env.GITHUB_STEP_SUMMARY, lines.join('\n') + '\n')
}

// Let pending connections close naturally; process.exit() can interrupt them on Windows.
process.exitCode = failures.length ? 1 : 0
