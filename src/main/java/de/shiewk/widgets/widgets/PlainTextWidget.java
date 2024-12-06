package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.TextFieldWidgetSettingOption;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class PlainTextWidget extends BasicTextWidget {
    public PlainTextWidget(Identifier id) {
        super(id, List.of(
                new TextFieldWidgetSettingOption("text", Text.translatable("widgets.widgets.plaintext.text"), Text.translatable("widgets.widgets.plaintext.initial"), Text.translatable("widgets.widgets.plaintext.placeholder"), true, 200)
        ));
    }

    @Override
    public void tickWidget() {}

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.plaintext");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.plaintext.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        renderText = Text.literal(((TextFieldWidgetSettingOption) settings.optionById("text")).getValue());
    }
}
