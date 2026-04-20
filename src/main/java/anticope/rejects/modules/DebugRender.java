package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.debug.BeeDebugRenderer;
import net.minecraft.client.render.debug.BlockOutlineDebugRenderer;
import net.minecraft.client.render.debug.BrainDebugRenderer;
import net.minecraft.client.render.debug.BreezeDebugRenderer;
import net.minecraft.client.render.debug.ChunkDebugRenderer;
import net.minecraft.client.render.debug.ChunkLoadingDebugRenderer;
import net.minecraft.client.render.debug.CollisionDebugRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.debug.EntityBlockIntersectionsDebugRenderer;
import net.minecraft.client.render.debug.GameEventDebugRenderer;
import net.minecraft.client.render.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.render.debug.HeightmapDebugRenderer;
import net.minecraft.client.render.debug.LightDebugRenderer;
import net.minecraft.client.render.debug.NeighborUpdateDebugRenderer;
import net.minecraft.client.render.debug.OctreeDebugRenderer;
import net.minecraft.client.render.debug.PathfindingDebugRenderer;
import net.minecraft.client.render.debug.PoiDebugRenderer;
import net.minecraft.client.render.debug.RaidCenterDebugRenderer;
import net.minecraft.client.render.debug.RedstoneUpdateOrderDebugRenderer;
import net.minecraft.client.render.debug.SkyLightDebugRenderer;
import net.minecraft.client.render.debug.StructureDebugRenderer;
import net.minecraft.client.render.debug.SupportingBlockDebugRenderer;
import net.minecraft.client.render.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.render.debug.WaterDebugRenderer;
import net.minecraft.world.LightType;
import net.minecraft.world.debug.DebugDataStore;
import java.util.HashMap;
import java.util.Map;

public class DebugRender extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Map<Setting<Boolean>, DebugRenderer.Renderer> renderers = new HashMap<>();

    public DebugRender() {
        super(MeteorRejectsAddon.CATEGORY, "debug-renders", "Render useful debug information.");

        BrainDebugRenderer brainRenderer = new BrainDebugRenderer(mc);

        addRenderer("bee-brain-&-hive", new BeeDebugRenderer(mc));
        addRenderer("breeze-brain", new BreezeDebugRenderer(mc));
        addRenderer("chunk-culling", new ChunkDebugRenderer(mc));
        addRenderer("chunk-debug", new ChunkLoadingDebugRenderer(mc));
        addRenderer("collision", new CollisionDebugRenderer(mc));
        addRenderer("entity-block-intersection", new EntityBlockIntersectionsDebugRenderer());
        addRenderer("game-event", new GameEventDebugRenderer());
        addRenderer("mob-goals", new GoalSelectorDebugRenderer(mc));
        addRenderer("heightmap", new HeightmapDebugRenderer(mc));
        addRenderer("block-light", new SkyLightDebugRenderer(mc, true, false));
        addRenderer("sky-light", new SkyLightDebugRenderer(mc, false, true));
        addRenderer("light-sections-sky", new LightDebugRenderer(mc, LightType.SKY));
        addRenderer("light-sections-block", new LightDebugRenderer(mc, LightType.BLOCK));
        addRenderer("neighbor-updates", new NeighborUpdateDebugRenderer());
        addRenderer("octree", new OctreeDebugRenderer(mc));
        addRenderer("pathfinding-debug", new PathfindingDebugRenderer());
        addRenderer("raid-center", new RaidCenterDebugRenderer(mc));
        addRenderer("redstone-wire-orientations", new RedstoneUpdateOrderDebugRenderer());
        addRenderer("solid-faces", new BlockOutlineDebugRenderer(mc));
        addRenderer("structure-outlines", new StructureDebugRenderer());
        addRenderer("support-blocks", new SupportingBlockDebugRenderer(mc));
        addRenderer("brain-debug", brainRenderer);
        addRenderer("poi-debug", new PoiDebugRenderer(brainRenderer));
        addRenderer("village-sections", new VillageSectionsDebugRenderer());
        addRenderer("water", new WaterDebugRenderer(mc));
    }

    // TODO add descriptions to each
    private void addRenderer(String name, DebugRenderer.Renderer renderer) {
        Setting<Boolean> setting = sgGeneral.add(new BoolSetting.Builder()
                .name(name)
                .defaultValue(false)
                .build()
        );
        renderers.put(setting, renderer);
    }

    public void render(Frustum frustum, double x, double y, double z, float partialTick) {
        if (mc.getNetworkHandler() == null) return;
        DebugDataStore debugValueAccess = mc.getNetworkHandler().getDebugDataStore();
        for (Map.Entry<Setting<Boolean>, DebugRenderer.Renderer> entry : renderers.entrySet()) {
            if (entry.getKey().get()) {
                entry.getValue().render(x, y, z, debugValueAccess, frustum, partialTick);
            }
        }
    }
}
