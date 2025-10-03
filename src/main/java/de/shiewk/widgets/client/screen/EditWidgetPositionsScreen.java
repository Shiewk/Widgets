package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.Dimensionable;
import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.client.WidgetManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Consumer;

import static de.shiewk.widgets.utils.WidgetUtils.translateToScreen;
import static de.shiewk.widgets.utils.WidgetUtils.translateToWidgetSettingsValue;

public class EditWidgetPositionsScreen extends AnimatedScreen {

    private record Alignment(int x, int y, int width, int height) implements Dimensionable {

            @Override
            public double getX(int mx) {
                return x;
            }

            @Override
            public double getY(int my) {
                return y;
            }

        @Override
        public float getScaleFactor() {
            return 1f;
        }
    }

    private final Screen parent;
    private final Consumer<ModWidget> onEdit;
    private boolean alignX = true;
    private boolean alignY = true;
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

    private @Nullable AlignResult alignX(double x, int width, ModWidget widget){
        double endX = x + width;
        int factor = alignX ? 2 : 0;
        for (Dimensionable rect : this.getAlignments(widget)) {
            if (rect == widget) continue;
            final int rectWidth = rect.width();
            final double nwx = Math.min(rect.getX(this.width), this.width - rectWidth);
            final double nww = rectWidth * rect.getScaleFactor();
            if (endX < nwx + factor && endX > nwx - factor){
                return new AlignResult(nwx - width, true);
            } else if (x < nwx + factor && x > nwx - factor){
                return new AlignResult(nwx, false);
            } else if (endX < nwx+nww + factor && endX > nwx + nww - factor){
                return new AlignResult(nwx + nww - width, true);
            } else if (x < nwx+nww + factor && x > nwx + nww - factor){
                return new AlignResult(nwx + nww, false);
            }
        }
        return null;
    }

    private Iterable<? extends Dimensionable> getAlignments(Dimensionable rel) {
        ObjectArrayList<Dimensionable> alignments = new ObjectArrayList<>();
        for (ModWidget widget : WidgetManager.getEnabledWidgets()) {
            alignments.add(widget);
        }
        alignments.add(new Alignment(2, 2, this.width / 2 - 2, this.height / 2 - 2));
        alignments.add(new Alignment(this.width / 2, this.height / 2, this.width / 2 - 2, this.height / 2 - 2));
        alignments.add(new Alignment(2, this.height / 2, this.width / 2 - 2, this.height / 2 - 2));
        alignments.add(new Alignment(this.width / 2, 2, this.width / 2 - 2, this.height / 2 - 2));
        alignments.add(new Alignment((int) ((float) this.width / 2 - (rel.width() * rel.getScaleFactor()) / 2), (int) ((float) this.height / 2 - (rel.height() * rel.getScaleFactor()) / 2), (int) (rel.width() * rel.getScaleFactor()), (int) (rel.height() * rel.getScaleFactor())));
        return alignments;
    }


    private @Nullable AlignResult alignY(double y, int height, ModWidget widget){
        double endY = y + height;
        int factor = alignY ? 2 : 0;
        for (Dimensionable rect : this.getAlignments(widget)) {
            if (rect == widget) continue;
            final int rectHeight = rect.height();
            final double nwy = Math.min(rect.getY(this.height), this.height - rectHeight);
            final double nwh = rectHeight * rect.getScaleFactor();
            if (endY < nwy + factor && endY > nwy - factor){
                return new AlignResult(nwy - height, true);
            } else if (y < nwy + factor && y > nwy - factor){
                return new AlignResult(nwy, false);
            } else if (endY < nwy+nwh + factor && endY > nwy + nwh - factor){
                return new AlignResult(nwy + nwh - height, true);
            } else if (y < nwy+nwh + factor && y > nwy + nwh - factor){
                return new AlignResult(nwy + nwh, false);
            }
        }
        return null;
    }

    private static final int SELECT_COLOR = Color.GREEN.getRGB(), ALIGN_COLOR = Color.ORANGE.getRGB(), ALIGN_DISABLED_COLOR = Color.GRAY.getRGB();
    private ModWidget selectedWidget = null;
    private ModWidget hoveredWidget = null;

