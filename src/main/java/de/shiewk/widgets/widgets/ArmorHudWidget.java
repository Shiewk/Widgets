package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.RGBAColorWidgetSetting;
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

import static net.minecraft.text.Text.translatable;

public class ArmorHudWidget extends ResizableWidget {

    public ArmorHudWidget(Identifier id) {
        super(id, List.of(
                new IntSliderWidgetSetting("padding", translatable("widgets.widgets.armorHud.padding"), 0, 2, 5),
                new ToggleWidgetSetting("show_durability", translatable("widgets.widgets.armorHud.showDurability"), true),
                new RGBAColorWidgetSetting("backgroundcolor", translatable("widgets.widgets.basictext.background"), 0, 0, 0, 80),
                new EnumWidgetSetting<>("durability_style", translatable("widgets.widgets.armorHud.durabilityStyle"), DurabilityStyle.class, DurabilityStyle.NUMBER, DurabilityStyle::getDisplayName),
                new ToggleWidgetSetting("rainbow", translatable("widgets.widgets.common.rainbow"), false),
                new IntSliderWidgetSetting("rainbow_speed", translatable("widgets.widgets.common.rainbow.speed"), 1, 3, 10),
                new RGBAColorWidgetSetting("textcolor", translatable("widgets.widgets.basictext.textcolor"), 255, 255, 255, 255)
        ));
        getSettings().optionById("durability_style").setShowCondition(() -> this.showDurability);
        getSettings().optionById("rainbow_speed").setShowCondition(() -> this.rainbow);
        getSettings().optionById("textcolor").setShowCondition(() -> !this.rainbow);
    }

    public enum DurabilityStyle {
        NUMBER,
        PERCENT;

        public Text getDisplayName() {
            return translatable("widgets.widgets.armorHud.durabilityStyle." + name().toLowerCase());
        }
    }

    private int padding = 1;
    private boolean showDurability = true;
    private DurabilityStyle durabilityStyle;
    protected ItemStack helmet;
    protected ItemStack chestplate;
    protected ItemStack leggings;
    protected ItemStack boots;
    protected boolean rainbow = false;
    protected int rainbowSpeed = 3;
    protected int backgroundColor = 0x50_00_00_00, textColor = 0xff_ff_ff_ff;

    @Override
    public void renderScaled(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY) {
        context.fill(posX, posY, posX+width(), posY+height(), backgroundColor);
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
        context.drawItemInSlot(textRenderer, stack, posX, posY);
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
            context.drawText(textRenderer, text, posX + 16 + padding, posY + 5, rainbow ? BasicTextWidget.rainbowColor(mt, rainbowSpeed) : textColor, true);
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
        this.rainbow = ((ToggleWidgetSetting) settings.optionById("rainbow")).getValue();
        this.rainbowSpeed = ((IntSliderWidgetSetting) settings.optionById("rainbow_speed")).getValue();
        this.textColor = ((RGBAColorWidgetSetting) settings.optionById("textcolor")).getColor();
        this.backgroundColor = ((RGBAColorWidgetSetting) settings.optionById("backgroundcolor")).getColor();
    }
}
