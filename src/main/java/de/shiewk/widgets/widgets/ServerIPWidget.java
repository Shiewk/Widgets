package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.WidgetUtils;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ServerIPWidget extends BasicTextWidget {
    public ServerIPWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("dynamicwidth", Text.translatable("widgets.widgets.serverIP.dynamicWidth"), true),
                new ToggleWidgetSetting("hide_in_singleplayer", Text.translatable("widgets.widgets.common.hideInSingleplayer"), false)
        ));
        getSettings().optionById("width").setShowCondition(() -> !this.dynamicWidth);
    }

    private int width;
    private int t = 0;

    private boolean dynamicWidth = true;
    private boolean hideInSingleplayer = false;

    @Override
    public void tickWidget() {
        shouldRender = !(this.hideInSingleplayer && WidgetUtils.isInSingleplayer());
        if (!shouldRender) return;
        final ServerInfo serverEntry = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverEntry != null){
            this.renderText = Text.of(serverEntry.address);
        } else {
            this.renderText = Text.translatable("menu.singleplayer");
        }
        t++;
        if (t >= 20){
            t = 0;
            this.width = MinecraftClient.getInstance().textRenderer.getWidth(this.renderText) + 20;
        }
    }

    @Override
    public int width() {
        return dynamicWidth ? this.width : super.width();
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.serverIP");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.serverIP.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.dynamicWidth = ((ToggleWidgetSetting) settings.optionById("dynamicwidth")).getValue();
        this.hideInSingleplayer = ((ToggleWidgetSetting) settings.optionById("hide_in_singleplayer")).getValue();
    }
}
