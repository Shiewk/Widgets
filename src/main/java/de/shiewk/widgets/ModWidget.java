package de.shiewk.widgets;

import de.shiewk.widgets.widgets.settings.WidgetSettingOption;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public abstract class ModWidget {

    private final Identifier id;
    private final WidgetSettings settings;

    protected ModWidget(Identifier id, List<WidgetSettingOption<?>> customSettings) {
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
    public abstract void render(GuiGraphicsExtractor context, long measuringTimeNano, Font textRenderer, int posX, int posY);
    public abstract void tick();
    public abstract Component getName();
    public abstract Component getDescription();
    public abstract void onSettingsChanged(WidgetSettings settings);

    public void onSettingsChanged() {
        this.onSettingsChanged(this.getSettings());
    }

    public int getX(int scaledScreenWidth){
        return settings.anchor.getAlignStartPosX(scaledScreenWidth) + settings.offsetX;
    }

    public int getY(int scaledScreenHeight){
        return settings.anchor.getAlignStartPosY(scaledScreenHeight) + settings.offsetY;
    }

    public abstract int width();
    public abstract int height();

    public float scaledWidth() {
        return width() * getScaleFactor();
    }

    public float scaledHeight() {
        return height() * getScaleFactor();
    }

    public void move(int dx, int dy) {
        settings.offsetX += dx;
        settings.offsetY += dy;
    }

    public void setPos(Anchor anchor, int offsetX, int offsetY){
        getSettings().setPos(anchor, offsetX, offsetY);
    }

    public void setAbsolutePos(int x, int y, int scaledWindowWidth, int scaledWindowHeight) {
        Anchor anchor = Anchor.getAnchor(scaledWindowWidth, scaledWindowHeight, x, y);
        if (anchor == null) {
            throw new IllegalArgumentException("Provided coordinates have no corresponding anchor");
        }
        settings.setPos(
                anchor,
                x - anchor.getAlignStartPosX(scaledWindowWidth),
                y - anchor.getAlignStartPosY(scaledWindowHeight)
        );
    }
}
