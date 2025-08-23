package de.shiewk.widgets;

import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public abstract class ModWidget implements Dimensionable {

    private final Identifier id;
    private final WidgetSettings settings;

    protected ModWidget(Identifier id, List<WidgetSettingOption> customSettings) {
        Objects.requireNonNull(id, "id");
        this.id = id;
        this.settings = WidgetSettings.ofId(id, customSettings);
    }

    public float getScaleFactor(){
        return 1f;
    }

    public final Identifier getId() {
        return id;
    }

    public final WidgetSettings getSettings() {
        return settings;
    }
    public abstract void render(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY);
    public abstract void tick();
    public abstract Text getName();
    public abstract Text getDescription();
    public abstract void onSettingsChanged(WidgetSettings settings);

    @Override
    public double getX(int mx) {
        return (int) WidgetUtils.translateToScreen(settings.posX, mx);
    }

    @Override
    public double getY(int my) {
        return (int) WidgetUtils.translateToScreen(settings.posY, my);
    }

}
