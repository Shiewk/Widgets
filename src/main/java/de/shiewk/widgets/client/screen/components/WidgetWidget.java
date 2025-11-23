package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.screen.WidgetSettingsScreen;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.Iterator;
import java.util.function.Consumer;

public class WidgetWidget extends ClickableWidget {



    protected static final int COLOR_BG = new Color(0, 0, 0, 80).getRGB(),
            COLOR_BG_HOVER = new Color(40, 40, 40, 80).getRGB(),
            COLOR_FG = Color.WHITE.getRGB(),
            COLOR_DISABLED = new Color(200, 0, 0, 200).getRGB(),
            COLOR_DISABLED_HOVER = new Color(255, 0, 0, 200).getRGB(),
            COLOR_ENABLED = new Color(0, 200, 0, 200).getRGB(),
            COLOR_ENABLED_HOVER = new Color(0, 255, 0, 200).getRGB(),
            COLOR_BORDER = 0x80_ff_ff_ff;

    private final MinecraftClient client;
    private final ModWidget widget;
    private final TextRenderer textRenderer;
    private final Consumer<ModWidget> onEdit;

    private long toggleTime = 0;

    public WidgetWidget(int x, int y, int width, int height, MinecraftClient client, ModWidget widget, TextRenderer textRenderer, Consumer<ModWidget> onEdit) {
        super(x, y, width, height, widget.getName());
        this.client = client;
        this.widget = widget;
        this.textRenderer = textRenderer;
        this.onEdit = onEdit;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height - 24);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hover = this.isMouseOver(mouseX, mouseY);
        boolean widgetEnabled = widget.getSettings().isEnabled();
        context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), hover ? COLOR_BG_HOVER : COLOR_BG);
        Matrix3x2fStack stack = context.getMatrices().pushMatrix();
        stack.scale(2, 2, stack);
        int titleSize = textRenderer.getWidth(widget.getName());
        context.drawText(textRenderer, widget.getName(), getX() / 2 + getWidth() / 4 - titleSize / 2, getY() / 2 + 4, COLOR_FG, false);
        stack.popMatrix();
        int y = this.getY() + 12 + textRenderer.fontHeight * 2;
        for (Iterator<OrderedText> it = textRenderer.wrapLines(widget.getDescription(), this.getWidth() - 10).iterator(); it.hasNext(); y += 9) {
            OrderedText t = it.next();
            context.drawText(textRenderer, t, getX() + 5 + ((getWidth() - 5) / 2) - (textRenderer.getWidth(t) / 2), y, COLOR_FG, false);
        }
        this.renderToggleButton(context, mouseX, mouseY, widgetEnabled);

        if (hover || isMouseOverToggle(mouseX, mouseY)){
            context.setCursor(StandardCursors.POINTING_HAND);
        }

        context.drawHorizontalLine(getX(), getX() + getWidth() - 1, getY(), COLOR_BORDER);
        context.drawHorizontalLine(getX() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, COLOR_BORDER);
        context.drawVerticalLine(getX(), getY(), getY() + getHeight(), COLOR_BORDER);
        context.drawVerticalLine(getX() + getWidth() - 1, getY(), getY() + getHeight() - 1, COLOR_BORDER);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (isMouseOver(click.x(), click.y())){
            client.setScreen(new WidgetSettingsScreen(client.currentScreen, widget));
            return true;
        } else if (isMouseOverToggle(click.x(), click.y())){
            this.toggleWidget();
            return true;
        }
        return false;
    }

    private void toggleWidget() {
        widget.getSettings().toggleEnabled(widget);
        toggleTime = Util.getMeasuringTimeNano();
        onEdit.accept(widget);
    }

    private void renderToggleButton(DrawContext context, int mouseX, int mouseY, boolean widgetEnabled){
        boolean hoverToggle = this.isMouseOverToggle(mouseX, mouseY);
        final int toggleColor;
        final int toggleColorInvert;
        if (hoverToggle){
            if (widgetEnabled){
                toggleColor = COLOR_ENABLED_HOVER;
                toggleColorInvert = COLOR_DISABLED_HOVER;
            } else {
                toggleColor = COLOR_DISABLED_HOVER;
                toggleColorInvert = COLOR_ENABLED_HOVER;
            }
        } else {
            if (widgetEnabled){
                toggleColor = COLOR_ENABLED;
                toggleColorInvert = COLOR_DISABLED;
            } else {
                toggleColor = COLOR_DISABLED;
                toggleColorInvert = COLOR_ENABLED;
            }
        }
        if (toggleTime > Util.getMeasuringTimeNano() - 250000000){
            context.fill(this.getX(), this.getY() + this.getHeight() - 24, this.getX() + this.getWidth(), this.getY() + this.getHeight(), toggleColorInvert);
            context.fill(this.getX(), this.getY() + this.getHeight() - 24, (int) (WidgetUtils.computeEasing((Util.getMeasuringTimeNano() - toggleTime) / 250000000d) * this.getWidth() + this.getX()), this.getY() + this.getHeight(), toggleColor);
        } else {
            context.fill(this.getX(), this.getY() + this.getHeight() - 24, this.getX() + this.getWidth(), this.getY() + this.getHeight(), toggleColor);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable(widgetEnabled ? "widgets.ui.enabled" : "widgets.ui.disabled"), this.getX() + (this.getWidth() / 2), this.getY() + this.getHeight() - 16, COLOR_FG);
    }

    private boolean isMouseOverToggle(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= this.getX() && mouseY >= (this.getY() + this.height - 24) && mouseX < (this.getX() + this.width) && mouseY < (this.getY() + this.height);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public ModWidget getWidget() {
        return widget;
    }
}
