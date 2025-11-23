package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.Anchor;
import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.client.WidgetManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Vector2i;

import java.awt.*;
import java.util.function.Consumer;

public class EditWidgetPositionsScreen extends AnimatedScreen {

    private final Screen parent;
    private final Consumer<ModWidget> onEdit;

    public EditWidgetPositionsScreen(Screen parent, Consumer<ModWidget> onEdit) {
        super(Text.translatable("widgets.ui.editPositions"), parent, 500);
        this.parent = parent;
        this.onEdit = onEdit;
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    private record AlignResult(double result, boolean isEnd){}

    private static final int SELECT_COLOR = Color.GREEN.getRGB(), ALIGN_COLOR = Color.ORANGE.getRGB(), ALIGN_DISABLED_COLOR = Color.GRAY.getRGB();
    private ModWidget selectedWidget = null;
    private ModWidget hoveredWidget = null;
    private int focusedExtraX = 0;
    private int focusedExtraY = 0;
    private boolean align = true;

    private int scaledWindowWidth = 1920;
    private int scaledWindowHeight = 1080;

    @Override
    public void renderScreenContents(DrawContext context, int mouseX, int mouseY, float delta) {
        this.scaledWindowWidth = context.getScaledWindowWidth();
        this.scaledWindowHeight = context.getScaledWindowHeight();
        long mt = Util.getMeasuringTimeNano();
        renderAnchorArea(context, mouseX, mouseY);

        for (ModWidget widget : WidgetManager.getEnabledWidgets()) {
            final int ww = (int) (widget.width() * widget.getScaleFactor());
            int wx = Math.min(widget.getX(scaledWindowWidth), this.width - ww);
            final int wh = (int) (widget.height() * widget.getScaleFactor());
            int wy = Math.min(widget.getY(scaledWindowHeight), this.height - wh);
            if (selectedWidget == widget){
                AlignResult alignedX = alignX(widget);
                if (alignedX != null){
                    context.drawVerticalLine(
                            (int) Math.round(alignedX.result),
                            0,
                            scaledWindowHeight,
                            align ? ALIGN_COLOR : ALIGN_DISABLED_COLOR
                    );
                }
                AlignResult alignedY = alignY(widget);
                if (alignedY != null){
                    context.drawHorizontalLine(
                            0,
                            scaledWindowWidth,
                            (int) Math.round(alignedY.result),
                            align ? ALIGN_COLOR : ALIGN_DISABLED_COLOR
                    );
                }
            }
            if (hoveredWidget == null || hoveredWidget == widget){
                if (mouseX <= wx + ww && mouseX >= wx && mouseY <= wy + wh && mouseY >= wy){
                    if (hoveredWidget == null){
                        hoveredWidget = widget;
                    }
                } else {
                    hoveredWidget = null;
                }
            }
            if (selectedWidget == null ? hoveredWidget == widget : selectedWidget == widget){
                context.drawStrokedRectangle(wx-1,wy-1, ww+2, wh+2, SELECT_COLOR);
                context.drawStrokedRectangle(wx, wy, ww, wh, SELECT_COLOR);
            }
            widget.render(context, mt, textRenderer, wx, wy);
        }

        if (hoveredWidget != null){
            context.setCursor(StandardCursors.RESIZE_ALL);
        }
    }

    private boolean canAlign(int val1, int val2){
        return ((val2 - val1) * (val2 - val1)) < 9;
    }

    private AlignResult alignX(ModWidget widget) {
        // Align with other widgets
        for (ModWidget other : WidgetManager.getEnabledWidgets()) {
            if (other == widget) continue;
            if (canAlign(widget.getX(scaledWindowWidth), other.getX(scaledWindowWidth))){
                return new AlignResult(other.getX(scaledWindowWidth), false);

            } else if (canAlign(widget.getX(scaledWindowWidth) + (int) widget.scaledWidth(), other.getX(scaledWindowWidth) + (int) other.scaledWidth())) {
                return new AlignResult(other.getX(scaledWindowWidth) + other.scaledWidth(), true);

            } else if (canAlign(widget.getX(scaledWindowWidth), other.getX(scaledWindowWidth) + (int) other.scaledWidth())){
                return new AlignResult(other.getX(scaledWindowWidth) + other.scaledWidth(), false);

            } else if (canAlign(widget.getX(scaledWindowWidth) + (int) widget.scaledWidth(), other.getX(scaledWindowWidth))){
                return new AlignResult(other.getX(scaledWindowWidth), true);

            }
        }
        return null;
    }

    private AlignResult alignY(ModWidget widget) {
        // Align with other widgets
        for (ModWidget other : WidgetManager.getEnabledWidgets()) {
            if (other == widget) continue;
            if (canAlign(widget.getY(scaledWindowHeight), other.getY(scaledWindowHeight))){
                return new AlignResult(other.getY(scaledWindowHeight), false);

            } else if (canAlign(widget.getY(scaledWindowHeight) + (int) widget.scaledHeight(), other.getY(scaledWindowHeight) + (int) other.scaledHeight())) {
                return new AlignResult(other.getY(scaledWindowHeight) + other.scaledHeight(), true);

            } else if (canAlign(widget.getY(scaledWindowHeight), other.getY(scaledWindowHeight) + (int) other.scaledHeight())){
                return new AlignResult(other.getY(scaledWindowHeight) + other.scaledHeight(), false);

            } else if (canAlign(widget.getY(scaledWindowHeight) + (int) widget.scaledHeight(), other.getY(scaledWindowHeight))){
                return new AlignResult(other.getY(scaledWindowHeight), true);

            }
        }
        return null;
    }

    private void renderAnchorArea(DrawContext context, int mouseX, int mouseY) {
        Anchor anchor = Anchor.getAnchor(
                scaledWindowWidth,
                scaledWindowHeight,
                mouseX,
                mouseY
        );
        Vector2i topLeft = anchor.getTopLeft(scaledWindowWidth, scaledWindowHeight);
        context.fill(
                topLeft.x,
                topLeft.y,
                topLeft.x + scaledWindowWidth / 3,
                topLeft.y + scaledWindowHeight / 3,
                0x08ffffff
        );
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && selectedWidget != null){
            if (align){
                AlignResult alignedX = alignX(selectedWidget);
                if (alignedX != null){
                    double diff = Math.round(alignedX.result - selectedWidget.getX(scaledWindowWidth));
                    if (alignedX.isEnd){
                        diff -= selectedWidget.scaledWidth();
                    }
                    selectedWidget.move((int) diff, 0);
                }
                AlignResult alignedY = alignY(selectedWidget);
                if (alignedY != null){
                    double diff = Math.round(alignedY.result - selectedWidget.getY(scaledWindowHeight));
                    if (alignedY.isEnd){
                        diff -= selectedWidget.scaledHeight();
                    }
                    selectedWidget.move(0, (int) diff);
                }
            }
            onEdit.accept(selectedWidget);
            selectedWidget = null;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0 && hoveredWidget != null){
            selectedWidget = hoveredWidget;
            focusedExtraX = (int) (click.x() - hoveredWidget.getX(scaledWindowWidth));
            focusedExtraY = (int) (click.y() - hoveredWidget.getY(scaledWindowHeight));
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (click.button() == 0){
            assert client != null;
            final ModWidget widget = selectedWidget;
            if (widget != null){
                final WidgetSettings settings = widget.getSettings();
                final int ww = (int) (widget.width() * widget.getScaleFactor());
                int wx = Math.min(widget.getX(scaledWindowWidth), this.width - ww);
                final int wh = (int) (widget.height() * widget.getScaleFactor());
                int wy = Math.min(widget.getY(scaledWindowHeight), this.height - wh);
                if (click.x() <= wx + ww + deltaX && click.x() >= wx + deltaX){
                    if (click.y() <= wy + wh + deltaY && click.y() >= wy + deltaY){
                        Anchor anchor = Anchor.getAnchor(scaledWindowWidth, scaledWindowHeight, (int) click.x(), (int) click.y());
                        int newOffX = (int) (click.x() - anchor.getAlignStartPosX(scaledWindowWidth)) - focusedExtraX;
                        int newOffY = (int) (click.y() - anchor.getAlignStartPosY(scaledWindowHeight)) - focusedExtraY;

                        // Ensure the thing does not go out of bounds
                        settings.setPos(anchor, newOffX, newOffY);

                        if (widget.getX(scaledWindowWidth) + ww > scaledWindowWidth){
                            newOffX -= widget.getX(scaledWindowWidth) - scaledWindowWidth + ww;
                            settings.setPos(anchor, newOffX, newOffY);
                        }
                        if (widget.getX(scaledWindowWidth) < 0){
                            newOffX -= widget.getX(scaledWindowWidth);
                            settings.setPos(anchor, newOffX, newOffY);
                        }
                        if (widget.getY(scaledWindowHeight) + wh > scaledWindowHeight){
                            newOffY -= widget.getY(scaledWindowHeight) - scaledWindowHeight + wh;
                            settings.setPos(anchor, newOffX, newOffY);
                        }
                        if (widget.getY(scaledWindowHeight) < 0){
                            newOffY -= widget.getY(scaledWindowHeight);
                            settings.setPos(anchor, newOffX, newOffY);
                        }

                        return true;
                    }
                }
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(
                new ButtonWidget.Builder(
                        Text.translatable(
                                "widgets.ui.editPositions.snap",
                                align ?
                                        Text.translatable("gui.yes") :
                                        Text.translatable("gui.no")
                        ), button -> {
                            align = !align;
                            button.setMessage(
                                    Text.translatable(
                                            "widgets.ui.editPositions.snap",
                                            align ?
                                                    Text.translatable("gui.yes")
                                                    : Text.translatable("gui.no")
                                    )
                            );
                        }).position(
                                this.width / 2 - 75,
                                this.height / 2 - 10
                        ).tooltip(
                                Tooltip.of(Text.translatable("widgets.ui.editPositions.snap.help"))
                ).build()
        );
    }
}
