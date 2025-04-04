package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetListWidget extends ScrollableWidget {

    private final MinecraftClient client;
    private final List<ModWidget> widgets;
    private final List<WidgetWidget> elements = new ArrayList<>();
    private final TextRenderer textRenderer;
    private final Consumer<ModWidget> onEdit;


    public WidgetListWidget(int x, int y, int width, int height, Text message, MinecraftClient client, List<ModWidget> widgets, TextRenderer textRenderer, Consumer<ModWidget> onEdit) {
        super(x, y, width, height, message);
        this.client = client;
        this.widgets = widgets;
        this.textRenderer = textRenderer;
        this.onEdit = onEdit;
        init();
    }

    private void init(){
        GridWidget gw = new GridWidget();
        gw.getMainPositioner().margin(4, 4, 4, 4);
        final GridWidget.Adder adder = gw.createAdder(this.width / 208);
        for (ModWidget widget : widgets) {
            adder.add(new WidgetWidget(0, 0, 200, 100, client, widget, textRenderer, onEdit));
        }
        gw.refreshPositions();
        SimplePositioningWidget.setPos(gw, 0, 0, this.width, this.getContentsHeightWithPadding(), 0.5F, 0.5F);
        gw.forEachChild(w -> this.addWidget((WidgetWidget) w));
    }

    protected void addWidget(WidgetWidget drawableElement) {
        this.elements.add(drawableElement);
    }

    @Override
    protected int getContentsHeightWithPadding() {
        final int rowSize = this.width / 208;
        final int rows = widgets.size() % rowSize == 0 ? widgets.size() / rowSize : widgets.size() / rowSize + 1;
        return 10 + (rows * 108);
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 35;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0, -getScrollY(), 0);
        for (WidgetWidget element : elements) {
            element.render(context, mouseX, (int) (mouseY + getScrollY()), delta);
        }
        matrices.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseY += getScrollY();
        for (Element element : elements) {
            if (element.mouseClicked(mouseX, mouseY, 0)){
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        if (super.checkScrollbarDragged(mouseX, mouseY - getScrollY(), button)) return true;
        return super.mouseClicked(mouseX, mouseY - getScrollY(), button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        for (ModWidget widget : widgets) {
            builder.put(NarrationPart.HINT, widget.getName());
        }
    }
}
