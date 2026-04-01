package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static net.minecraft.network.chat.Component.translatable;

public class ServerIPWidget extends BasicTextWidget {
    public ServerIPWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("dynamicwidth", translatable("widgets.widgets.serverIP.dynamicWidth"), true),
                new ToggleWidgetSetting("hide_in_singleplayer", translatable("widgets.widgets.common.hideInSingleplayer"), false)
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
        final ServerData serverEntry = Minecraft.getInstance().getCurrentServer();
        if (serverEntry != null){
            formatAndSetRenderText(serverEntry.ip);
        } else {
            formatAndSetRenderText(translatable("menu.singleplayer"));
        }
        t++;
        if (dynamicWidth && t >= 20){
            t = 0;
            this.width = Minecraft.getInstance().font.width(this.renderText) + 20;
        }
    }

    @Override
    public int width() {
        return dynamicWidth ? this.width : super.width();
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.serverIP");
    }

    @Override
    public Component getDescription() {
        return translatable("widgets.widgets.serverIP.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.dynamicWidth = (boolean) settings.optionById("dynamicwidth").getValue();
        this.hideInSingleplayer = (boolean) settings.optionById("hide_in_singleplayer").getValue();
    }
}
