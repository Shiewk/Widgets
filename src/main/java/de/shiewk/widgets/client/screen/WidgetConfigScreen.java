package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.WidgetManager;
import de.shiewk.widgets.client.screen.components.WidgetListWidget;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class WidgetConfigScreen extends Screen {

    private final Screen parent;
    private final long creationTime = Util.getMeasuringTimeNano();
    private String search = "";
    private WidgetListWidget widgetList = null;
    private final ArrayList<ModWidget> widgetsEdited = new ArrayList<>();
    private final Consumer<ModWidget> onWidgetEdit = this::changedSettings;

    private double getScreenTimeMs(){
        return (Util.getMeasuringTimeNano() - creationTime) / 1000000d;
    }

    public WidgetConfigScreen(Screen parent) {
        super(Text.translatable("widgets.ui.config"));
        this.parent = parent;
    }

    @Override
    public void close() {
        WidgetManager.saveWidgets(widgetsEdited);
        for (ModWidget widget : widgetsEdited) {
            widget.onSettingsChanged(widget.getSettings());
        }
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();
        AxisGridWidget agw = new AxisGridWidget(3, height - 22, width - 6, 20, AxisGridWidget.DisplayAxis.HORIZONTAL);
        final TextFieldWidget searchField = new TextFieldWidget(textRenderer, this.width - 160, 20, Text.empty());
        searchField.setPlaceholder(Text.translatable("widgets.ui.search"));
        searchField.setChangedListener(this::search);
        if (this.widgetList != null){
            searchField.setText(this.getSearchQuery());
        }
        agw.add(searchField);
        agw.add(new ButtonWidget.Builder(Text.translatable("widgets.ui.editPositions"), this::switchToEditPositions).build());

        agw.refreshPositions();
        agw.forEachChild(this::addDrawableChild);

        if (Objects.equals(search, "")){
            search("");
        }
    }

    private String getSearchQuery() {
        return search;
    }

    public void setSearchQuery(String search) {
        this.search = search;
    }

    private void search(String query) {
        this.setSearchQuery(query);
        widgetList = new WidgetListWidget(0, 0, width-4, height-24, Text.translatable("widgets.ui.config"), client, WidgetManager.getAllWidgets().stream().filter(this::searchQueryMatches).toList(), textRenderer, this.onWidgetEdit);
    }

    private void switchToEditPositions(ButtonWidget widget) {
        widget.active = false;
        assert client != null;
        client.setScreen(new EditWidgetPositionsScreen(this, this.onWidgetEdit));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)){
            return true;
        } else return widgetList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)){
            return true;
        } else return widgetList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        assert client != null;
        final double time = getScreenTimeMs();
        if (time < 400){
            Matrix3x2fStack stack = context.getMatrices().pushMatrix();
            final float v = (float) WidgetUtils.computeEasing(time / 400d);
            stack.translate((float) (width / 2d - (width * v / 2d)), (float) (height / 2d - (height * v / 2d)), stack);
            stack.scale(v, v, stack);
        }
        super.render(context, mouseX, mouseY, delta);
        if (widgetList != null){
            widgetList.render(context, mouseX, mouseY, delta);
        }
        if (time < 400){
            context.getMatrices().popMatrix();
        }
    }

    private boolean searchQueryMatches(ModWidget widget) {
        return widget.getName().getString().contains(search) || widget.getDescription().getString().contains(search) || widget.getId().toString().contains(search);
    }

    public void changedSettings(ModWidget widget) {
        if (!widgetsEdited.contains(widget)){
            widgetsEdited.add(widget);
        }
    }
}
