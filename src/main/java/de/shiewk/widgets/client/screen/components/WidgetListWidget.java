package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.WidgetManager;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class WidgetListWidget extends ScrollableWidget {

    public static final int COLUMN_SIZE = 208;
    private final MinecraftClient client;
    private List<ModWidget> widgets;
    private final List<WidgetWidget> elements = new ArrayList<>();
    private final TextRenderer textRenderer;
    private final Consumer<ModWidget> onEdit;

    public static boolean searchQueryMatches(String search, ModWidget widget) {
        if (search == null) return true;
        return widget.getName().getString().contains(search) || widget.getDescription().getString().contains(search) || widget.getId().toString().contains(search);
    }

    public WidgetListWidget(int x, int y, int width, int height, MinecraftClient client, TextRenderer textRenderer, Consumer<ModWidget> onEdit) {
        super(x, y, width, height, Text.empty());
        this.client = client;
        this.widgets = loadWidgets(null);
        this.textRenderer = textRenderer;
        this.onEdit = onEdit;
        init();
    }

    private List<ModWidget> loadWidgets(String search) {
        return WidgetManager.getAllWidgets()
                .stream()
                .filter(w -> searchQueryMatches(search, w))
                .sorted(Comparator.comparing(w -> w.getName().getString().toLowerCase(Locale.ROOT)))
                .toList();
    }

    private void init(){
        GridWidget gw = new GridWidget();
        gw.getMainPositioner().margin(4, 4, 4, 4);
        final GridWidget.Adder adder = gw.createAdder(getColumns());
        for (ModWidget widget : widgets) {
            adder.add(new WidgetWidget(0, 0, 200, 100, client, widget, textRenderer, onEdit));
        }
        SimplePositioningWidget.setPos(gw, getX(), getY(), this.getWidth(), this.getContentsHeightWithPadding(), 0, 0);
        gw.refreshPositions();
        this.elements.clear();
        gw.forEachChild(w -> this.addWidget((WidgetWidget) w));
    }

    protected void addWidget(WidgetWidget drawableElement) {
        this.elements.add(drawableElement);
    }

    @Override
    protected int getContentsHeightWithPadding() {
        final int columns = getColumns();
        final int rows = widgets.size() / columns;
        return 10 + (rows * 108);
    }

    public int getColumns() {
        return getColumns(this.width);
    }

    public static int getColumns(int width){
        return Math.max(1, width / COLUMN_SIZE);
    }

    @Override
    protected double getDeltaYPerScroll() {
        return 35;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(getX(), getY(), getX()+width, getY()+height);
        MatrixStack stack = context.getMatrices();
        stack.push();
        stack.translate(0, (float) -getScrollY(), 0);
        for (WidgetWidget element : elements) {
            element.render(context, mouseX, (int) (mouseY + getScrollY()), delta);
        }
        stack.pop();
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseY += getScrollY();
        for (Element element : elements) {
            if (element.mouseClicked(mouseX, mouseY, button)){
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.checkScrollbarDragged(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        for (ModWidget widget : widgets) {
            builder.put(NarrationPart.HINT, widget.getName());
        }
    }

    public void search(String query) {
        widgets = this.loadWidgets(query);
        setScrollY(0);
        init();
    }
}
