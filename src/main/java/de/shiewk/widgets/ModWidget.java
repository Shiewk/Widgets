package de.shiewk.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public abstract class ModWidget {

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
