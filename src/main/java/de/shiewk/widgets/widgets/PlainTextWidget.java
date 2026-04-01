package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.TextFieldWidgetSettingOption;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PlainTextWidget extends BasicTextWidget {
    public PlainTextWidget(Identifier id) {
        super(id, List.of(
                new TextFieldWidgetSettingOption("text", Component.translatable("widgets.widgets.plaintext.text"), Component.translatable("widgets.widgets.plaintext.initial"), Component.translatable("widgets.widgets.plaintext.placeholder"), true, 200)
        ));
    }

    @Override
    public void tickWidget() {}

    @Override
    public Component getName() {
        return Component.translatable("widgets.widgets.plaintext");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("widgets.widgets.plaintext.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        formatAndSetRenderText((String) settings.optionById("text").getValue());
    }
}
