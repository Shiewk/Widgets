package de.shiewk.widgets;

import com.google.gson.JsonElement;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class WidgetSettingOption implements Drawable, Widget {
    private final String id;
    private final Text name;
    private int x = 0;
    private int y = 0;
    private int maxRenderWidth = 200; // this will always be changed before rendering
    private boolean focused = false;
    private BooleanSupplier shouldShow = WidgetUtils.TRUE_SUPPLIER;

    protected WidgetSettingOption(String id, Text name) {
        this.id = id;
        this.name = name;
    }

    public void setMaxRenderWidth(int maxRenderWidth) {
        this.maxRenderWidth = maxRenderWidth;
    }

    public int getMaxRenderWidth() {
        return maxRenderWidth;
    }

    public final String getId() {
        return id;
    }

    public final Text getName() {
        return name;
    }

    public abstract JsonElement saveState();
    public abstract void loadState(JsonElement state);

    public boolean mouseClicked(Click click, boolean doubled) {
        return false;
    }

    public boolean mouseReleased(Click click){
        return false;
    }

    public boolean charTyped(CharInput input) {
        return false;
    }

    public boolean keyPressed(KeyInput input) {
        return false;
    }

    public boolean keyReleased(KeyInput input) {
        return false;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public final void forEachChild(Consumer<ClickableWidget> consumer) {
        throw new UnsupportedOperationException();
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean shouldShow(){
        return shouldShow.getAsBoolean();
    }

    public WidgetSettingOption setShowCondition(BooleanSupplier shouldShow){
        this.shouldShow = shouldShow;
        return this;
    }
}
