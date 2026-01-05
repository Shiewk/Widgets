package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ToggleWidgetSetting extends WidgetSettingOption<Boolean> {

    private boolean value;
    private long toggleTime = 0;

    public ToggleWidgetSetting(String id, Text name, boolean defaultValue) {
        super(id, name);
        this.value = defaultValue;
    }

    @Override
    public @NotNull Boolean getValue(){
        return value;
    }

    @Override
    public JsonElement saveState() {
        return new JsonPrimitive(value);
    }

    @Override
    public void loadState(JsonElement state) {
        if (state.isJsonPrimitive() && state.getAsJsonPrimitive().isBoolean()){
            this.value = state.getAsBoolean();
        }
    }

    protected static final int COLOR_ENABLED = new Color(0, 255, 0, 255).getRGB(),
                            COLOR_ENABLED_THUMB = new Color(0, 175, 0, 255).getRGB(),
                            COLOR_DISABLED = new Color(255, 0, 0, 255).getRGB(),
                            COLOR_DISABLED_THUMB = new Color(255, 200, 200, 255).getRGB();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int col = value ? COLOR_ENABLED : COLOR_DISABLED;
        int thumb = value ? COLOR_ENABLED_THUMB : COLOR_DISABLED_THUMB;
        int thumbLoc = (Util.getMeasuringTimeNano() - toggleTime) < 300000000f ?
                (int) (getX() + MathHelper.lerp(WidgetUtils.computeEasing((Util.getMeasuringTimeNano() - toggleTime) / 300000000f),
                        value ? 2 : getWidth() - 4 - 12,
                        value ? getWidth() - 4 - 12 : 4))
                : value ? getX() + getWidth() - 4 - 12 : getX() + 4;
        context.fill(getX() + 2, getY() + 2, getX() + getWidth() - 2, getY() + getHeight() - 2, col);
        context.fill(thumbLoc, getY() + 4, thumbLoc + 12, getY() + getHeight() - 4, thumb);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        toggle();
        return true;
    }

    public void toggle(){
        this.value = !value;
        WidgetUtils.playSound(value ? SoundEvents.BLOCK_COPPER_BULB_TURN_ON : SoundEvents.BLOCK_COPPER_BULB_TURN_OFF);
        this.toggleTime = Util.getMeasuringTimeNano();
    }

    @Override
    public int getWidth() {
        return 50;
    }

    @Override
    public int getHeight() {
        return 20;
    }
}
