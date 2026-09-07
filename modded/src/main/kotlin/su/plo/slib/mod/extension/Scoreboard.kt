package su.plo.slib.mod.extension

import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Scoreboard

fun Scoreboard.getObjectiveBelowName() =
    getDisplayObjective(DisplaySlot.BELOW_NAME)
