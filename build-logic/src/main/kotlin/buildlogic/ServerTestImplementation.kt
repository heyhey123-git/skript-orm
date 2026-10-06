package buildlogic

data class ServerTestImplementation(
    val keys: String,
    val module: String?,
    val username: String,
    val transactions: Boolean = true
)
