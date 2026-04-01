package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.shiewk.widgets.client.screen.WidgetSettingsScreen;
import de.shiewk.widgets.client.screen.gradienteditor.GradientEditorScreen;
import de.shiewk.widgets.color.GradientMode;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import static de.shiewk.widgets.client.WidgetManager.gson;
import static de.shiewk.widgets.utils.WidgetUtils.colorARGBToHexRGBA;

public class GradientWidgetSetting extends WidgetSettingOption<GradientOptions> {

    public GradientWidgetSetting(String id, Component name, GradientMode defaultMode, int defaultGradientSize, int defaultGradientSpeed, int defaultColor) {
        super(id, name);
        this.value = new GradientOptions(
                defaultMode,
                defaultGradientSize,
                defaultGradientSpeed,
                new int[]{defaultColor}
        );
    }

    public GradientWidgetSetting(String id, Component name, int defaultColor) {
        this(id, name, GradientMode.SWEEP, 40, 10, defaultColor);
    }

    private GradientOptions value;

    @Override
    public JsonElement saveState() {
        return gson.toJsonTree(getValue());
    }

    @Override
    public void loadState(JsonElement state) {
        if (state.isJsonPrimitive() && state.getAsJsonPrimitive().isNumber()){
            this.value = new GradientOptions(
                    this.value.mode(),
                    this.value.gradientSize(),
                    this.value.gradientSpeed(),
                    new int[]{ state.getAsInt() }
            );
        } else {
            this.value = gson.fromJson(state, GradientOptions.class);
        }
    }

    @Override
    public GradientOptions getValue() {
        return this.value;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        final Font textRenderer = Minecraft.getInstance().font;
        final long n = Util.getNanos();
        GradientOptions gradient = this.getValue();
        gradient.fillHorizontal(
                context,
                n,
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight()
        );
        int outlineColor;
        String displayText;
        int[] colors = gradient.colors();
        if (colors.length == 1){
            outlineColor = colors[0] | 0xff_00_00_00;
            displayText = "#" + colorARGBToHexRGBA(colors[0]);
        } else {
            outlineColor = 0xff_ff_ff_ff;
            displayText = Component.translatable("widgets.ui.widgetSettings.colors", colors.length).getString();
        }
        context.horizontalLine(getX(), getX()+getWidth(), getY(), outlineColor);
        context.horizontalLine(getX(), getX()+getWidth(), getY()+getHeight(), outlineColor);
        context.verticalLine(getX(), getY(), getY() + getHeight(), outlineColor);
        context.verticalLine(getX() + getWidth(), getY(), getY() + getHeight(), outlineColor);

        int width = textRenderer.width(displayText);
        context.text(
                textRenderer,
                displayText,
                getX() + (getWidth() / 2 - (width / 2)),
                getY() + (getHeight() / 2 - 4),
                0xff_ff_ff_ff,
                true
        );

        if (this.isHovered(mouseX, mouseY)){
            context.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public int getWidth() {
        return 72;
    }

    @Override
    public int getHeight() {
        return 24;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof WidgetSettingsScreen screen) {
            WidgetUtils.playSound(SoundEvents.COPPER_BULB_TURN_ON);
            client.setScreen(new GradientEditorScreen(client.screen, screen.getWidget(), this, screen.getOnChange()));
        }
        return true;
    }

    public void setValue(GradientOptions value) {
        this.value = value;
    }
}
