package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

public class ContextMenuScreen extends Screen implements WidgetVisibilityToggle {

    public record Option(Text title, boolean highlighted, Runnable action){

        public Option(Text title, Runnable action){
            this(title, false, action);
        }

    }

    private final Screen parent;
    private int menuX;
    private int menuY;
    private final List<Option> options;

    private int menuWidth;
    private int menuHeight;

    public ContextMenuScreen(Text title, Screen parent, int menuX, int menuY, List<Option> options) {
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
            int width = textRenderer.getWidth(option.title);
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
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isInBounds(mouseX, mouseY)){
            close();
            return false;
        }
        int opt = (int) (mouseY - menuY - 1) / 15;
        if (opt < options.size()){
            Option option = options.get(opt);
            close();
            WidgetUtils.playSound(SoundEvents.BLOCK_COPPER_BULB_TURN_OFF);
            option.action.run();
        }
        return false;
    }

    private boolean isInBounds(double x, double y) {
        return x > menuX && y > menuY && x < menuX + menuWidth && y < menuY + menuHeight;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        parent.render(context, -67, -67, deltaTicks);
        context.drawBorder(
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

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    public void renderMenu(DrawContext context, int mouseX, int mouseY){
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
            }
            context.drawText(textRenderer, option.title, menuX + 5, y + 3, option.highlighted ? 0xff_00_ff_ff : 0xff_ff_ff_ff, false);
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
