package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.translatable;

public class PlayerCountWidget extends BasicTextWidget{
    public PlayerCountWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("showlabel", translatable("widgets.widgets.common.showLabel"), true),
                new ToggleWidgetSetting("hide_in_singleplayer", translatable("widgets.widgets.common.hideInSingleplayer"), false)
        ));
    }

    private boolean showLabel = true;
    private boolean hideInSingleplayer = false;

    @Override
    public void tickWidget() {
        shouldRender = !(hideInSingleplayer && WidgetUtils.isInSingleplayer());
        if (!shouldRender) return;
        final ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        String online = networkHandler == null ? "?" : String.valueOf(networkHandler.getListedPlayerListEntries().size());
        formatAndSetRenderText(showLabel ? literal(translatable("widgets.widgets.playerCount.online", online).getString()) : literal(online));
    }

    @Override
    public Text getName() {
        return translatable("widgets.widgets.playerCount");
    }

    @Override
    public Text getDescription() {
        return translatable("widgets.widgets.playerCount.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        showLabel = ((ToggleWidgetSetting) settings.optionById("showlabel")).getValue();
        hideInSingleplayer = ((ToggleWidgetSetting) settings.optionById("hide_in_singleplayer")).getValue();
        super.onSettingsChanged(settings);
    }
}
