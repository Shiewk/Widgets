package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.screen.components.WidgetSettingsEditWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.function.Consumer;

public class WidgetSettingsScreen extends AnimatedScreen {
    private static final Text previewText = Text.translatable("widgets.ui.preview");
    private final ModWidget widget;
    private final Runnable onChange;
    public WidgetSettingsScreen(Screen parent, ModWidget widget, Consumer<ModWidget> changedWidgetConsumer) {
        super(Text.translatable("widgets.ui.widgetSettings", widget.getName()), parent, 500);
        this.widget = widget;
        onChange = () -> {
            widget.onSettingsChanged(widget.getSettings());
            changedWidgetConsumer.accept(widget);
        };
    }

    @Override
    protected void init() {
        super.init();
        addDrawableChild(new WidgetSettingsEditWidget(0, 0, this.width / 2 - 8, this.height, textRenderer, widget, this.onChange));
    }

    @Override
    public void renderScreenContents(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawText(textRenderer, previewText, this.width * 3 / 4 - textRenderer.getWidth(previewText) / 2, this.height / 50, 0xffffffff, false);
        widget.render(context, Util.getMeasuringTimeNano(), textRenderer, (int) ((float) (this.width * 3) / 4 - (widget.width() * widget.getScaleFactor()) / 2), (int) ((float) this.height / 2 - (widget.height() * widget.getScaleFactor()) / 2));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Element child : children()) {
            if (child instanceof ClickableWidget s){
                s.mouseReleased(mouseX, mouseY, button);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    public void tick() {
        super.tick();
        if (!widget.getSettings().isEnabled()){
            widget.tick();
        }
    }
}
