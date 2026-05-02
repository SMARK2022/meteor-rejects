package anticope.rejects.mixin.meteor.modules;

import anticope.rejects.mixininterface.IInventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryTweaks.class, remap = false)
public abstract class InventoryTweaksMixin implements IInventoryTweaks {
    private Runnable callback;

    // Use stable methods instead of compiler-generated lambda names; lambda ordinals drift between Meteor updates.
    @Inject(method = "steal", at = @At("RETURN"))
    private void afterSteal(ScreenHandler handler, CallbackInfo info) {
        if (callback != null) {
            callback.run();
            callback = null;
        }
    }

    @Override
    public void stealCallback(Runnable callback) {
        this.callback = callback;
    }

    @Inject(method = "checkAutoStealSettings", at = @At("HEAD"))
    private void onStealChanged(CallbackInfo info) {
        callback = null;
    }
}
