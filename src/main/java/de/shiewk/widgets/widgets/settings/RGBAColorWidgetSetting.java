package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.shiewk.widgets.WidgetSettingOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

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
    private boolean mouseClicked = false;
    private int sv = 0;

    @Override
    public JsonElement saveState() {
        return new JsonPrimitive(getColor());
    }

    public int getColor(){
        return new Color(r, g, b, a).getRGB();
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
        return 28 + 127 + 7;
    }

    @Override
    public int getHeight() {
        return 95;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int o = 0;
        for (int i = 0; i <= 255; i+=2) {
            context.drawVerticalLine(this.getX() + 5 + 7 + o, this.getY() + 5,  this.getY() + 20, new Color(i, g, b, a).getRGB());
            context.drawVerticalLine(this.getX() + 5 + 7 + o, this.getY() + 25, this.getY() + 40, new Color(r, i, b, a).getRGB());
            context.drawVerticalLine(this.getX() + 5 + 7 + o, this.getY() + 45, this.getY() + 60, new Color(r, g, i, a).getRGB());
            context.drawVerticalLine(this.getX() + 5 + 7 + o, this.getY() + 65, this.getY() + 80, new Color(r, g, b, i).getRGB());
            o++;
        }

        context.drawText(textRenderer, "R", this.getX() + 7 - textRenderer.getWidth("R"), this.getY() + 5  + 4, 0xffffffff, true);
        context.drawText(textRenderer, "G", this.getX() + 7 - textRenderer.getWidth("G"), this.getY() + 25 + 4, 0xffffffff, true);
        context.drawText(textRenderer, "B", this.getX() + 7 - textRenderer.getWidth("B"), this.getY() + 45 + 4, 0xffffffff, true);
        context.drawText(textRenderer, "A", this.getX() + 7 - textRenderer.getWidth("A"), this.getY() + 65 + 4, 0xffffffff, true);

        context.drawText(textRenderer, String.valueOf(r), this.getX() + this.getWidth() - 19, this.getY() +  5 + 4, 0xffffffff, true);
        context.drawText(textRenderer, String.valueOf(g), this.getX() + this.getWidth() - 19, this.getY() + 25 + 4, 0xffffffff, true);
        context.drawText(textRenderer, String.valueOf(b), this.getX() + this.getWidth() - 19, this.getY() + 45 + 4, 0xffffffff, true);
        context.drawText(textRenderer, String.valueOf(a), this.getX() + this.getWidth() - 19, this.getY() + 65 + 4, 0xffffffff, true);

        context.drawVerticalLine(this.getX() + 5 + 7 + r/2, this.getY() + 4,  this.getY() + 21, 0xffffffff);
        context.drawVerticalLine(this.getX() + 5 + 7 + g/2, this.getY() + 24, this.getY() + 41, 0xffffffff);
        context.drawVerticalLine(this.getX() + 5 + 7 + b/2, this.getY() + 44, this.getY() + 61, 0xffffffff);
        context.drawVerticalLine(this.getX() + 5 + 7 + a/2, this.getY() + 64, this.getY() + 81, 0xffffffff);

        if (mouseClicked){
            int col = MathHelper.clamp((mouseX - this.getX() - 5 - 7) * 2, 0, 255);
            if (sv == 0){
                if (mouseY > this.getY() + 5 && mouseY < this.getY() + 20){
                    sv = 1;
                } else if (mouseY > this.getY() + 25 && mouseY < this.getY() + 40){
                    sv = 2;
                } else if (mouseY > this.getY() + 45 && mouseY < this.getY() + 60){
                    sv = 3;
                } else if (mouseY > this.getY() + 65 && mouseY < this.getY() + 80){
                    sv = 4;
                }
            }
            switch (sv){
                case 1 -> r = col;
                case 2 -> g = col;
                case 3 -> b = col;
                case 4 -> a = col;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.mouseClicked = true;
        sv = 0;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mouseClicked = false;
        final boolean c = this.sv != 0;
        this.sv = 0;
        return c;
    }
}
