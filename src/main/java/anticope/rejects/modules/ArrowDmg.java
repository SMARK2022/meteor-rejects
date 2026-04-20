package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.StopUsingItemEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.Vec3d;

public class ArrowDmg extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> strength = sgGeneral.add(new DoubleSetting.Builder()
            .name("strength")
            .description("More strength = higher damage.")
            .defaultValue(5)
            .min(0.1)
            .sliderMax(25)
            .build()
    );

    public final Setting<Boolean> tridents = sgGeneral.add(new BoolSetting.Builder()
            .name("tridents")
            .description("When enabled, tridents fly much further. Doesn't seem to affect damage or Riptide. WARNING: You can easily lose your trident by enabling this option!")
            .defaultValue(false)
            .build()
    );


    public ArrowDmg() {
        super(MeteorRejectsAddon.CATEGORY, "arrow-damage", "Massively increases arrow damage, but reduces accuracy and consume more hunger. Does not work with crossbows and is patched on Paper servers.");
    }

    @EventHandler
    private void onStopUsingItem(StopUsingItemEvent event) {
        if (!isValidItem(event.itemStack.getItem()))
            return;

        ClientPlayerEntity p = mc.player;

        p.networkHandler.sendPacket(
                new ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.START_SPRINTING));

        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();

        double adjustedStrength = strength.get() / 10.0 * Math.sqrt(500);
        Vec3d lookVec = p.getRotationVec(1).multiply(adjustedStrength);

        for (int i = 0; i < 4; i++) {
            sendPos(x, y, z, true);
        }
        sendPos(x - lookVec.x, y, z - lookVec.z, true);
        sendPos(x, y, z, false);
    }

    private void sendPos(double x, double y, double z, boolean onGround) {
        ClientPlayNetworkHandler clientPacketListener = mc.player.networkHandler;
        clientPacketListener.sendPacket(new PositionAndOnGround(x, y, z, onGround, mc.player.horizontalCollision));
    }

    private boolean isValidItem(Item item) {
        return tridents.get() && item == Items.TRIDENT || item == Items.BOW;
    }
}
