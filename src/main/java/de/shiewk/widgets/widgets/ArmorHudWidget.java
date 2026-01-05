package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.*;
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
import java.util.Locale;

import static net.minecraft.text.Text.translatable;

public class ArmorHudWidget extends ResizableWidget {

    public ArmorHudWidget(Identifier id) {
        super(id, List.of(
                new IntSliderWidgetSetting("padding", translatable("widgets.widgets.armorHud.padding"), 0, 2, 5),
                new ToggleWidgetSetting("show_durability", translatable("widgets.widgets.armorHud.showDurability"), true),
                new IntSliderWidgetSetting("width", translatable("widgets.widgets.basictext.width"), 16, 42, 128),
                new EnumWidgetSetting<>("alignment", translatable("widgets.widgets.basictext.alignment"), BasicTextWidget.TextAlignment.class, BasicTextWidget.TextAlignment.CENTER, BasicTextWidget.TextAlignment::displayText),
                new GradientWidgetSetting("backgroundcolor", translatable("widgets.widgets.basictext.background"), 0x50_00_00_00),
                new EnumWidgetSetting<>("durability_style", translatable("widgets.widgets.armorHud.durabilityStyle"), DurabilityStyle.class, DurabilityStyle.NUMBER, DurabilityStyle::getDisplayName),
                new GradientWidgetSetting("textcolor", translatable("widgets.widgets.basictext.textcolor"), 0xff_ff_ff_ff)
        ));
        getSettings().optionById("width").setShowCondition(() -> this.showDurability);
        getSettings().optionById("alignment").setShowCondition(() -> this.showDurability);
        getSettings().optionById("durability_style").setShowCondition(() -> this.showDurability);
    }

    public enum DurabilityStyle {
        NUMBER,
        PERCENT;

        public Text getDisplayName() {
            return translatable("widgets.widgets.armorHud.durabilityStyle." + name().toLowerCase(Locale.ROOT));
        }
    }

    protected int padding = 1;
    protected boolean showDurability = true;
    protected DurabilityStyle durabilityStyle;
    protected ItemStack helmet;
    protected ItemStack chestplate;
    protected ItemStack leggings;
    protected ItemStack boots;
    protected int preferredWidth = 42;
    protected BasicTextWidget.TextAlignment textAlignment = BasicTextWidget.TextAlignment.CENTER;
    protected GradientOptions backgroundColor, textColor;

    @Override
    public void renderScaled(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY) {
        backgroundColor.fillHorizontal(
                context,
                measuringTimeNano,
                posX, posY,
                posX+width(), posY+height()
        );
        if (helmet != null){
            renderItem(context, measuringTimeNano, textRenderer, helmet, posX + padding, posY + padding);
        }
        if (chestplate != null){
            renderItem(context, measuringTimeNano, textRenderer, chestplate, posX + padding, posY + 16 + padding);
        }
        if (leggings != null){
            renderItem(context, measuringTimeNano, textRenderer, leggings, posX + padding, posY + 32 + padding);
        }
        if (boots != null){
            renderItem(context, measuringTimeNano, textRenderer, boots, posX + padding, posY + 48 + padding);
        }
    }

    private void renderItem(DrawContext context, long mt, TextRenderer textRenderer, ItemStack stack, int posX, int posY){
        context.drawItemWithoutEntity(stack, posX, posY);
        context.drawStackOverlay(textRenderer, stack, posX, posY);
        if (showDurability){
            renderDurability(context, mt, textRenderer, stack, posX, posY);
        }
    }

    private void renderDurability(DrawContext context, long mt, TextRenderer textRenderer, ItemStack stack, int posX, int posY) {
        Integer maxDamage = stack.get(DataComponentTypes.MAX_DAMAGE);
        if (maxDamage != null) {
            int damage = stack.getOrDefault(DataComponentTypes.DAMAGE, 0);
            String text = switch (durabilityStyle){
                case NUMBER -> String.valueOf(maxDamage - damage);
                case PERCENT -> ((maxDamage - damage) * 100 / maxDamage) + "%";
            };
            switch (textAlignment){
                case RIGHT -> {
                    int width = textRenderer.getWidth(text);
                    textColor.drawText(context, textRenderer, mt, text, posX + width() - width - padding * 2, posY + 5, true);
                }
                case CENTER -> {
                    int width = textRenderer.getWidth(text);
                    textColor.drawText(context, textRenderer, mt, text, posX + ((preferredWidth + padding*2) - width) / 2 + 8, posY + 5, true);
                }
                case LEFT -> {
                    textColor.drawText(context, textRenderer, mt, text, posX + 16 + padding, posY + 5, true);
                }
            }
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
        return translatable("widgets.widgets.armorHud");
    }

    @Override
    public Text getDescription() {
        return translatable("widgets.widgets.armorHud.description");
    }

    @Override
    public int width() {
        if (showDurability){
            return 3 * padding + this.preferredWidth;
        } else {
            return 2 * padding + 16;
        }
    }

    @Override
    public int height() {
        return 64 + padding * 2;
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.padding = (int) settings.optionById("padding").getValue();
        this.showDurability = (boolean) settings.optionById("show_durability").getValue();
        this.durabilityStyle = (DurabilityStyle) settings.optionById("durability_style").getValue();
        this.textColor = (GradientOptions) settings.optionById("textcolor").getValue();
        this.backgroundColor = (GradientOptions) settings.optionById("backgroundcolor").getValue();
        this.preferredWidth = (int) settings.optionById("width").getValue();
        this.textAlignment = (BasicTextWidget.TextAlignment) settings.optionById("alignment").getValue();
    }
}
