package de.shiewk.widgets.color;

import static net.minecraft.network.chat.Component.translatable;

import net.minecraft.network.chat.Component;

public enum GradientMode {
    SWEEP(translatable("widgets.gradient.sweep"), translatable("widgets.gradient.sweep.description")),
    PULSE(translatable("widgets.gradient.pulse"), translatable("widgets.gradient.pulse.description"));

    public final Component name;
    public final Component description;

    GradientMode(Component name, Component description) {
        this.name = name;
        this.description = description;
    }
}
