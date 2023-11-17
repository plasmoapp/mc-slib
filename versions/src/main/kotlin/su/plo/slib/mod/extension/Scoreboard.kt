package su.plo.slib.mod.extension

import net.minecraft.world.scores.Scoreboard

//#if MC>=12002
//$$ import net.minecraft.world.scores.DisplaySlot
//#endif

fun Scoreboard.getObjectiveBelowName() =
    //#if MC>=12002
    //$$ getDisplayObjective(DisplaySlot.BELOW_NAME)
    //#else
    getDisplayObjective(2)
    //#endif
