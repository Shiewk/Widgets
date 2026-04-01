package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.WidgetManager;
import de.shiewk.widgets.client.screen.components.ScaledTextWidget;
import de.shiewk.widgets.client.screen.components.WidgetListWidget;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.function.Consumer;

public class WidgetConfigScreen extends Screen {

    private final Screen parent;
    private final long creationTime = Util.getNanos();
    private final ArrayList<ModWidget> widgetsEdited = new ArrayList<>();
    private final Consumer<ModWidget> onWidgetEdit = this::changedSettings;

    private double getScreenTimeMs(){
        return (Util.getNanos() - creationTime) / 1000000d;
    }

    public WidgetConfigScreen(Screen parent) {
        super(Component.translatable("widgets.ui.config"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        WidgetManager.saveWidgets(widgetsEdited);
        for (ModWidget widget : widgetsEdited) {
            widget.onSettingsChanged();
        }
        minecraft.setScreen(parent);
    }

    int widgetListWidth;

    @Override
    protected void init() {
        super.init();

        int widgetColumns = WidgetListWidget.getColumns(width / 2);
        widgetListWidth = widgetColumns * WidgetListWidget.COLUMN_SIZE;

        // Right side
        final WidgetListWidget widgetList = new WidgetListWidget(width - widgetListWidth - 4, 0, widgetListWidth, height - 24, minecraft, font, onWidgetEdit);

        final EditBox searchField = new EditBox(font, widgetListWidth, 20, Component.empty());
        searchField.setHint(Component.translatable("widgets.ui.search"));
        searchField.setResponder(widgetList::search);
        searchField.setX(width - widgetListWidth - 4);
        searchField.setY(height - 24);

        this.addRenderableWidget(widgetList);
        this.addRenderableWidget(searchField);
        setInitialFocus(searchField);

        // Left side
        GridLayout gw = new GridLayout(0, 0);
        gw.defaultCellSetting().padding(4, 4, 4, 4);
        GridLayout.RowHelper adder = gw.createRowHelper(1);
        {
            ScaledTextWidget title = new ScaledTextWidget(0, 0, Component.literal("ᴡɪᴅɢᴇᴛѕ"), font, width < 600 ? 4 : 6);
            adder.addChild(title);

            Button editPosButton = Button.builder(Component.translatable("widgets.ui.editPositions"), this::switchToEditPositions).build();
            adder.addChild(editPosButton);
        }
        gw.arrangeElements();
        FrameLayout.alignInRectangle(gw, 0, 0, width - widgetListWidth, height, .5f, .5f);
        gw.visitWidgets(c -> c.setX((width - widgetListWidth) / 2 - (c.getWidth() / 2)));
        gw.visitWidgets(this::addRenderableWidget);
    }

    private void switchToEditPositions(Button widget) {
        widget.active = false;
        minecraft.setScreen(new EditWidgetPositionsScreen(this, this.onWidgetEdit));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        final double time = getScreenTimeMs();
        if (time < 400){
            Matrix3x2fStack stack = context.pose().pushMatrix();
            final float v = (float) WidgetUtils.computeEasing(time / 400d);
            stack.translate((float) (width / 2d - (width * v / 2d)), (float) (height / 2d - (height * v / 2d)), stack);
            stack.scale(v, v, stack);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);

        if (time < 400){
            context.pose().popMatrix();
        }
    }

    public void changedSettings(ModWidget widget) {
        if (!widgetsEdited.contains(widget)){
            widgetsEdited.add(widget);
        }
    }
}
