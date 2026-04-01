package de.shiewk.widgets.client.screen.gradienteditor;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.utils.WidgetUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.IntSupplier;

import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.translatable;

public class GradientEditorColorSection extends ObjectSelectionList<GradientEditorColorSection.ListEntry> {

    private final GradientEditorScreen editor;
    private static final Identifier TEXTURE_BUTTON_PLUS = Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "textures/gui/button_plus.png"),
                                    TEXTURE_ARROW_DOWN = Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "textures/gui/arrow_down.png");

    private final List<ColorEntry> colorEntries;

    public GradientEditorColorSection(GradientEditorScreen editor, Minecraft client, int x, int y, int width, int height, int focusedIndex) {
        super(client, width, height, y, 64);
        setX(x);
        this.editor = editor;
        colorEntries = new ObjectArrayList<>(editor.colors.size());
        addEntry(new HeadingEntry(client.font), 18);

        IntArrayList colors = editor.colors;
        for (int i = 0; i < colors.size(); i++) {
            int finalI = i;
            ColorEntry col = new ColorEntry(() -> colors.getInt(finalI));
            colorEntries.add(col);
            addEntry(col);
            addEntry(new ArrowDownEntry(), 48);
        }

        addEntry(new AddButtonEntry(), 70);
        setFocused(colorEntries.get(focusedIndex));
    }

    @Override
    public int getRowWidth() {
        return 64;
    }

    @Override
    protected void extractSelection(@NonNull GuiGraphicsExtractor context, @NonNull ListEntry entry, int color) {}

    @Override
    protected void extractListBackground(GuiGraphicsExtractor context) {
        context.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 0x50_00_00_00);
    }

    @Override
    protected void extractListSeparators(@NonNull GuiGraphicsExtractor context) {}

    @Override
    protected void extractScrollbar(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY) {}

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (focused == null) return;
        if (focused instanceof ListEntry se && !se.canFocus()) return;

        if (super.getFocused() != focused){
            WidgetUtils.playSound(SoundEvents.COPPER_BULB_TURN_ON);
        }
        if (focused instanceof ColorEntry color){
            editor.setCurrentColorIndex(colorEntries.indexOf(color));
        }
        super.setFocused(focused);
    }

    public void focusLast() {
        setFocused(colorEntries.getLast());
    }

    public abstract static class ListEntry extends ObjectSelectionList.Entry<ListEntry> {

        public boolean canFocus(){
            return false;
        }

        @Override
        public @NonNull Component getNarration() {
            return empty();
        }
    }

    public static class ColorEntry extends ListEntry {

        private final IntSupplier color;

        public ColorEntry(IntSupplier color) {
            this.color = color;
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int color = this.color.getAsInt();
            int x = this.getX();
            int y = this.getContentY();

            if (hovered && !isFocused()){
                context.requestCursor(CursorTypes.POINTING_HAND);
                context.fill(x, y, x+getWidth(), y+getHeight(), 0x30_ff_ff_ff);
            }

            int outlineColor;
            if (isFocused() || hovered){
                outlineColor = 0xff_ff_ff_00;
            } else {
                outlineColor = color | 0xff000000;
            }

            context.fill(x+1, y+1, x+getWidth()-1, y+getHeight()-1, color);

            // Outline:
            context.fill(x+1, y, x+getWidth()-1, y+1, outlineColor);
            context.fill(x+1, y+getHeight()-1, x+getWidth()-1, y+getHeight(), outlineColor);
            context.fill(x, y+1, x+1, y+getHeight()-1, outlineColor);
            context.fill(x+getWidth()-1, y+1, x+getWidth(), y+getHeight()-1, outlineColor);
        }

        @Override
        public boolean canFocus() {
            return true;
        }
    }

    private static class ArrowDownEntry extends ListEntry {

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE_ARROW_DOWN,
                    getX() + 16,
                    getContentY() + 8,
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

        private final Font textRenderer;

        private HeadingEntry(Font textRenderer) {
            this.textRenderer = textRenderer;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            Component text = translatable("widgets.ui.gradientEditor.colors");

            int y = this.getContentY() + 2;
            int rowWidth = 64;

            int textWidth = textRenderer.width(text);
            int textX = getX() + (rowWidth - textWidth) / 2;
            context.text(textRenderer, text, textX, y, 0xffffffff, true);
        }
    }

    private class AddButtonEntry extends ListEntry {
        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE_BUTTON_PLUS,
                    getX(),
                    getContentY(),
                    0,
                    0,
                    64,
                    64,
                    64,
                    64,
                    hovered ? 0xff_cf_cf_cf : 0xff_8d_8d_8d
            );
            if (hovered){
                context.requestCursor(CursorTypes.POINTING_HAND);
                context.setComponentTooltipForNextFrame(minecraft.font, List.of(
                        translatable("widgets.ui.gradientEditor.colors.add.tooltip.0"),
                        translatable("widgets.ui.gradientEditor.colors.add.tooltip.1")
                ), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
            editor.addNewColor(editor.getCurrentColor());
            return true;
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
