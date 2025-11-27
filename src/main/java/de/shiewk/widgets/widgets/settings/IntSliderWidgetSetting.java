package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.shiewk.widgets.WidgetSettingOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class IntSliderWidgetSetting extends WidgetSettingOption {

    private int value;
    private int minValue;
    private int maxValue;
    private boolean changed = false;
    private boolean clicked = false;

    public IntSliderWidgetSetting(String id, Text name, int minValue, int defaultValue, int maxValue) {
        super(id, name);
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (maxValue > value && keyCode == GLFW.GLFW_KEY_RIGHT) {
            value++;
            return true;
        } else if (minValue < value && keyCode == GLFW.GLFW_KEY_LEFT){
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
        return MathHelper.lerp((float) (value - minValue) / (maxValue - minValue), getX() + 5, getX() + 155);
    }

    private int xPosToValue(int xpos){
        return MathHelper.lerp((xpos - getX() - 5) / 150f, minValue, maxValue);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int xp = valueToXPos(getValue());
        context.drawHorizontalLine(getX() + 5, getX() + 155, getY() + 6, 0xffffffff);
        context.drawVerticalLine(getX() + 4, getY() + 3, getY() + 10, 0xffffffff);
        context.drawVerticalLine(getX() + 155, getY() + 3, getY() + 10, 0xffffffff);
        context.fill(xp-2, getY() + 3, xp+2, getY() + 10, 0xffffffff);
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, String.valueOf(getValue()), getX() + 160, getY() + 3, 0xffffffff, true);
        if (clicked){
            this.changed = true;
            this.value = MathHelper.clamp(xPosToValue(mouseX), minValue, maxValue);
        }
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX()
                && mouseY >= this.getY()
                && getY() + getHeight() > mouseY
                && getX() + getWidth() > mouseX;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.clicked = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY) {
        this.clicked = false;
        boolean t = this.changed;
        this.changed = false;
        return t;
    }

    public int getValue() {
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
