package su.plo.slib.mod.mixin.accessor;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSourceStack.class)
public interface CommandSourceStackAccessor {
    @Accessor("source")
    @NotNull CommandSource slib_getSource();
}
