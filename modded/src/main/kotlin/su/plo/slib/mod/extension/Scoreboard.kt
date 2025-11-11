package su.plo.slib.mod.extension

import net.minecraft.world.scores.Scoreboard

//? if >=1.20.2 {
/*import net.minecraft.world.scores.DisplaySlot
*///?}

fun Scoreboard.getObjectiveBelowName() =
    //? if >=1.20.2 {
    /*getDisplayObjective(DisplaySlot.BELOW_NAME)
    *///?} else {
    getDisplayObjective(2)
    //?}
