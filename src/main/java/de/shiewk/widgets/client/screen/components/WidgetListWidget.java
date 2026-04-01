package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.WidgetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class WidgetListWidget extends AbstractScrollArea {

    public static final int COLUMN_SIZE = 208;
    private final Minecraft client;
    private List<ModWidget> widgets;
    private final List<WidgetWidget> elements = new ArrayList<>();
    private final Font textRenderer;
    private final Consumer<ModWidget> onEdit;

    public static boolean searchQueryMatches(String search, ModWidget widget) {
        if (search == null) return true;
        return widget.getName().getString().contains(search) || widget.getDescription().getString().contains(search) || widget.getId().toString().contains(search);
    }

    public WidgetListWidget(int x, int y, int width, int height, Minecraft client, Font textRenderer, Consumer<ModWidget> onEdit) {
        super(x, y, width, height, Component.empty(), AbstractScrollArea.defaultSettings(35));
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
        GridLayout gw = new GridLayout();
        gw.defaultCellSetting().padding(4, 4, 4, 4);
        final GridLayout.RowHelper adder = gw.createRowHelper(getColumns());
        for (ModWidget widget : widgets) {
            adder.addChild(new WidgetWidget(0, 0, 200, 100, client, widget, textRenderer, onEdit));
        }
        FrameLayout.alignInRectangle(gw, getX(), getY(), this.getWidth(), this.contentHeight(), 0, 0);
        gw.arrangeElements();
        this.elements.clear();
        gw.visitWidgets(w -> this.addWidget((WidgetWidget) w));
    }

    protected void addWidget(WidgetWidget drawableElement) {
        this.elements.add(drawableElement);
    }

    @Override
    protected int contentHeight() {
        final int columns = getColumns();
        final int rows = (int) Math.ceil((double) widgets.size() / columns);
        return 10 + (rows * 108);
    }

    public int getColumns() {
        return getColumns(this.width);
    }

    public static int getColumns(int width){
        return Math.max(1, width / COLUMN_SIZE);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.enableScissor(getX(), getY(), getX()+width, getY()+height);
        Matrix3x2fStack stack = context.pose().pushMatrix();
        stack.translate(0, (float) -scrollAmount(), stack);
        for (WidgetWidget element : elements) {
            element.extractRenderState(context, mouseX, (int) (mouseY + scrollAmount()), delta);
        }
        stack.popMatrix();
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseY = click.y();
        double mouseX = click.x();
        mouseY += scrollAmount();
        MouseButtonEvent newClick = new MouseButtonEvent(mouseX, mouseY, click.buttonInfo());
        for (GuiEventListener element : elements) {
            if (element.mouseClicked(newClick, doubled)){
                client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.updateScrolling(newClick);
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput builder) {
        for (ModWidget widget : widgets) {
            builder.add(NarratedElementType.HINT, widget.getName());
        }
    }

    public void search(String query) {
        widgets = this.loadWidgets(query);
        setScrollAmount(0);
        init();
    }
}
