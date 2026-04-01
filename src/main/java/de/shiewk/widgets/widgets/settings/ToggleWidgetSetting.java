package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ToggleWidgetSetting extends WidgetSettingOption<Boolean> {

    private boolean value;
    private long toggleTime = 0;

    public ToggleWidgetSetting(String id, Component name, boolean defaultValue) {
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
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int col = value ? COLOR_ENABLED : COLOR_DISABLED;
        int thumb = value ? COLOR_ENABLED_THUMB : COLOR_DISABLED_THUMB;
        int thumbLoc = (Util.getNanos() - toggleTime) < 300000000f ?
                (int) (getX() + Mth.lerp(WidgetUtils.computeEasing((Util.getNanos() - toggleTime) / 300000000f),
                        value ? 2 : getWidth() - 4 - 12,
                        value ? getWidth() - 4 - 12 : 4))
                : value ? getX() + getWidth() - 4 - 12 : getX() + 4;
        context.fill(getX() + 2, getY() + 2, getX() + getWidth() - 2, getY() + getHeight() - 2, col);
        context.fill(thumbLoc, getY() + 4, thumbLoc + 12, getY() + getHeight() - 4, thumb);

        if (this.isHovered(mouseX, mouseY)){
            context.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        toggle();
        return true;
    }

    public void toggle(){
        this.value = !value;
        WidgetUtils.playSound(value ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF);
        this.toggleTime = Util.getNanos();
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
