package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class IntSliderWidgetSetting extends WidgetSettingOption<Integer> {

    private int value;
    private int minValue;
    private int maxValue;
    private boolean changed = false;
    private boolean clicked = false;

    public IntSliderWidgetSetting(String id, Component name, int minValue, int defaultValue, int maxValue) {
        super(id, name);
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (maxValue > value && input.isRight()) {
            value++;
            return true;
        } else if (minValue < value && input.isLeft()){
            value--;
            return true;
        }
        return false;
    }

    @Override
    public JsonElement saveState() {
        return new JsonPrimitive(value);
    }

    @Override
    public void loadState(JsonElement state) {
        if (state.isJsonPrimitive() && state.getAsJsonPrimitive().isNumber()){
            this.value = state.getAsInt();
        }
    }

    private int valueToXPos(int value){
        return Mth.lerpInt((float) (value - minValue) / (maxValue - minValue), getX() + 5, getX() + 155);
    }

    private int xPosToValue(int xpos){
        return Mth.lerpInt((xpos - getX() - 5) / 150f, minValue, maxValue);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int xp = valueToXPos(getValue());
        context.horizontalLine(getX() + 5, getX() + 155, getY() + 6, 0xffffffff);
        context.verticalLine(getX() + 4, getY() + 3, getY() + 10, 0xffffffff);
        context.verticalLine(getX() + 155, getY() + 3, getY() + 10, 0xffffffff);
        context.fill(xp-2, getY() + 3, xp+2, getY() + 10, 0xffffffff);
        final Font textRenderer = Minecraft.getInstance().font;
        context.text(textRenderer, String.valueOf(getValue()), getX() + 160, getY() + 3, 0xffffffff, true);
        if (clicked){
            this.changed = true;
            this.value = Mth.clamp(xPosToValue(mouseX), minValue, maxValue);
        }
        if (isHovering(mouseX, mouseY)){
            context.requestCursor(CursorTypes.RESIZE_EW);
        }
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX()
                && mouseY >= this.getY()
                && getY() + getHeight() > mouseY
                && getX() + getWidth() > mouseX;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        this.clicked = true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.clicked = false;
        boolean t = this.changed;
        this.changed = false;
        return t;
    }

    @Override
    public @NotNull Integer getValue() {
        return value;
    }

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public int getHeight() {
        return 15;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
