package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import java.util.List;

public class AutoRename extends Module {
    public enum ContainerType {
        BOTH, BUNDLES, SHULKERS, NONE;

        @Override
        public String toString() {
            return switch (this) {
                case BOTH -> "Both";
                case BUNDLES -> "Bundles";
                case SHULKERS  -> "Shulkers";
                case NONE     -> "None";
            };
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRename = settings.createGroup("Rename");
    private final SettingGroup sgLabelContainers = settings.createGroup("Label Containers");

    // General
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("How many ticks to wait between actions.")
            .defaultValue(2)
            .min(0)
            .sliderMax(40)
            .build()
    );

    // Rename group
    private final Setting<List<Item>> items = sgRename.add(new ItemListSetting.Builder()
            .name("items")
            .description("Items to rename.")
            .defaultValue(List.of())
            .build()
    );

    private final Setting<String> name = sgRename.add(new StringSetting.Builder()
            .name("name")
            .description("Name to apply to items. Leave blank to keep the default name.")
            .defaultValue("")
            .build()
    );

    // Label Containers group
    private final Setting<ContainerType> containerType = sgLabelContainers.add(new EnumSetting.Builder<ContainerType>()
            .name("container-type")
            .description("Which container types to rename based on the first item inside them.")
            .defaultValue(ContainerType.NONE)
            .build()
    );

    public AutoRename() {
        super(MeteorRejectsAddon.CATEGORY, "auto-rename", "Automatically renames items at an anvil. Can also label shulkers and bundles based on their contents.");
    }

    private int delayLeft = 0;

    @EventHandler
    private void onTick(TickEvent.Post ignoredEvent) {
        if (mc.interactionManager == null) return;
        if (items.get().isEmpty() && containerType.get() == ContainerType.NONE) return;
        if (!(mc.player.currentScreenHandler instanceof AnvilScreenHandler)) return;

        if (delayLeft > 0) {
            delayLeft--;
            return;
        } else {
            delayLeft = delay.get();
        }

        var slot0 = mc.player.currentScreenHandler.getSlot(0);
        var slot1 = mc.player.currentScreenHandler.getSlot(1);
        var slot2 = mc.player.currentScreenHandler.getSlot(2);
        if (slot1.hasStack()) {
            return; // second anvil slot occupied
        }
        if (slot2.hasStack()) {
            if (mc.player.experienceLevel >= 1) {
                extractNamed();
            }
        } else {
            if (slot0.hasStack()) {
                renameItem(slot0.getStack());
            } else {
                populateAnvil();
            }
        }
    }

    private boolean isContainerTarget(ItemStack st) {
        return switch (containerType.get()) {
            case SHULKERS -> st.contains(DataComponentTypes.CONTAINER);
            case BUNDLES  -> st.contains(DataComponentTypes.BUNDLE_CONTENTS);
            case BOTH     -> st.contains(DataComponentTypes.CONTAINER) || st.contains(DataComponentTypes.BUNDLE_CONTENTS);
            case NONE -> false;
        };
    }

    private void renameItem(ItemStack s) {
        String setname = isContainerTarget(s) ? getFirstItemName(s) : name.get();
        if (!(mc.currentScreen instanceof AnvilScreen)) {
            error("Not anvil screen");
            toggle();
            return;
        }
        var input = (TextFieldWidget) mc.currentScreen.children().get(0);
        input.setText(setname);
    }

    private String getFirstItemName(ItemStack stack) {
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            for (ItemStack item : container.iterateNonEmpty()) {
                return item.getName().getString();
            }
        }

        BundleContentsComponent bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (ItemStack item : bundle.iterate()) {
                return item.getName().getString();
            }
        }
        return "";
    }

    private void extractNamed() {
        var inv = mc.player.currentScreenHandler;
        for (int i = 3; i < 38; i++) {
            if (inv.getSlot(i).hasStack()) {
                InvUtils.shiftClick().fromId(2).toId(i);
                return;
            }
        }
    }

    private void populateAnvil() {
        var inv = mc.player.currentScreenHandler;
        for (int i = 3; i < 38; i++) {
            var sl = inv.getSlot(i);
            if (!sl.hasStack()) continue;
            var st = sl.getStack();
            boolean hasCustomName = st.getComponents().contains(DataComponentTypes.CUSTOM_NAME);
            boolean isRenameItem = items.get().contains(st.getItem()) &&
                    (name.get().isEmpty() ? hasCustomName : !hasCustomName);
            boolean isContainerItem = isContainerTarget(st) && !getFirstItemName(st).isEmpty();

            if (isRenameItem || (isContainerItem && !hasCustomName)) {
                InvUtils.shiftClick().fromId(i).toId(0);
                return;
            }
        }
    }
}
