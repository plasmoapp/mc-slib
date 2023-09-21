package su.plo.slib.mod.util

import com.google.common.collect.Maps
import net.minecraft.resources.ResourceLocation

object ResourceLocationCache {

    private val locationByString: MutableMap<String, ResourceLocation> = Maps.newConcurrentMap()

    fun getLocation(resourceLocation: String): ResourceLocation {
        return locationByString.computeIfAbsent(resourceLocation) {
            ResourceLocation(resourceLocation)
        }
    }
}
