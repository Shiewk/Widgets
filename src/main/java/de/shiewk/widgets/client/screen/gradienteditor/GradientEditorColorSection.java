package de.shiewk.widgets.client.screen.gradienteditor;

import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.utils.WidgetUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntSupplier;

import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.translatable;

public class GradientEditorColorSection extends AlwaysSelectedEntryListWidget<GradientEditorColorSection.ListEntry> {

    private final GradientEditorScreen editor;
    private static final Identifier TEXTURE_BUTTON_PLUS = Identifier.of(WidgetsMod.MOD_ID, "textures/gui/button_plus.png"),
            TEXTURE_ARROW_DOWN = Identifier.of(WidgetsMod.MOD_ID, "textures/gui/arrow_down.png");

    private final List<ColorEntry> colorEntries;

    public GradientEditorColorSection(GradientEditorScreen editor, MinecraftClient client, int x, int y, int width, int height, int focusedIndex) {
        super(client, width, height, y, 64);
        setX(x);
        this.editor = editor;
        colorEntries = new ObjectArrayList<>(editor.colors.size());
        addEntry(new HeadingEntry(client.textRenderer));

        IntArrayList colors = editor.colors;
        for (int i = 0; i < colors.size(); i++) {
            int finalI = i;
            ColorEntry col = new ColorEntry(() -> colors.getInt(finalI));
            colorEntries.add(col);
            addEntry(col);
            addEntry(new ArrowDownEntry());
        }

        addEntry(new AddButtonEntry());
        setFocused(colorEntries.get(focusedIndex));
    }

    @Override
    public int getRowWidth() {
        return 64;
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {}

    @Override
    protected void drawMenuListBackground(DrawContext context) {
        context.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 0x50_00_00_00);
    }

    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {}

    @Override
    protected void drawScrollbar(DrawContext context) {}

    @Override
    public void setFocused(@Nullable Element focused) {
        if (focused == null) return;
        if (focused instanceof ListEntry se && !se.canFocus()) return;

        if (super.getFocused() != focused){
            WidgetUtils.playSound(SoundEvents.BLOCK_COPPER_BULB_TURN_ON);
        }
        if (focused instanceof ColorEntry color){
            editor.setCurrentColorIndex(colorEntries.indexOf(color));
        }
        super.setFocused(focused);
    }

    public void focusLast() {
        setFocused(colorEntries.getLast());
    }

    public abstract static class ListEntry extends AlwaysSelectedEntryListWidget.Entry<ListEntry> {

        public boolean canFocus(){
            return false;
        }

        @Override
        public Text getNarration() {
            return empty();
        }
    }

    public static class ColorEntry extends ListEntry {

        private final IntSupplier color;

        public ColorEntry(IntSupplier color) {
            this.color = color;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int color = this.color.getAsInt();

            if (hovered && !isFocused()){
                context.fill(x, y, x+entryWidth, y+entryHeight, 0x30_ff_ff_ff);
            }

            int outlineColor;
            if (isFocused() || hovered){
                outlineColor = 0xff_ff_ff_00;
            } else {
                outlineColor = color | 0xff000000;
            }

            context.fill(x+1, y+1, x+entryWidth-1, y+entryHeight-1, color);

            // Outline:
            context.fill(x+1, y, x+entryWidth-1, y+1, outlineColor);
            context.fill(x+1, y+entryHeight-1, x+entryWidth-1, y+entryHeight, outlineColor);
            context.fill(x, y+1, x+1, y+entryHeight-1, outlineColor);
            context.fill(x+entryWidth-1, y+1, x+entryWidth, y+entryHeight-1, outlineColor);
        }

        @Override
        public boolean canFocus() {
            return true;
        }
    }

    private static class ArrowDownEntry extends ListEntry {

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE_ARROW_DOWN,
                    x + 16,
                    y + 16,
                    0,
                    0,
                    32,
                    32,
                    32,
                    32,
                    0xff_8d_8d_8d
            );
        }
    }

    private static class HeadingEntry extends ListEntry {

        private final TextRenderer textRenderer;

        private HeadingEntry(TextRenderer textRenderer) {
            this.textRenderer = textRenderer;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Text text = translatable("widgets.ui.gradientEditor.colors");

            y += 2;
            int rowWidth = 64;

            int textWidth = textRenderer.getWidth(text);
            int textX = x + (rowWidth - textWidth) / 2;
            context.drawText(textRenderer, text, textX, y, 0xffffffff, true);
        }
    }

    private class AddButtonEntry extends ListEntry {

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE_BUTTON_PLUS,
                    x,
                    y,
                    0,
                    0,
                    64,
                    64,
                    64,
                    64,
                    hovered ? 0xff_cf_cf_cf : 0xff_8d_8d_8d
            );
            if (hovered){
                context.drawTooltip(client.textRenderer, List.of(
                        translatable("widgets.ui.gradientEditor.colors.add.tooltip.0"),
                        translatable("widgets.ui.gradientEditor.colors.add.tooltip.1")
                ), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            editor.addNewColor(editor.getCurrentColor());
            return true;
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}