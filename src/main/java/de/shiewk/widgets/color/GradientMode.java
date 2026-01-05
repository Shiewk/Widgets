package de.shiewk.widgets.color;

import net.minecraft.text.Text;

import static net.minecraft.text.Text.translatable;

public enum GradientMode {
    SWEEP(translatable("widgets.gradient.sweep"), translatable("widgets.gradient.sweep.description")),
    PULSE(translatable("widgets.gradient.pulse"), translatable("widgets.gradient.pulse.description"));

    public final Text name;
    public final Text description;

    GradientMode(Text name, Text description) {
        this.name = name;
        this.description = description;
    }
}
