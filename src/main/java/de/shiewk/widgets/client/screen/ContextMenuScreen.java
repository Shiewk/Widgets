package de.shiewk.widgets.client.screen;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ContextMenuScreen extends Screen implements WidgetVisibilityToggle {

    public record Option(Component title, boolean highlighted, Runnable action){

        public Option(Component title, Runnable action){
            this(title, false, action);
        }

    }

    private final Screen parent;
    private int menuX;
    private int menuY;
    private final List<Option> options;

    private int menuWidth;
    private int menuHeight;

    public ContextMenuScreen(Component title, Screen parent, int menuX, int menuY, List<Option> options) {
        super(title);
        this.parent = parent;
        this.menuX = menuX;
        this.menuY = menuY;
        this.options = options;
    }

    @Override
    public void tick() {
        menuWidth = computeMenuWidth();
        menuHeight = computeMenuHeight();

        if (menuX + menuWidth > width){
            menuX = width - menuWidth;
        }
        if (menuY + menuHeight > height){
            menuY = height - menuHeight;
        }
    }

    private int computeMenuHeight() {
        return options.size() * (9 + 6) + 1;
    }

    private int computeMenuWidth() {
        int max = 0;
        for (Option option : options) {
            int width = font.width(option.title);
            if (width > max){
                max = width;
            }
        }
        return max + 10;
    }

    @Override
    protected void init() {
        tick();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!isInBounds(click.x(), click.y())){
            onClose();
            return false;
        }
        int opt = (int) (click.y() - menuY - 1) / 15;
        if (opt < options.size()){
            Option option = options.get(opt);
            onClose();
            WidgetUtils.playSound(SoundEvents.COPPER_BULB_TURN_OFF);
            option.action.run();
        }
        return false;
    }

    private boolean isInBounds(double x, double y) {
        return x > menuX && y > menuY && x < menuX + menuWidth && y < menuY + menuHeight;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
        parent.extractRenderState(context, -67, -67, deltaTicks);
        context.outline(
                menuX,
                menuY,
                menuWidth,
                menuHeight,
                0x67ffffff
        );
        context.fill(
                menuX,
                menuY,
                menuX + menuWidth,
                menuY + menuHeight,
                0xff000000
        );
        renderMenu(context, mouseX, mouseY);
    }

    public void renderMenu(GuiGraphicsExtractor context, int mouseX, int mouseY){
        int y = menuY + 1;
        for (Option option : options) {
            boolean hover = isInBounds(mouseX, mouseY) && mouseY >= y && mouseY < y + 15;
            if (hover){
                context.fill(
                        menuX,
                        y,
                        menuX + menuWidth,
                        y + 15,
                        0x30_ff_ff_ff
                );
                context.requestCursor(CursorTypes.POINTING_HAND);
            }
            context.text(font, option.title, menuX + 5, y + 3, option.highlighted ? 0xff_00_ff_ff : 0xff_ff_ff_ff, false);
            y += 15;
        }
    }

    @Override
    public boolean shouldRenderWidgets() {
        if (parent instanceof WidgetVisibilityToggle t) {
            return t.shouldRenderWidgets();
        }
        return true;
    }
}
