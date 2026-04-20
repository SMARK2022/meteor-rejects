package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.NonNull;

public class KnockbackPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ka = sgGeneral.add(new BoolSetting.Builder()
            .name("only-killaura")
            .description("Only perform more KB when using killaura.")
            .defaultValue(false)
            .build()
    );

    public KnockbackPlus() {
        super(MeteorRejectsAddon.CATEGORY, "knockback-plus", "Performs more KB when you hit your target.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerInteractEntityC2SPacket packet) {
            packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
                @Override
                public void interact(@NonNull Hand interactionHand) {
                }

                @Override
                public void interactAt(@NonNull Hand interactionHand, @NonNull Vec3d vec3) {
                }

                @Override
                public void attack() {
                    Entity entity = ((IPlayerInteractEntityC2SPacket) packet).meteor$getEntity();
                    if (!(entity instanceof LivingEntity) || (entity != Modules.get().get(KillAura.class).getTarget() && ka.get()))
                        return;

                    assert mc.player != null;
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                }
            });
        }
    }
}
