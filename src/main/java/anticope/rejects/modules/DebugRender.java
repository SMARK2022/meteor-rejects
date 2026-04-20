package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DebugRender extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Map<Setting<Boolean>, Supplier<DebugRenderer.Renderer>> renderers = new HashMap<>();

    public DebugRender() {
        super(MeteorRejectsAddon.CATEGORY, "debug-renders", "Render useful debug information.");

        addRenderer("bee-brain-&-hive", () -> mc.debugRenderer.beeDebugRenderer);
        addRenderer("chunk-culling", () -> mc.debugRenderer.chunkBorderDebugRenderer);
        addRenderer("chunk-debug", () -> mc.debugRenderer.chunkLoadingDebugRenderer);
        addRenderer("collision", () -> mc.debugRenderer.collisionDebugRenderer);
        addRenderer("game-event", () -> mc.debugRenderer.gameEventDebugRenderer);
        addRenderer("mob-goals", () -> mc.debugRenderer.goalSelectorDebugRenderer);
        addRenderer("heightmap", () -> mc.debugRenderer.heightmapDebugRenderer);
        addRenderer("sky-light", () -> mc.debugRenderer.skyLightDebugRenderer);
        addRenderer("light-sections", () -> mc.debugRenderer.lightDebugRenderer);
        addRenderer("neighbor-updates", () -> mc.debugRenderer.neighborUpdateDebugRenderer);
        addRenderer("pathfinding-debug", () -> mc.debugRenderer.pathfindingDebugRenderer);
        addRenderer("raid-center", () -> mc.debugRenderer.raidCenterDebugRenderer);
        addRenderer("redstone-wire-orientations", () -> mc.debugRenderer.redstoneUpdateOrderDebugRenderer);
        addRenderer("solid-faces", () -> mc.debugRenderer.blockOutlineDebugRenderer);
        addRenderer("structure-outlines", () -> mc.debugRenderer.structureDebugRenderer);
        addRenderer("support-blocks", () -> mc.debugRenderer.supportingBlockDebugRenderer);
        addRenderer("village-sections", () -> mc.debugRenderer.villageSectionsDebugRenderer);
        addRenderer("water", () -> mc.debugRenderer.waterDebugRenderer);
    }

    private void addRenderer(String name, Supplier<DebugRenderer.Renderer> supplier) {
        Setting<Boolean> setting = sgGeneral.add(new BoolSetting.Builder()
                .name(name)
                .defaultValue(false)
                .build()
        );
        renderers.put(setting, supplier);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double x, double y, double z) {
        if (mc.getNetworkHandler() == null) return;
        for (Map.Entry<Setting<Boolean>, Supplier<DebugRenderer.Renderer>> entry : renderers.entrySet()) {
            if (entry.getKey().get()) {
                entry.getValue().get().render(matrices, vertexConsumers, x, y, z);
            }
        }
    }
}

