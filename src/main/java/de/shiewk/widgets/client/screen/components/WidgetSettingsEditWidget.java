package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.widgets.settings.WidgetSettingOption;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class WidgetSettingsEditWidget extends AbstractScrollArea {
    private static final int COLOR_FG = Color.WHITE.getRGB(), COLOR_BG = new Color(0, 0, 0, 60).getRGB();
    private final Font textRenderer;
    private final ModWidget widget;
    private final Runnable onChange;
    private WidgetSettingOption<?> focus = null;
    private int contentsHeight = 10;

    public WidgetSettingsEditWidget(int x, int y, int width, int height, Font textRenderer, ModWidget widget, Runnable onChange) {
        super(x, y, width, height, Component.empty(), AbstractScrollArea.defaultSettings(20));
        this.widget = widget;
        this.textRenderer = textRenderer;
        this.onChange = onChange;
        for (WidgetSettingOption<?> customSetting : widget.getSettings().getCustomSettings()) {
            customSetting.setFocused(false);
        }
        setWidth(width);
        widget.onSettingsChanged();
    }

    @Override
    public void setWidth(int width) {
        for (WidgetSettingOption<?> setting : widget.getSettings().getCustomSettings()) {
            setting.setMaxRenderWidth(width - 10);
        }
        super.setWidth(width);
    }

    @Override
    protected int contentHeight() {
        return this.contentsHeight;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX()+width, getY()+height, COLOR_BG);
        Matrix3x2fStack matrices = context.pose().pushMatrix();
        matrices.translate(0, (float) -scrollAmount(), matrices);
        matrices.pushMatrix();
        matrices.scale(2, 2, matrices);
        matrices.translate(0, (float) -scrollAmount(), matrices);
        context.text(textRenderer, widget.getName(), this.width / 4 - textRenderer.width(widget.getName()) / 2, this.height / 100, COLOR_FG, true);
        matrices.popMatrix();
        int y = textRenderer.lineHeight * 2 + this.height / 50 + 5;
        for (WidgetSettingOption<?> setting : widget.getSettings().getCustomSettings()) {
            if (!setting.shouldShow()) continue;
            if (this.width - setting.getWidth() > textRenderer.width(setting.getName()) + 20){
                setting.setX(this.getX() + this.width - setting.getWidth() - 5);
                setting.setY(y);
                context.text(textRenderer, setting.getName(), getX() + 10, y + (setting.getHeight() / 2), COLOR_FG, true);
            } else {
                setting.setX(this.getX() + this.width / 2 - setting.getWidth() / 2);
                setting.setY(y + 9 + 5);
                context.text(textRenderer, setting.getName(), getX() + getWidth() / 2 - textRenderer.width(setting.getName()) / 2, y, COLOR_FG, true);
                y += 9 + 5;
            }
            setting.extractRenderState(context, mouseX, (int) (mouseY + scrollAmount()), delta);
            y += setting.getHeight();
            y += 5;
        }
        this.contentsHeight = y;
        matrices.popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseY = click.y();
        double mouseX = click.x();
        mouseY += scrollAmount();
        for (WidgetSettingOption<?> customSetting : widget.getSettings().getCustomSettings()) {
            if (!customSetting.shouldShow()) continue;
            if (customSetting.isHovered(mouseX, mouseY)){
                focus = customSetting;
                customSetting.setFocused(true);
                if (customSetting.mouseClicked(new MouseButtonEvent(mouseX, mouseY + scrollAmount(), click.buttonInfo()), doubled)){
                    onChange.run();
                    return true;
                }
            } else {
                customSetting.setFocused(false);
            }
        }
        return updateScrolling(click);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent click) {
        for (WidgetSettingOption<?> customSetting : widget.getSettings().getCustomSettings()) {
            if (!customSetting.shouldShow()) continue;
            if (customSetting.mouseReleased(new MouseButtonEvent(click.x(), click.y() + scrollAmount(), click.buttonInfo()))){
                onChange.run();
                return true;
            }
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent input) {
        if (this.focus != null){
            if (this.focus.charTyped(input)){
                onChange.run();
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        if (this.focus != null){
            if (this.focus.keyPressed(input)){
                onChange.run();
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(@NonNull KeyEvent input) {
        if (this.focus != null){
            if (this.focus.keyReleased(input)){
                onChange.run();
                return true;
            }
        }
        return super.keyReleased(input);
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput builder) {

    }
}
