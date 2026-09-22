package ai.privateai.modelmanager

import ai.privateai.domain.device.DeviceCapabilities
import ai.privateai.domain.model.ModelDescriptor
import ai.privateai.domain.model.ModelInstallState
import ai.privateai.domain.model.ModelType
import ai.privateai.domain.selection.ModelSelectionResult
import ai.privateai.domain.selection.ModelSelector
import java.io.File
import java.security.MessageDigest

class ModelManager(
    private val modelsRoot: File,
    private val selector: ModelSelector = ModelSelector(),
) {
    private val registry = mutableMapOf<String, RegistryEntry>()

    init {
        registerKnown(ModelCatalog.defaultQwen3_4b_Q4())
        refreshInstalled()
    }

    fun registerKnown(descriptor: ModelDescriptor) {
        val state = if (descriptor.localPath != null && File(descriptor.localPath).exists()) {
            ModelInstallState.READY
        } else {
            ModelInstallState.NOT_INSTALLED
        }
        registry[descriptor.id] = RegistryEntry(descriptor, state)
    }

    fun refreshInstalled() {
        if (!modelsRoot.exists()) {
            modelsRoot.mkdirs()
        }
        registry.values.forEach { entry ->
            val path = entry.descriptor.localPath ?: findInstalledPath(entry.descriptor.id)
            if (path != null && File(path).exists()) {
                val updated = entry.descriptor.copy(localPath = path)
                registry[entry.descriptor.id] = RegistryEntry(updated, ModelInstallState.READY)
            }
        }
    }

    fun importModelFile(source: File, descriptor: ModelDescriptor): ModelDescriptor {
        require(source.exists()) { "Source model file does not exist" }
        val target = File(modelsRoot, "${descriptor.id}.gguf")
        source.copyTo(target, overwrite = true)
        val checksum = sha256(target)
        val installed = descriptor.copy(
            localPath = target.absolutePath,
            checksum = checksum,
            sizeBytes = target.length(),
        )
        registry[installed.id] = RegistryEntry(installed, ModelInstallState.READY)
        return installed
    }

    fun activateLlm(device: DeviceCapabilities): ModelSelectionResult {
        refreshInstalled()
        val installed = registry.values
            .filter { it.descriptor.type == ModelType.LLM && it.state == ModelInstallState.READY }
            .map { it.descriptor }
        return selector.selectLlm(device, installed)
    }

    fun getRegistry(): List<RegistryEntry> = registry.values.toList()

    fun getDescriptor(id: String): ModelDescriptor? = registry[id]?.descriptor

    fun markInUse(id: String) {
        registry[id]?.let { entry ->
            registry[id] = entry.copy(state = ModelInstallState.IN_USE)
        }
    }

    fun markReady(id: String) {
        registry[id]?.let { entry ->
            registry[id] = entry.copy(state = ModelInstallState.READY)
        }
    }

    private fun findInstalledPath(id: String): String? {
        val candidate = File(modelsRoot, "$id.gguf")
        return if (candidate.exists()) candidate.absolutePath else null
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read = input.read(buffer)
            while (read > 0) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
