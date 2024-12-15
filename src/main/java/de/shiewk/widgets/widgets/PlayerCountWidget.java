package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class PlayerCountWidget extends BasicTextWidget{
    public PlayerCountWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("showlabel", Text.translatable("widgets.widgets.common.showLabel"), true)
        ));
    }

    private boolean showLabel = true;

    @Override
    public void tickWidget() {
        final ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        String online = networkHandler == null ? "?" : String.valueOf(networkHandler.getPlayerUuids().size());
        this.renderText = showLabel ? Text.literal(Text.translatable("widgets.widgets.playerCount.online", online).getString()) : Text.literal(online);
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.playerCount");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.playerCount.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        showLabel = ((ToggleWidgetSetting) settings.optionById("showlabel")).getValue();
        super.onSettingsChanged(settings);
    }
}
