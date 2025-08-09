package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ArmorHudWidget extends ResizableWidget {

    public ArmorHudWidget(Identifier id) {
        super(id, List.of(
                new IntSliderWidgetSetting("padding", Text.translatable("widgets.widgets.armorHud.padding"), 0, 2, 5),
                new ToggleWidgetSetting("show_durability", Text.translatable("widgets.widgets.armorHud.showDurability"), true),
                new EnumWidgetSetting<>("durability_style", Text.translatable("widgets.widgets.armorHud.durabilityStyle"), DurabilityStyle.class, DurabilityStyle.NUMBER, DurabilityStyle::getDisplayName)
        ));
        getSettings().optionById("durability_style").setShowCondition(() -> this.showDurability);
    }

    public enum DurabilityStyle {
        NUMBER,
        PERCENT;

        public Text getDisplayName() {
            return Text.translatable("widgets.widgets.armorHud.durabilityStyle." + name().toLowerCase());
        }
    }

    private int padding = 1;
    private boolean showDurability = true;
    private DurabilityStyle durabilityStyle;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    @Override
    public void renderScaled(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY) {
        if (helmet != null){
            renderItem(context, textRenderer, helmet, posX + padding, posY + padding);
        }
        if (chestplate != null){
            renderItem(context, textRenderer, chestplate, posX + padding, posY + 16 + padding);
        }
        if (leggings != null){
            renderItem(context, textRenderer, leggings, posX + padding, posY + 32 + padding);
        }
        if (boots != null){
            renderItem(context, textRenderer, boots, posX + padding, posY + 48 + padding);
        }
    }

    private void renderItem(DrawContext context, TextRenderer textRenderer, ItemStack stack, int posX, int posY){
        context.drawItemWithoutEntity(stack, posX, posY);
        context.drawStackOverlay(textRenderer, stack, posX, posY);
        if (showDurability){
            renderDurability(context, textRenderer, stack, posX, posY);
        }
    }

    private void renderDurability(DrawContext context, TextRenderer textRenderer, ItemStack stack, int posX, int posY) {
        Integer maxDamage = stack.get(DataComponentTypes.MAX_DAMAGE);
        if (maxDamage != null) {
            int damage = stack.getOrDefault(DataComponentTypes.DAMAGE, 0);
            String text = switch (durabilityStyle){
                case NUMBER -> String.valueOf(maxDamage - damage);
                case PERCENT -> ((maxDamage - damage) * 100 / maxDamage) + "%";
            };
            context.drawText(textRenderer, text, posX + 16 + padding, posY + 5, 0xffffffff, true);
        }
    }

    @Override
    public void tick() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            PlayerInventory inventory = player.getInventory();
            helmet = inventory.getStack(39);
            chestplate = inventory.getStack(38);
            leggings = inventory.getStack(37);
            boots = inventory.getStack(36);
        } else {
            helmet = chestplate = leggings = boots = null;
        }
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.armorHud");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.armorHud.description");
    }

    @Override
    public int width() {
        if (showDurability){
            return 36 + padding * 3;
        } else {
            return 16 + padding * 2;
        }
    }

    @Override
    public int height() {
        return 64 + padding * 2;
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.padding = ((IntSliderWidgetSetting) settings.optionById("padding")).getValue();
        this.showDurability = ((ToggleWidgetSetting) settings.optionById("show_durability")).getValue();
        this.durabilityStyle = (DurabilityStyle) ((EnumWidgetSetting<?>) settings.optionById("durability_style")).getValue();
    }
}
