package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.widgets.settings.WidgetSettingOption;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;

public class WidgetSettingsEditWidget extends ScrollableWidget {
    private static final int COLOR_FG = Color.WHITE.getRGB(), COLOR_BG = new Color(0, 0, 0, 60).getRGB();
    private final TextRenderer textRenderer;
    private final ModWidget widget;
    private final Runnable onChange;
    private WidgetSettingOption<?> focus = null;
    private int contentsHeight = 10;

    public WidgetSettingsEditWidget(int x, int y, int width, int height, TextRenderer textRenderer, ModWidget widget, Runnable onChange) {
        super(x, y, width, height, Text.empty());
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
    protected int getContentsHeightWithPadding() {
        return this.contentsHeight;
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 20;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX()+width, getY()+height, COLOR_BG);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0, (float) -getScrollY(), 0);
        matrices.push();
        matrices.scale(2, 2, 1);
        matrices.translate(0, (float) -getScrollY(), 0);
        context.drawText(textRenderer, widget.getName(), this.width / 4 - textRenderer.getWidth(widget.getName()) / 2, this.height / 100, COLOR_FG, true);
        matrices.pop();
        int y = textRenderer.fontHeight * 2 + this.height / 50 + 5;
        for (WidgetSettingOption<?> setting : widget.getSettings().getCustomSettings()) {
            if (!setting.shouldShow()) continue;
            if (this.width - setting.getWidth() > textRenderer.getWidth(setting.getName()) + 20){
                setting.setX(this.getX() + this.width - setting.getWidth() - 5);
                setting.setY(y);
                context.drawText(textRenderer, setting.getName(), getX() + 10, y + (setting.getHeight() / 2), COLOR_FG, true);
            } else {
                setting.setX(this.getX() + this.width / 2 - setting.getWidth() / 2);
                setting.setY(y + 9 + 5);
                context.drawText(textRenderer, setting.getName(), getX() + getWidth() / 2 - textRenderer.getWidth(setting.getName()) / 2, y, COLOR_FG, true);
                y += 9 + 5;
            }
            setting.render(context, mouseX, (int) (mouseY + getScrollY()), delta);
            y += setting.getHeight();
            y += 5;
        }
        this.contentsHeight = y;
        matrices.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseY += getScrollY();
        for (WidgetSettingOption<?> customSetting : widget.getSettings().getCustomSettings()) {
            if (!customSetting.shouldShow()) continue;
            if (customSetting.isHovered(mouseX, mouseY)){
                focus = customSetting;
                customSetting.setFocused(true);
                if (customSetting.mouseClicked(mouseX, mouseY + getScrollY(), button)){
                    onChange.run();
                    return true;
                }
            } else {
                customSetting.setFocused(false);
            }
        }
        return checkScrollbarDragged(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (WidgetSettingOption<?> customSetting : widget.getSettings().getCustomSettings()) {
            if (!customSetting.shouldShow()) continue;
            if (customSetting.mouseReleased(mouseX, mouseY + getScrollY())){
                onChange.run();
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.focus != null){
            if (this.focus.charTyped(chr, modifiers)){
                onChange.run();
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.focus != null){
            if (this.focus.keyPressed(keyCode, scanCode, modifiers)){
                onChange.run();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.focus != null){
            if (this.focus.keyReleased(keyCode, scanCode, modifiers)){
                onChange.run();
                return true;
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
