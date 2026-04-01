package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import static net.minecraft.network.chat.Component.translatable;

public class BiomeWidget extends BasicTextWidget {

    public BiomeWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("show_label", translatable("widgets.widgets.common.showLabel"), true)
        ));
    }

    private long tickCounter = 0;
    private boolean showLabel = true;

    @Override
    public void tickWidget() {
        if (++tickCounter % 20 == 0){
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            ClientLevel world = client.level;
            if (world != null && player != null){
                Holder<Biome> biome = world.getBiome(player.blockPosition());
                String text = biome.unwrap().map(
                        (biomeKey) -> {
                            if (showLabel){
                                return translatable("widgets.widgets.biome.label", translatable(biomeKey.identifier().toLanguageKey("biome"))).getString();
                            } else {
                                return translatable(biomeKey.identifier().toLanguageKey("biome")).getString();
                            }
                        },
                        (b) -> "[unregistered " + b + "]"
                );
                formatAndSetRenderText(text);
            } else {
                if (showLabel){
                    formatAndSetRenderText(translatable("widgets.widgets.biome.label", "?"));
                } else {
                    formatAndSetRenderText("?");
                }
            }
        }
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.biome");
    }

    @Override
    public Component getDescription() {
        return translatable("widgets.widgets.biome.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.showLabel = (boolean) settings.optionById("show_label").getValue();
    }
}
