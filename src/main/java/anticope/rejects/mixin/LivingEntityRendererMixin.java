package anticope.rejects.mixin;

import anticope.rejects.modules.Rendering;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Inject(method = "setupTransforms", at = @At("HEAD"))
    private void dinnerboneEntities(S state, MatrixStack matrices, float animationProgress, float bodyYaw, CallbackInfo ci) {
        if (state instanceof PlayerEntityRenderState) return;
        if (Modules.get() == null) return;
        Rendering renderingModule = Modules.get().get(Rendering.class);
        if (renderingModule != null && renderingModule.dinnerboneEnabled()) {
            state.flipUpsideDown = true;
        }
    }

}