    @Override
    public void renderScreenContents(DrawContext context, int mouseX, int mouseY, float delta) {
        assert client != null;
        for (ModWidget widget : WidgetManager.getEnabledWidgets()) {
            final WidgetSettings settings = widget.getSettings();
            final int ww = (int) (widget.width() * widget.getScaleFactor());
            double wx = Math.min(translateToScreen(settings.posX, this.width), this.width - ww);
            final int wh = (int) (widget.height() * widget.getScaleFactor());
            double wy = Math.min(translateToScreen(settings.posY, this.height), this.height - wh);
            if (selectedWidget == widget){
                final AlignResult alignedX = alignX(wx, ww, widget);
                if (alignedX != null){
                    context.drawVerticalLine((int) (!alignedX.isEnd() ? alignedX.result() : alignedX.result() + ww), 0, this.height, alignX ? ALIGN_COLOR : ALIGN_DISABLED_COLOR);
                    wx = alignedX.result();
                }
                final AlignResult alignedY = alignY(wy, wh, widget);
                if (alignedY != null){
                    context.drawHorizontalLine(0, this.width, (int) (!alignedY.isEnd() ? alignedY.result() : alignedY.result() + wh), alignY ? ALIGN_COLOR : ALIGN_DISABLED_COLOR);
                    wy = alignedY.result();
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
                context.drawStrokedRectangle((int) Math.round(wx-1), (int) Math.round(wy-1), ww+2, wh+2, SELECT_COLOR);
                context.drawStrokedRectangle((int) Math.round(wx), (int) Math.round(wy), ww, wh, SELECT_COLOR);
            }
            widget.render(context, Util.getMeasuringTimeNano(), textRenderer, (int) Math.round(wx), (int) Math.round(wy));
        }
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && selectedWidget != null){
            final AlignResult alignedX = alignX(translateToScreen(selectedWidget.getSettings().posX, this.width), (int) (selectedWidget.width() * selectedWidget.getScaleFactor()), selectedWidget);
            if (alignedX != null){
                selectedWidget.getSettings().setPosX(translateToWidgetSettingsValue(alignedX.result(), this.width), (int) (selectedWidget.width() * selectedWidget.getScaleFactor()), this.width);
            }
            final AlignResult alignedY = alignY(translateToScreen(selectedWidget.getSettings().posY, this.height), (int) (selectedWidget.height() * selectedWidget.getScaleFactor()), selectedWidget);
            if (alignedY != null){
                selectedWidget.getSettings().setPosY(translateToWidgetSettingsValue(alignedY.result(), this.height), (int) (selectedWidget.height() * selectedWidget.getScaleFactor()), this.height);
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
                final int wx = (int) Math.min(translateToScreen(settings.posX, this.width), this.width - ww);
                final int wh = (int) (widget.height() * widget.getScaleFactor());
                final int wy = (int) Math.min(translateToScreen(settings.posY, this.height), this.height - wh);
                if (click.x() <= wx + ww + deltaX && click.x() >= wx + deltaX){
                    if (click.y() <= wy + wh + deltaY && click.y() >= wy + deltaY){
                        double newPosX = settings.posX + translateToWidgetSettingsValue(deltaX, this.width);
                        double newPosY = settings.posY + translateToWidgetSettingsValue(deltaY, this.height);
                        settings.setPosX(newPosX, ww, this.width);
                        settings.setPosY(newPosY, wh, this.height);
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
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("widgets.ui.editPositions.snap", alignX ? Text.translatable("gui.yes") : Text.translatable("gui.no")), button -> {
            alignX = !alignX;
            alignY = !alignY;
            button.setMessage(Text.translatable("widgets.ui.editPositions.snap", alignX ? Text.translatable("gui.yes") : Text.translatable("gui.no")));
        }).position(this.width / 2 - 75, this.height / 2 - 10).tooltip(Tooltip.of(Text.translatable("widgets.ui.editPositions.snap.help"))).build());
    }
}
