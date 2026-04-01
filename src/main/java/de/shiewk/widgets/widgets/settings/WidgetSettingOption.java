package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import de.shiewk.widgets.utils.WidgetUtils;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public abstract class WidgetSettingOption<T> implements Renderable, LayoutElement {

    private final String id;
    private final Component name;
    private int x = 0;
    private int y = 0;
    private int maxRenderWidth = 200; // this will always be changed before rendering
    private boolean focused = false;
    private BooleanSupplier shouldShow = WidgetUtils.TRUE_SUPPLIER;

    protected WidgetSettingOption(String id, Component name) {
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

    public final Component getName() {
        return name;
    }

    public abstract JsonElement saveState();
    public abstract void loadState(JsonElement state);
    public abstract T getValue();

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent click){
        return false;
    }

    public boolean charTyped(CharacterEvent input) {
        return false;
    }

    public boolean keyPressed(KeyEvent input) {
        return false;
    }

    public boolean keyReleased(KeyEvent input) {
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

    public boolean isHovered(double mouseX, double mouseY){
        return mouseX >= getX() && mouseX <= getX() + getWidth()
                && mouseY >= getY() && mouseY <= getY() + getHeight();
    }

    @Override
    public final void visitWidgets(@NonNull Consumer<AbstractWidget> consumer) {
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

    public WidgetSettingOption<?> setShowCondition(BooleanSupplier shouldShow){
        this.shouldShow = shouldShow;
        return this;
    }
}
