package de.shiewk.widgets.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static net.minecraft.text.Text.literal;

public class FPSWidget extends BasicTextWidget {
    public FPSWidget(Identifier id) {
        super(id, List.of());
    }

    @Override
    public void tickWidget() {
        formatAndSetRenderText(literal(MinecraftClient.getInstance().getCurrentFps() + " FPS"));
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.fps");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.fps.description");
    }
}
