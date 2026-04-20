package anticope.rejects.mixin;

import net.minecraft.server.debug.SubscriberTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SubscriberTracker.class)
public class ServerDebugSubscribersMixin {

    @Inject(method = "canSubscribe", at = @At("HEAD"), cancellable = true)
    private void bypassPermissionCheck(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}

