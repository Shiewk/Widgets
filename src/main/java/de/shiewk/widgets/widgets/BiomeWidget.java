package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class BiomeWidget extends BasicTextWidget {

    public BiomeWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("show_label", Text.translatable("widgets.widgets.biome.showLabel"), true)
        ));
    }

    private long tickCounter = 0;
    private boolean showLabel = true;

    @Override
    public void tickWidget() {
        if (++tickCounter % 20 == 0){
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            ClientWorld world = client.world;
            if (world != null && player != null){
                RegistryEntry<Biome> biome = world.getBiome(player.getBlockPos());
                String text = biome.getKeyOrValue().map(
                        (biomeKey) -> {
                            if (showLabel){
                                return Text.translatable("widgets.widgets.biome.label", Text.translatable(biomeKey.getValue().toTranslationKey("biome"))).getString();
                            } else {
                                return Text.translatable(biomeKey.getValue().toTranslationKey("biome")).getString();
                            }
                        },
                        (b) -> "[unregistered " + b + "]"
                );
                this.renderText = Text.literal(text);
            } else {
                if (showLabel){
                    this.renderText = Text.translatable("widgets.widgets.biome.label", "?");
                } else {
                    this.renderText = Text.literal("?");
                }
            }
        }
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.biome");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.biome.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.showLabel = ((ToggleWidgetSetting) settings.optionById("show_label")).getValue();
    }
}
