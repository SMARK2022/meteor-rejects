package anticope.rejects.mixin;

import anticope.rejects.modules.DebugRender;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V", at = @At("HEAD"))
    public void render(MatrixStack matrices, Frustum frustum, VertexConsumerProvider.Immediate vertexConsumers, double d, double e, double f, CallbackInfo ci) {
        DebugRender debugRender = Modules.get().get(DebugRender.class);
        if (debugRender != null && debugRender.isActive()) {
            debugRender.render(matrices, vertexConsumers, d, e, f);
        }
    }

}
