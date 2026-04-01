package de.shiewk.widgets.color;

import static net.minecraft.network.chat.Component.translatable;

import net.minecraft.network.chat.Component;

public record GradientPreset(Component name, GradientOptions gradient) {

    public static final GradientPreset[] presets = new GradientPreset[]{
            new GradientPreset(
                    translatable("widgets.gradient.preset.rainbow"),
                    new GradientOptions(
                            GradientMode.PULSE,
                            14,
                            20,
                            new int[]{
                                    0xff_ff_00_00,
                                    0xff_ff_88_00,
                                    0xff_ff_ff_00,
                                    0xff_00_ff_00,
                                    0xff_00_ff_ff,
                                    0xff_50_00_80,
                                    0xff_ff_00_ff,
                            }
                    )
            )
    };

}
