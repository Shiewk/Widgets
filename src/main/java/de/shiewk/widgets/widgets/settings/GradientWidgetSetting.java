package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import de.shiewk.widgets.client.screen.WidgetSettingsScreen;
import de.shiewk.widgets.client.screen.gradienteditor.GradientEditorScreen;
import de.shiewk.widgets.color.GradientMode;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import static de.shiewk.widgets.client.WidgetManager.gson;
import static de.shiewk.widgets.utils.WidgetUtils.colorARGBToHexRGBA;

public class GradientWidgetSetting extends WidgetSettingOption<GradientOptions> {

    public GradientWidgetSetting(String id, Text name, GradientMode defaultMode, int defaultGradientSize, int defaultGradientSpeed, int defaultColor) {
        super(id, name);
        this.value = new GradientOptions(
                defaultMode,
                defaultGradientSize,
                defaultGradientSpeed,
                new int[]{defaultColor}
        );
    }

    public GradientWidgetSetting(String id, Text name, int defaultColor) {
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
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        final long n = Util.getMeasuringTimeNano();
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
            displayText = Text.translatable("widgets.ui.widgetSettings.colors", colors.length).getString();
        }
        context.drawHorizontalLine(getX(), getX()+getWidth(), getY(), outlineColor);
        context.drawHorizontalLine(getX(), getX()+getWidth(), getY()+getHeight(), outlineColor);
        context.drawVerticalLine(getX(), getY(), getY() + getHeight(), outlineColor);
        context.drawVerticalLine(getX() + getWidth(), getY(), getY() + getHeight(), outlineColor);

        int width = textRenderer.getWidth(displayText);
        context.drawText(
                textRenderer,
                displayText,
                getX() + (getWidth() / 2 - (width / 2)),
                getY() + (getHeight() / 2 - 4),
                0xff_ff_ff_ff,
                true
        );

        if (this.isHovered(mouseX, mouseY)){
            context.setCursor(StandardCursors.POINTING_HAND);
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
    public boolean mouseClicked(Click click, boolean doubled) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof WidgetSettingsScreen screen) {
            WidgetUtils.playSound(SoundEvents.BLOCK_COPPER_BULB_TURN_ON);
            client.setScreen(new GradientEditorScreen(client.currentScreen, screen.getWidget(), this, screen.getOnChange()));
        }
        return true;
    }

    public void setValue(GradientOptions value) {
        this.value = value;
    }
}
