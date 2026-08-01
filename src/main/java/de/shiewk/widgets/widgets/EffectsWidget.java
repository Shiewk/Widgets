package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

public class EffectsWidget extends ResizableWidget {

    public record ActiveEffect(String name, String duration, Identifier iconSprite){}

    protected ActiveEffect[] effects = new ActiveEffect[0];
    protected GradientOptions backgroundColor, textColor;
    protected int paddingIn, paddingOutX, paddingOutY, width;
    protected boolean showIcons, textShadow, visible;

    public EffectsWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("icons", translatable("widgets.widgets.effects.showIcon"), true),
                new IntSliderWidgetSetting("width", translatable("widgets.widgets.basictext.width"), 10, 115, 400),
                new IntSliderWidgetSetting("padding_in", translatable("widgets.widgets.effects.paddingIn"), -3, 3, 10),
                new IntSliderWidgetSetting("padding_out_x", translatable("widgets.widgets.effects.paddingOutX"), 0, 5, 10),
                new IntSliderWidgetSetting("padding_out_y", translatable("widgets.widgets.effects.paddingOutY"), 0, 5, 10),
                new GradientWidgetSetting("backgroundcolor", translatable("widgets.widgets.basictext.background"), 0x50_00_00_00),
                new GradientWidgetSetting("textcolor", translatable("widgets.widgets.basictext.textcolor"), 0xffffffff),
                new ToggleWidgetSetting("textshadow", translatable("widgets.widgets.basictext.textshadow"), true)
        ));
    }

    @Override
    public void renderScaled(GuiGraphicsExtractor context, long timeNanos, Font textRenderer, int posX, int posY) {
        if (visible){
            Minecraft client = Minecraft.getInstance();
            this.backgroundColor.fillHorizontal(context, timeNanos, posX, posY, posX + width(), posY + height());
            int ox = this.paddingOutX;
            int oy = this.paddingOutY;
            for (ActiveEffect effect : this.effects) {
                if (showIcons){
                    context.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            effect.iconSprite,
                            posX + ox, posY + oy,
                            24, 24
                    );
                }
                this.textColor.drawText(
                        context,
                        client.font,
                        timeNanos,
                        effect.name,
                        posX + ox + (showIcons ? 29 : 0),
                        posY + oy + 3,
                        this.textShadow
                );
                this.textColor.drawText(
                        context,
                        client.font,
                        timeNanos,
                        effect.duration,
                        posX + ox + (showIcons ? 29 : 0),
                        posY + oy + 14,
                        this.textShadow
                );
                oy += 24 + this.paddingIn;
            }
        }
    }

    @Override
    public void tick() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player != null) {
            MobEffectInstance[] mobEffects = player.getActiveEffects().toArray(MobEffectInstance[]::new);
            ActiveEffect[] modEffects = new ActiveEffect[mobEffects.length];
            for (int i = 0; i < mobEffects.length; i++) {
                MobEffectInstance effect = mobEffects[i];
                int duration = effect.getDuration();
                modEffects[i] = new ActiveEffect(
                        effect.getEffect().value().getDisplayName().getString(),
                        getDurationString(duration),
                        Gui.getMobEffectSprite(effect.getEffect())
                );
            }
            this.effects = modEffects;
        }
        this.visible = this.effects.length != 0;
    }

    public static @NonNull String getDurationString(int durationTicks) {
        if (durationTicks == MobEffectInstance.INFINITE_DURATION){
            return "∞";
        }
        String hourPart = String.valueOf(durationTicks / 72000);
        String minutePart = String.valueOf(durationTicks / 1200 % 60);
        String secondPart = String.valueOf(durationTicks / 20 % 60);
        if (minutePart.length() < 2){
            minutePart = "0" + minutePart;
        }
        if (secondPart.length() < 2){
            secondPart = "0" + secondPart;
        }
        return hourPart + ':' + minutePart + ':' + secondPart;
    }

    @Override
    public int width() {
        return width + paddingOutX * 2;
    }

    @Override
    public int height() {
        return Math.max(this.effects.length, 1) * (24 + paddingIn) + paddingOutY * 2 - paddingIn;
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.textColor = (GradientOptions) settings.optionById("textcolor").getValue();
        this.backgroundColor = (GradientOptions) settings.optionById("backgroundcolor").getValue();
        this.paddingIn = (int) settings.optionById("padding_in").getValue();
        this.paddingOutX = (int) settings.optionById("padding_out_x").getValue();
        this.paddingOutY = (int) settings.optionById("padding_out_y").getValue();
        this.width = (int) settings.optionById("width").getValue();
        this.showIcons = (boolean) settings.optionById("icons").getValue();
        this.textShadow = (boolean) settings.optionById("textshadow").getValue();
    }
}
