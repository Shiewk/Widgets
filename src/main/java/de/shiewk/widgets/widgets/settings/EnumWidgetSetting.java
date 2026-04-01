package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.function.Function;

public class EnumWidgetSetting<T extends Enum<T>> extends WidgetSettingOption<T> {

    private final Class<T> enumClass;
    private T value;
    private final Function<T, Component> enumNameGetter;
    private int height = 0;
    private boolean mouseClick = false;
    private boolean changed = false;

    public EnumWidgetSetting(String id, Component name, Class<T> enumClass, T defaultValue, Function<T, Component> enumNameGetter) {
        super(id, name);
        this.enumClass = enumClass;
        this.value = defaultValue;
        this.enumNameGetter = enumNameGetter;
    }

    @Override
    public T getValue(){
        return value;
    }

    @Override
    public JsonElement saveState() {
        return new JsonPrimitive(value.name());
    }

    @Override
    public void loadState(JsonElement state) {
        if (state.isJsonPrimitive() && state.getAsJsonPrimitive().isString()){
            final String name = state.getAsString();
            for (T constant : enumClass.getEnumConstants()) {
                if (constant.name().equals(name)){
                    this.value = constant;
                }
            }
        }
    }

    private static final int COLOR_SELECTED = new Color(0, 0x5f, 0x68, 255).getRGB(),
            COLOR_UNSELECTED = new Color(0, 0, 0, 50).getRGB(),
            COLOR_UNSELECTED_HOVER = new Color(80, 80, 80, 50).getRGB(),
            COLOR_TEXT = new Color(255, 255, 255, 255).getRGB();

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        final Font textRenderer = Minecraft.getInstance().font;
        int y = 0;
        int nx = 5;
        final int bx = getX() + getWidth();
        for (T constant : enumClass.getEnumConstants()) {
            final Component name = enumNameGetter.apply(constant);
            final int textRendererWidth = textRenderer.width(name);
            if (nx != 5 && nx + textRendererWidth + 20 > this.getWidth()){
                y += 24;
                nx = 5;
            }
            final boolean hover = mouseX <= bx - nx && mouseX >= bx - nx - 10 - textRendererWidth && mouseY <= y + 19 + getY() && mouseY >= y + getY();
            context.fill(bx - 10 - textRendererWidth - nx, y + getY(), bx - nx, y + 19 + getY(), constant == value ? COLOR_SELECTED : hover ? COLOR_UNSELECTED_HOVER : COLOR_UNSELECTED);
            context.text(textRenderer, name, bx - nx - 5 - textRendererWidth, y + 5 + getY(), COLOR_TEXT, true);
            if (hover && mouseClick){
                this.value = constant;
                this.changed = true;
                WidgetUtils.playSound(SoundEvents.COPPER_BULB_TURN_ON);
            }
            if (hover){
                context.requestCursor(CursorTypes.POINTING_HAND);
            }
            nx += textRendererWidth + 20;
        }

        mouseClick = false;
        this.height = y + 34;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        mouseClick = true;
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        mouseClick = false;
        boolean changed = this.changed;
        this.changed = false;
        return changed;
    }

    @Override
    public int getWidth() {
        return 200;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
