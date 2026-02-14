package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.WidgetManager;
import de.shiewk.widgets.client.screen.components.ScaledTextWidget;
import de.shiewk.widgets.client.screen.components.WidgetListWidget;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.function.Consumer;

public class WidgetConfigScreen extends Screen {

    private final Screen parent;
    private final long creationTime = Util.getMeasuringTimeNano();
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
            widget.onSettingsChanged();
        }
        assert client != null;
        client.setScreen(parent);
    }

    int widgetListWidth;

    @Override
    protected void init() {
        super.init();

        int widgetColumns = WidgetListWidget.getColumns(width / 2);
        widgetListWidth = widgetColumns * WidgetListWidget.COLUMN_SIZE;

        // Right side
        final WidgetListWidget widgetList = new WidgetListWidget(width - widgetListWidth - 4, 0, widgetListWidth, height - 24, client, textRenderer, onWidgetEdit);

        final TextFieldWidget searchField = new TextFieldWidget(textRenderer, widgetListWidth, 20, Text.empty());
        searchField.setPlaceholder(Text.translatable("widgets.ui.search"));
        searchField.setChangedListener(widgetList::search);
        searchField.setX(width - widgetListWidth - 4);
        searchField.setY(height - 24);

        this.addDrawableChild(widgetList);
        this.addDrawableChild(searchField);
        setInitialFocus(searchField);

        // Left side
        GridWidget gw = new GridWidget(0, 0);
        gw.getMainPositioner().margin(4, 4, 4, 4);
        GridWidget.Adder adder = gw.createAdder(1);
        {
            ScaledTextWidget title = new ScaledTextWidget(0, 0, Text.literal("ᴡɪᴅɢᴇᴛѕ"), textRenderer, width < 600 ? 4 : 6);
            adder.add(title);

            ButtonWidget editPosButton = ButtonWidget.builder(Text.translatable("widgets.ui.editPositions"), this::switchToEditPositions).build();
            adder.add(editPosButton);
        }
        gw.refreshPositions();
        SimplePositioningWidget.setPos(gw, 0, 0, width - widgetListWidth, height, .5f, .5f);
        gw.forEachChild(c -> c.setX((width - widgetListWidth) / 2 - (c.getWidth() / 2)));
        gw.forEachChild(this::addDrawableChild);
    }

    private void switchToEditPositions(ButtonWidget widget) {
        widget.active = false;
        assert client != null;
        client.setScreen(new EditWidgetPositionsScreen(this, this.onWidgetEdit));
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

        if (time < 400){
            context.getMatrices().popMatrix();
        }
    }

    public void changedSettings(ModWidget widget) {
        if (!widgetsEdited.contains(widget)){
            widgetsEdited.add(widget);
        }
    }
}
