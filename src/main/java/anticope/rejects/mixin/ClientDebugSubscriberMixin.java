package anticope.rejects.mixin;

import anticope.rejects.modules.DebugRender;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientDebugSubscriptionManager;
import net.minecraft.world.debug.DebugSubscriptionType;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ClientDebugSubscriptionManager.class)
public class ClientDebugSubscriberMixin {

    @Inject(method = "getRequestedSubscriptions", at = @At("RETURN"))
    private void addModuleSubscriptions(CallbackInfoReturnable<Set<DebugSubscriptionType<?>>> cir) {
        DebugRender module = Modules.get().get(DebugRender.class);
        if (module == null || !module.isActive()) return;

        Set<DebugSubscriptionType<?>> set = cir.getReturnValue();
        set.add(DebugSubscriptionTypes.BEES);
        set.add(DebugSubscriptionTypes.BEE_HIVES);
        set.add(DebugSubscriptionTypes.BRAINS);
        set.add(DebugSubscriptionTypes.BREEZES);
        set.add(DebugSubscriptionTypes.GOAL_SELECTORS);
        set.add(DebugSubscriptionTypes.ENTITY_PATHS);
        set.add(DebugSubscriptionTypes.ENTITY_BLOCK_INTERSECTIONS);
        set.add(DebugSubscriptionTypes.POIS);
        set.add(DebugSubscriptionTypes.REDSTONE_WIRE_ORIENTATIONS);
        set.add(DebugSubscriptionTypes.VILLAGE_SECTIONS);
        set.add(DebugSubscriptionTypes.RAIDS);
        set.add(DebugSubscriptionTypes.STRUCTURES);
        set.add(DebugSubscriptionTypes.GAME_EVENT_LISTENERS);
        set.add(DebugSubscriptionTypes.NEIGHBOR_UPDATES);
        set.add(DebugSubscriptionTypes.GAME_EVENTS);
    }
}

