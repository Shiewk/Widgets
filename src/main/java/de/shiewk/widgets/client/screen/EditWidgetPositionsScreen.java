package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.Anchor;
import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.client.WidgetManager;
import de.shiewk.widgets.utils.WidgetUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2i;

import java.awt.*;
import java.util.List;
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
            int wx = MathHelper.clamp(widget.getX(scaledWindowWidth), 0, this.width - ww);
            final int wh = (int) (widget.height() * widget.getScaleFactor());
            int wy = MathHelper.clamp(widget.getY(scaledWindowHeight), 0, this.height - wh);
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
        int widgetX = widget.getX(scaledWindowWidth);

        // Align with center
        if (canAlign(widgetX, scaledWindowWidth / 2)){
            return new AlignResult(scaledWindowWidth / 2d, false);

        } else if (canAlign(widgetX + (int) widget.scaledWidth(), scaledWindowWidth / 2)){
            return new AlignResult(scaledWindowWidth / 2d, true);

        } else if (canAlign(widgetX + (int) widget.scaledWidth(), scaledWindowWidth / 2)){
            return new AlignResult(scaledWindowWidth / 2d, true);

        } else if (canAlign(widgetX + (int) widget.scaledWidth() / 2, scaledWindowWidth / 2)){
            return new AlignResult(scaledWindowWidth / 2d - widget.scaledWidth() / 2, false);
        }

        // Align with edges
        if (canAlign(widgetX, 3)){
            return new AlignResult(3, false);
        } else if (canAlign((int) (widgetX + widget.scaledWidth()), scaledWindowWidth - 3)){
            return new AlignResult(scaledWindowWidth - 3, true);
        }

        // Align with other widgets
        for (ModWidget other : WidgetManager.getEnabledWidgets()) {
            if (other == widget) continue;
            int otherX = other.getX(scaledWindowWidth);
            if (canAlign(widgetX, otherX)){
                return new AlignResult(otherX, false);

            } else if (canAlign(widgetX + (int) widget.scaledWidth(), otherX + (int) other.scaledWidth())) {
                return new AlignResult(otherX + other.scaledWidth(), true);

            } else if (canAlign(widgetX, otherX + (int) other.scaledWidth())){
                return new AlignResult(otherX + other.scaledWidth(), false);

            } else if (canAlign(widgetX + (int) widget.scaledWidth(), otherX)){
                return new AlignResult(otherX, true);

            }
        }

        return null;
    }

    private AlignResult alignY(ModWidget widget) {
        int widgetY = widget.getY(scaledWindowHeight);

        // Align with center
        if (canAlign(widgetY, scaledWindowHeight / 2)){
            return new AlignResult(scaledWindowHeight / 2d, false);

        } else if (canAlign(widgetY + (int) widget.scaledHeight(), scaledWindowHeight / 2)){
            return new AlignResult(scaledWindowHeight / 2d, true);

        } else if (canAlign(widgetY + (int) widget.scaledHeight(), scaledWindowHeight / 2)){
            return new AlignResult(scaledWindowHeight / 2d, true);

        } else if (canAlign(widgetY + (int) widget.scaledHeight() / 2, scaledWindowHeight / 2)){
            return new AlignResult(scaledWindowHeight / 2d - widget.scaledHeight() / 2, false);
        }

        // Align with edges
        if (canAlign(widgetY, 3)){
            return new AlignResult(3, false);
        } else if (canAlign((int) (widgetY + widget.scaledHeight()), scaledWindowHeight - 3)){
            return new AlignResult(scaledWindowHeight - 3, true);
        }

        // Align with other widgets
        for (ModWidget other : WidgetManager.getEnabledWidgets()) {
            if (other == widget) continue;
            int otherY = other.getY(scaledWindowHeight);
            if (canAlign(widgetY, otherY)){
                return new AlignResult(otherY, false);

            } else if (canAlign(widgetY + (int) widget.scaledHeight(), otherY + (int) other.scaledHeight())) {
                return new AlignResult(otherY + other.scaledHeight(), true);

            } else if (canAlign(widgetY, otherY + (int) other.scaledHeight())){
                return new AlignResult(otherY + other.scaledHeight(), false);

            } else if (canAlign(widgetY + (int) widget.scaledHeight(), otherY)){
                return new AlignResult(otherY, true);

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
        if (anchor != null) {
            Vector2i topLeft = anchor.getTopLeft(scaledWindowWidth, scaledWindowHeight);
            context.fill(
                    topLeft.x,
                    topLeft.y,
                    topLeft.x + scaledWindowWidth / 3,
                    topLeft.y + scaledWindowHeight / 3,
                    0x08ffffff
            );
        }
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
        } else if (click.button() == 1){
            int x = (int) click.x();
            int y = (int) click.y();
            ModWidget hovered = hoveredWidget;
            WidgetUtils.playSound(SoundEvents.BLOCK_COPPER_BULB_TURN_ON);
            assert client != null;
            if (hovered != null){
                client.setScreen(new ContextMenuScreen(
                        Text.empty(),
                        this,
                        x,
                        y,
                        List.of(
                                new ContextMenuScreen.Option(
                                        Text.translatable("widgets.ui.editPositions.menu.widgetSettings"),
                                        () -> client.setScreen(new WidgetSettingsScreen(
                                                this,
                                                hovered,
                                                this.onEdit
                                        ))
                                ),
                                new ContextMenuScreen.Option(
                                        Text.translatable("widgets.ui.editPositions.menu.setAnchor"),
                                        () -> {
                                            List<ContextMenuScreen.Option> options = new ObjectArrayList<>(Anchor.values().length);
                                            for (Anchor anchor : Anchor.values()) {
                                                options.add(new ContextMenuScreen.Option(
                                                        Text.translatable("widgets.ui.anchor." + anchor.name().toLowerCase()),
                                                        hovered.getSettings().anchor == anchor,
                                                        () -> {
                                                            hovered.setPos(
                                                                    anchor,
                                                                    hovered.getX(scaledWindowWidth) - anchor.getAlignStartPosX(scaledWindowWidth),
                                                                    hovered.getY(scaledWindowHeight) - anchor.getAlignStartPosY(scaledWindowHeight)
                                                            );
                                                            onEdit.accept(hovered);
                                                        }
                                                ));
                                            }
                                            // Add widget context menu
                                            client.setScreen(new ContextMenuScreen(
                                                    Text.empty(),
                                                    this,
                                                    x,
                                                    y,
                                                    options
                                            ));
                                        }
                                ),
                                new ContextMenuScreen.Option(
                                        Text.translatable("widgets.ui.editPositions.menu.removeWidget"),
                                        () -> {
                                            hovered.getSettings().toggleEnabled(hovered);
                                            onEdit.accept(hovered);
                                        }
                                )
                        )
                ));
            } else {
                client.setScreen(new ContextMenuScreen(
                        Text.empty(),
                        this,
                        x,
                        y,
                        List.of(
                                new ContextMenuScreen.Option(
                                        Text.translatable("widgets.ui.editPositions.menu.addWidget"),
                                        () -> {
                                            List<ContextMenuScreen.Option> options = new ObjectArrayList<>();
                                            for (ModWidget widget : WidgetManager.getAllWidgets()) {
                                                if (!widget.getSettings().isEnabled()){
                                                    options.add(new ContextMenuScreen.Option(
                                                            widget.getName(),
                                                            () -> {
                                                                widget.getSettings().setEnabled(widget, true);
                                                                widget.setAbsolutePos(x, y, scaledWindowWidth, scaledWindowHeight);
                                                                onEdit.accept(widget);
                                                            }
                                                    ));
                                                }
                                            }
                                            // Add widget context menu
                                            client.setScreen(new ContextMenuScreen(
                                                    Text.empty(),
                                                    this,
                                                    x,
                                                    y,
                                                    options
                                            ));
                                        }
                                )
                        )
                ));
            }
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
                int wx = MathHelper.clamp(widget.getX(scaledWindowWidth), 0, this.width - ww);
                final int wh = (int) (widget.height() * widget.getScaleFactor());
                int wy = MathHelper.clamp(widget.getY(scaledWindowHeight), 0, this.height - wh);
                if (click.x() <= wx + ww + deltaX && click.x() >= wx + deltaX){
                    if (click.y() <= wy + wh + deltaY && click.y() >= wy + deltaY){
                        Anchor anchor = Anchor.getAnchor(scaledWindowWidth, scaledWindowHeight, (int) click.x(), (int) click.y());
                        if (anchor == null) {
                            return false;
                        }
                        int newOffX = (int) (click.x() - anchor.getAlignStartPosX(scaledWindowWidth)) - focusedExtraX;
                        int newOffY = (int) (click.y() - anchor.getAlignStartPosY(scaledWindowHeight)) - focusedExtraY;

                        settings.setPos(anchor, newOffX, newOffY);

                        // Ensure the thing does not go out of bounds
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
