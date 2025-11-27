package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.shiewk.widgets.WidgetSettingOption;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class RGBAColorWidgetSetting extends WidgetSettingOption {
    public RGBAColorWidgetSetting(String id, Text name, int defaultR, int defaultG, int defaultB, int defaultAlpha) {
        super(id, name);
        this.r = defaultR;
        this.g = defaultG;
        this.b = defaultB;
        this.a = defaultAlpha;
    }

    private int r;
    private int g;
    private int b;
    private int a;

    @Override
    public JsonElement saveState() {
        return new JsonPrimitive(getColor());
    }

    public int getColor(){
        return a << 24 | r << 16 | g << 8 | b;
    }

    @Override
    public void loadState(JsonElement state) {
        if (state.isJsonPrimitive() && state.getAsJsonPrimitive().isNumber()){
            final Color color = new Color(state.getAsJsonPrimitive().getAsInt(), true);
            this.r = color.getRed();
            this.g = color.getGreen();
            this.b = color.getBlue();
            this.a = color.getAlpha();
        }
    }

    @Override
    public int getWidth() {
        return 72;
    }

    @Override
    public int getHeight() {
        return 24;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.fill(
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight(),
                getColor()
        );
        context.drawHorizontalLine(getX(), getX()+getWidth(), getY(), getColor() | 0xff_00_00_00);
        context.drawHorizontalLine(getX(), getX()+getWidth(), getY()+getHeight(), getColor() | 0xff_00_00_00);
        context.drawVerticalLine(getX(), getY(), getY() + getHeight(), getColor() | 0xff_00_00_00);
        context.drawVerticalLine(getX() + getWidth(), getY(), getY() + getHeight(), getColor() | 0xff_00_00_00);

        String colorText = "#" + toHexString();
        int width = textRenderer.getWidth(colorText);
        context.drawText(
                textRenderer,
                colorText,
                getX() + (getWidth() / 2 - (width / 2)),
                getY() + (getHeight() / 2 - 4),
                0xff_ff_ff_ff,
                true
        );
    }

    private String toHexSingle(int comp){
        String s = Integer.toHexString(comp);
        return "0".repeat(2 - s.length()) + s;
    }

    public String toHexString() {
        return toHexSingle(r) + toHexSingle(g) + toHexSingle(b) + toHexSingle(a);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        WidgetUtils.playSound(SoundEvents.BLOCK_COPPER_BULB_TURN_ON);
        client.setScreen(
                new ChangeScreen(client.currentScreen, (int) (client.mouse.getX() / client.getWindow().getScaleFactor()), (int) (client.mouse.getY() / client.getWindow().getScaleFactor()))
        );
        return true;
    }

    public class ChangeScreen extends Screen {

        private final Screen parent;
        private int x;
        private int y;

        protected ChangeScreen(Screen parent, int x, int y) {
            super(Text.empty());
            this.parent = parent;
            this.x = x;
            this.y = y;
        }

        private static final int PADDING = 16;
        private static final int BAR_WIDTH = 18;
        private static final int BAR_HEIGHT = 127 + 20;

        private static final int RECT_WIDTH = 5 * PADDING + 4 * BAR_WIDTH;
        private static final int RECT_HEIGHT = 2 * PADDING + BAR_HEIGHT;

        @Override
        protected void init() {
            super.init();
            if (y + RECT_HEIGHT > height){
                y = height - RECT_HEIGHT;
            }
            if (x + RECT_WIDTH > width){
                x = width - RECT_WIDTH;
            }
            // Color components
            // Red color
            addDrawableChild(new ColorBar(
                    x + PADDING,
                    y + PADDING,
                    BAR_WIDTH,
                    BAR_HEIGHT,
                    0
            ));
            // Green color
            addDrawableChild(new ColorBar(
                    x + 2* PADDING + BAR_WIDTH,
                    y + PADDING,
                    BAR_WIDTH,
                    BAR_HEIGHT,
                    1
            ));
            // Blue color
            addDrawableChild(new ColorBar(
                    x + 3* PADDING + 2* BAR_WIDTH,
                    y + PADDING,
                    BAR_WIDTH,
                    BAR_HEIGHT,
                    2
            ));
            // Alpha
            addDrawableChild(new ColorBar(
                    x + 4* PADDING + 3* BAR_WIDTH,
                    y + PADDING,
                    BAR_WIDTH,
                    BAR_HEIGHT,
                    3
            ));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            parent.render(context, 0, 0, deltaTicks);
            context.fill(x, y, x+ RECT_WIDTH, y+ RECT_HEIGHT,0xc0_00_00_00);
            context.drawBorder(x, y, RECT_WIDTH, RECT_HEIGHT, 0x67_ff_ff_ff);
            super.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {

        }

        @Override
        public void close() {
            client.setScreen(parent);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX < x || mouseY < y || mouseX > x + RECT_WIDTH || mouseY > y + RECT_HEIGHT){
                close();
                WidgetUtils.playSound(SoundEvents.BLOCK_COPPER_BULB_TURN_OFF);
                return false;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public class ColorBar extends ClickableWidget {

            private final int component;

            public ColorBar(int x, int y, int width, int height, int component) {
                super(x, y, width, height, Text.empty());
                this.component = component;
            }

            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (isHovered()){
                    setValue((int) (getValue() + verticalAmount * 2));
                    return true;
                }
                return false;
            }

            public int getValue(){
                return switch (component){
                    case 0 -> RGBAColorWidgetSetting.this.r; // Red
                    case 1 -> RGBAColorWidgetSetting.this.g; // Green
                    case 2 -> RGBAColorWidgetSetting.this.b; // Blue
                    case 3 -> RGBAColorWidgetSetting.this.a; // Alpha
                    default -> throw new IllegalStateException("Component out of range: " + component);
                };
            }

            public void setValue(int val){
                val = MathHelper.clamp(val, 0, 255);
                switch (component){
                    case 0 -> RGBAColorWidgetSetting.this.r = val; // Red
                    case 1 -> RGBAColorWidgetSetting.this.g = val; // Green
                    case 2 -> RGBAColorWidgetSetting.this.b = val; // Blue
                    case 3 -> RGBAColorWidgetSetting.this.a = val; // Alpha
                    default -> throw new IllegalStateException("Component out of range: " + component);
                }
            }

            @Override
            protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
                context.fillGradient(
                        getX() + 2,
                        getY() + 10,
                        getX() + getWidth() - 2,
                        getY() + getHeight() - 10,
                        topColor(),
                        bottomColor()
                );
                context.drawHorizontalLine(
                        getX(),
                        getX() + getWidth() - 1,
                        getY() + 10 + (255 - getValue()) / 2,
                        0xffffffff
                );
                {
                    String text = ""+getValue();
                    int textWidth = textRenderer.getWidth(text);
                    context.drawText(textRenderer, text, getX() + (getWidth() / 2 - textWidth / 2), getY() - 2, 0xffffffff, true);
                }
                {
                    String text = componentLabel();
                    int textWidth = textRenderer.getWidth(text);
                    context.drawText(textRenderer, text, getX() + (getWidth() / 2 - textWidth / 2), getY() + 142, 0xffffffff, true);
                }
            }

            private String componentLabel() {
                return switch (component){
                    case 0 -> "R"; // Red
                    case 1 -> "G"; // Green
                    case 2 -> "B"; // Blue
                    case 3 -> "A"; // Alpha
                    default -> throw new IllegalStateException("Component out of range: " + component);
                };
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return this.mouseDragged(mouseX, mouseY, button, 0, 0);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double offsetX, double offsetY) {
                if (isHovered()){
                    double pos = mouseY - this.getY() - 10;
                    int val = (int) (255 - pos * 2);
                    setValue(val);
                    return true;
                }
                return false;
            }

            private int componentMask(){
                return switch (component){
                    case 0 -> 0x00_ff_00_00; // Red
                    case 1 -> 0x00_00_ff_00; // Green
                    case 2 -> 0x00_00_00_ff; // Blue
                    case 3 -> 0xff_00_00_00; // Alpha
                    default -> throw new IllegalStateException("Component out of range: " + component);
                };
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_UP) {
                    setValue(getValue() + 1);
                    return true;
                } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    setValue(getValue() - 1);
                    return true;
                } else {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }
            }

            private int bottomColor() {
                return RGBAColorWidgetSetting.this.getColor() & ~componentMask();
            }

            private int topColor() {
                return RGBAColorWidgetSetting.this.getColor() | componentMask();
            }

            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
        }
    }
}
