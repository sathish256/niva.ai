package ai.niva.domain.network

enum class NetworkAccess {
    ALLOW,
    DENY,
}

data class NetworkPolicy(
    val aiRuntime: NetworkAccess = NetworkAccess.DENY,
    val rag: NetworkAccess = NetworkAccess.DENY,
    val memory: NetworkAccess = NetworkAccess.DENY,
    val voice: NetworkAccess = NetworkAccess.DENY,
    val vision: NetworkAccess = NetworkAccess.DENY,
    val documentProcessing: NetworkAccess = NetworkAccess.DENY,
) {
    fun assertNoNetwork(subsystem: String, access: NetworkAccess) {
        if (access == NetworkAccess.ALLOW) {
            throw IllegalStateException("Network access not permitted for $subsystem in offline-first build")
        }
    }
}
