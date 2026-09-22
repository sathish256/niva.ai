package ai.privateai.domain.device

enum class GpuBackend {
    NONE,
    VULKAN,
    METAL,
    OPENCL,
    CPU,
}

enum class ThermalState {
    UNKNOWN,
    NOMINAL,
    FAIR,
    SERIOUS,
    CRITICAL,
}

data class DeviceCapabilities(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val architecture: String,
    val gpuBackend: GpuBackend,
    val availableStorageBytes: Long,
    val supportsVulkan: Boolean,
    val supportsMetal: Boolean,
    val supportsNpu: Boolean,
    val osVersion: String,
    val thermalState: ThermalState,
    val batteryPercent: Int?,
)
