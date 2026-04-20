package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SkeletonESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> skeletonColorSetting = sgGeneral.add(new ColorSetting.Builder()
            .name("players-color")
            .description("The other player's color.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    public final Setting<Boolean> distance = sgGeneral.add(new BoolSetting.Builder()
            .name("distance-colors")
            .description("Changes the color of skeletons depending on distance.")
            .defaultValue(false)
            .build()
    );

    private final Freecam freecam;
    private final Color color = new Color();

    public SkeletonESP() {
        super(MeteorRejectsAddon.CATEGORY, "skeleton-esp", "Looks cool as fuck");
        freecam = Modules.get().get(Freecam.class);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        float tickDelta = event.tickDelta;
        int rotationHoldTicks = Config.get().rotationHoldTicks.get();

        mc.world.getEntities().forEach(entity -> {
            if (!(entity instanceof PlayerEntity)) return;
            if (mc.options.getPerspective() == Perspective.FIRST_PERSON && !freecam.isActive() && mc.player == entity)
                return;

            Color skeletonColor = PlayerUtils.getPlayerColor((PlayerEntity) entity, skeletonColorSetting.get());
            if (distance.get()) skeletonColor = getColorFromDistance(entity);
            PlayerEntity player = (PlayerEntity) entity;

            Vec3d footPos = getEntityRenderPosition(player, tickDelta);
            PlayerEntityRenderer livingEntityRenderer = (PlayerEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
            PlayerEntityModel playerModel = (PlayerEntityModel) livingEntityRenderer.getModel();

            float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.lastBodyYaw, player.bodyYaw);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) bodyYaw = Rotations.serverYaw;
            float headYaw = MathHelper.lerpAngleDegrees(tickDelta, player.lastHeadYaw, player.headYaw);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) headYaw = Rotations.serverYaw;

            float ageInTicks = (float) player.age + tickDelta;
            float relativeHeadYaw = headYaw - bodyYaw;
            float pitch = player.getPitch(tickDelta);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) pitch = Rotations.serverPitch;

            boolean swimming = player.isInSwimmingPose();
            boolean sneaking = player.isInSneakingPose();
            boolean flying = player.isGliding();

            PlayerEntityRenderState renderState = new PlayerEntityRenderState();
            renderState.limbSwingAnimationProgress = player.limbAnimator.getAnimationProgress(tickDelta);
            renderState.limbSwingAmplitude = player.limbAnimator.getAmplitude(tickDelta);
            renderState.age = ageInTicks;
            renderState.relativeHeadYaw = relativeHeadYaw;
            renderState.pitch = pitch;
            playerModel.setAngles(renderState);

            ModelPart head = playerModel.head;
            ModelPart leftArm = playerModel.leftArm;
            ModelPart rightArm = playerModel.rightArm;
            ModelPart leftLeg = playerModel.leftLeg;
            ModelPart rightLeg = playerModel.rightLeg;

            Matrix4f outerMat = new Matrix4f();
            if (swimming) outerMat.translate(0, 0.35f, 0);
            outerMat.rotateY((float) Math.toRadians(-(bodyYaw + 180)));
            if (swimming || flying) outerMat.rotateX((float) Math.toRadians(-(90 + pitch)));
            if (swimming) outerMat.translate(0, -0.95f, 0);

            Matrix4f tiltedMat = new Matrix4f(outerMat).translate(0, 0.75f, 0);
            if (sneaking && !swimming && !flying) tiltedMat.rotate(0.5f, -1, 0, 0);
            tiltedMat.translate(0, -0.75f, 0);

            float hipY = sneaking ? 0.6f : 0.7f;
            float hipZ = sneaking ? 0.23f : 0f;
            float shoulderY = sneaking ? 1.05f : 1.35f;
            float spineTopY = sneaking ? 1.05f : 1.4f;

            // Spine
            drawBone(event, footPos, tiltedMat, 0, hipY, hipZ, 0, spineTopY, 0, skeletonColor);
            // Shoulders
            drawBone(event, footPos, tiltedMat, -0.37f, shoulderY, 0, 0.37f, shoulderY, 0, skeletonColor);
            // Pelvis
            drawBone(event, footPos, outerMat, -0.15f, hipY, hipZ, 0.15f, hipY, hipZ, skeletonColor);

            // Head
            Matrix4f headMat = new Matrix4f(tiltedMat).translate(0, spineTopY, 0);
            applyModelRot(headMat, head);
            drawBone(event, footPos, headMat, 0, 0, 0, 0, 0.15f, 0, skeletonColor);

            // Right Leg
            Matrix4f rightLegMat = new Matrix4f(outerMat).translate(0.15f, hipY, hipZ);
            applyModelRot(rightLegMat, rightLeg);
            drawBone(event, footPos, rightLegMat, 0, 0, 0, 0, -0.6f, 0, skeletonColor);

            // Left Leg
            Matrix4f leftLegMat = new Matrix4f(outerMat).translate(-0.15f, hipY, hipZ);
            applyModelRot(leftLegMat, leftLeg);
            drawBone(event, footPos, leftLegMat, 0, 0, 0, 0, -0.6f, 0, skeletonColor);

            // Right Arm
            Matrix4f rightArmMat = new Matrix4f(tiltedMat).translate(0.37f, shoulderY, 0);
            applyModelRot(rightArmMat, rightArm);
            drawBone(event, footPos, rightArmMat, 0, 0, 0, 0, -0.55f, 0, skeletonColor);

            // Left Arm
            Matrix4f leftArmMat = new Matrix4f(tiltedMat).translate(-0.37f, shoulderY, 0);
            applyModelRot(leftArmMat, leftArm);
            drawBone(event, footPos, leftArmMat, 0, 0, 0, 0, -0.55f, 0, skeletonColor);
        });
    }

    private void drawBone(Render3DEvent event, Vec3d origin, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, Color color) {
        Vector3f p1 = mat.transformPosition(new Vector3f(x1, y1, z1));
        Vector3f p2 = mat.transformPosition(new Vector3f(x2, y2, z2));
        event.renderer.line(
            origin.x + p1.x, origin.y + p1.y, origin.z + p1.z,
            origin.x + p2.x, origin.y + p2.y, origin.z + p2.z, color
        );
    }

    private void applyModelRot(Matrix4f mat, ModelPart part) {
        if (part.roll != 0) mat.rotate(part.roll, 0, 0, 1);
        if (part.yaw != 0) mat.rotate(part.yaw, 0, -1, 0);
        if (part.pitch != 0) mat.rotate(part.pitch, -1, 0, 0);
    }

    private Vec3d getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.lastX + (entity.getX() - entity.lastX) * partial;
        double y = entity.lastY + (entity.getY() - entity.lastY) * partial;
        double z = entity.lastZ + (entity.getZ() - entity.lastZ) * partial;

        return new Vec3d(x, y, z);
    }

    private Color getColorFromDistance(Entity entity) {
        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        double distance = mc.gameRenderer.getCamera().getCameraPos().distanceTo(entityPos);
        double percent = distance / 60;

        if (percent < 0 || percent > 1) {
            color.set(0, 255, 0, 255);
            return color;
        }

        int r, g;

        if (percent < 0.5) {
            r = 255;
            g = (int) (255 * percent / 0.5);
        } else {
            g = 255;
            r = 255 - (int) (255 * (percent - 0.5) / 0.5);
        }

        color.set(r, g, 0, 255);
        return color;
    }
}