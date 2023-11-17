package su.plo.slib.mod.extension

import net.minecraft.world.entity.Entity

fun Entity.level() =
    //#if MC>=12001
    //$$ level()
    //#else
    level
    //#endif
