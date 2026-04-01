package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.screen.components.WidgetSettingsEditWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class WidgetSettingsScreen extends AnimatedScreen implements WidgetVisibilityToggle {
    private static final Component previewText = Component.translatable("widgets.ui.preview");
    private final ModWidget widget;
    private final Runnable onChange;
    public WidgetSettingsScreen(Screen parent, ModWidget widget, Consumer<ModWidget> changedWidgetConsumer) {
        super(Component.translatable("widgets.ui.widgetSettings", widget.getName()), parent, 500);
        this.widget = widget;
        onChange = () -> {
            widget.onSettingsChanged();
            changedWidgetConsumer.accept(widget);
        };
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new WidgetSettingsEditWidget(0, 0, this.width / 2 - 8, this.height, font, widget, this.onChange));
    }

    @Override
    public void renderScreenContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.text(font, previewText, this.width * 3 / 4 - font.width(previewText) / 2, this.height / 50, 0xffffffff, false);
        widget.render(context, Util.getNanos(), font, (int) ((float) (this.width * 3) / 4 - (widget.width() * widget.getScaleFactor()) / 2), (int) ((float) this.height / 2 - (widget.height() * widget.getScaleFactor()) / 2));
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent click) {
        for (GuiEventListener child : children()) {
            if (child instanceof AbstractWidget s){
                s.mouseReleased(click);
            }
        }
        return super.mouseReleased(click);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void tick() {
        super.tick();
        if (!widget.getSettings().isEnabled()){
            widget.tick();
        }
    }

    @Override
    public boolean shouldRenderWidgets() {
        return false;
    }

    public ModWidget getWidget() {
        return widget;
    }

    public Runnable getOnChange() {
        return onChange;
    }
}
