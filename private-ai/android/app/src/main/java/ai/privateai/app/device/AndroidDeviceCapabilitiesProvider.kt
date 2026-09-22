package ai.privateai.app.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import ai.privateai.domain.device.DeviceCapabilities
import ai.privateai.domain.device.GpuBackend
import ai.privateai.domain.device.ThermalState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDeviceCapabilitiesProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun current(): DeviceCapabilities {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val statFs = context.filesDir?.let { java.io.File(it.parent ?: it.absolutePath) }
        val storage = statFs?.usableSpace ?: 0L
        return DeviceCapabilities(
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
            architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            gpuBackend = GpuBackend.VULKAN,
            availableStorageBytes = storage,
            supportsVulkan = true,
            supportsMetal = false,
            supportsNpu = false,
            osVersion = Build.VERSION.RELEASE ?: "unknown",
            thermalState = ThermalState.UNKNOWN,
            batteryPercent = null,
        )
    }
}
