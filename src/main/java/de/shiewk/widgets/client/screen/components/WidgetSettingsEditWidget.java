package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetSettingOption;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.awt.*;

public class WidgetSettingsEditWidget extends ScrollableWidget {
    private static final int COLOR_FG = Color.WHITE.getRGB(), COLOR_BG = new Color(0, 0, 0, 60).getRGB();
    private final TextRenderer textRenderer;
    private final ModWidget widget;
    private final Runnable onChange;
    private WidgetSettingOption focus = null;
    private int contentsHeight = 10;
    public WidgetSettingsEditWidget(int x, int y, int width, int height, TextRenderer textRenderer, ModWidget widget, Runnable onChange) {
        super(x, y, width, height, Text.empty());
        this.widget = widget;
        this.textRenderer = textRenderer;
        this.onChange = onChange;
        for (WidgetSettingOption customSetting : widget.getSettings().getCustomSettings()) {
            customSetting.setFocused(false);
        }
        setWidth(width);
    }

    @Override
    public void setWidth(int width) {
        for (WidgetSettingOption setting : widget.getSettings().getCustomSettings()) {
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
        Matrix3x2fStack matrices = context.getMatrices().pushMatrix();
        matrices.translate(0, (float) -getScrollY(), matrices);
        matrices.pushMatrix();
        matrices.scale(2, 2, matrices);
        matrices.translate(0, (float) -getScrollY(), matrices);
        context.drawText(textRenderer, widget.getName(), this.width / 4 - textRenderer.getWidth(widget.getName()) / 2, this.height / 100, COLOR_FG, true);
        matrices.popMatrix();
        int y = textRenderer.fontHeight * 2 + this.height / 50 + 5;
        for (WidgetSettingOption setting : widget.getSettings().getCustomSettings()) {
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
        matrices.popMatrix();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseY = click.y();
        double mouseX = click.x();
        mouseY += getScrollY();
        for (WidgetSettingOption customSetting : widget.getSettings().getCustomSettings()) {
            if (!customSetting.shouldShow()) continue;
            if (mouseX >= customSetting.getX() && mouseX <= customSetting.getX() + customSetting.getWidth()
                    && mouseY >= customSetting.getY() && mouseY <= customSetting.getY() + customSetting.getHeight()){
                focus = customSetting;
                customSetting.setFocused(true);
                if (customSetting.mouseClicked(new Click(mouseX, mouseY + getScrollY(), click.buttonInfo()), doubled)){
                    onChange.run();
                    return true;
                }
            } else {
                customSetting.setFocused(false);
            }
        }
        if (checkScrollbarDragged(click)) return true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (WidgetSettingOption customSetting : widget.getSettings().getCustomSettings()) {
            if (!customSetting.shouldShow()) continue;
            if (customSetting.mouseReleased(new Click(click.x(), click.y() + getScrollY(), click.buttonInfo()))){
                onChange.run();
                return true;
            }
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (this.focus != null){
            if (this.focus.charTyped(input)){
                onChange.run();
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.focus != null){
            if (this.focus.keyPressed(input)){
                onChange.run();
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        if (this.focus != null){
            if (this.focus.keyReleased(input)){
                onChange.run();
                return true;
            }
        }
        return super.keyReleased(input);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
