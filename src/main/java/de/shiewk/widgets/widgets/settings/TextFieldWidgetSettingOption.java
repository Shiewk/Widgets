package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.shiewk.widgets.WidgetsMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class TextFieldWidgetSettingOption extends WidgetSettingOption<String> {

    private TextField textField = null;
    private final Text initialValue;
    private final Text placeholder;
    private final boolean trim;
    private final int maxLength;
    private String value = "";

    private void setValue(String value){
        this.value = value;
    }

    public class TextField extends TextFieldWidget {

        public TextField(TextRenderer textRenderer, int width, int height, Text text) {
            super(textRenderer, width, height, text);
            this.setChangedListener(value -> TextFieldWidgetSettingOption.this.setValue(TextFieldWidgetSettingOption.this.trim ? value.trim() : value));
        }

        @Override
        public boolean isFocused() {
            return TextFieldWidgetSettingOption.this.isFocused();
        }
    }

    public TextFieldWidgetSettingOption(String id, Text name, Text initialValue, Text placeholder, boolean trim, int maxLength) {
        super(id, name);
        this.initialValue = initialValue;
        this.placeholder = placeholder;
        this.trim = trim;
        this.maxLength = maxLength;
    }

    private void initializeTextField() {
        if (textField != null) return;
        textField = new TextField(MinecraftClient.getInstance().textRenderer, this.getWidth(), this.getHeight(), Text.empty());
        textField.setPlaceholder(placeholder);
        textField.setMaxLength(maxLength);
        textField.setText(value.isEmpty() ? initialValue.getString() : value);
    }

    @Override
    public String getValue(){
        return value;
    }

    @Override
    public JsonElement saveState() {
        return new JsonPrimitive(value);
    }

    @Override
    public void loadState(JsonElement state) {
        if (state.isJsonPrimitive() && state.getAsJsonPrimitive().isString()){
            if (textField != null) textField.setText(state.getAsString());
            this.value = state.getAsString();
        } else {
            WidgetsMod.LOGGER.warn("Failed to load text field widget setting option for state {}", state);
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        initializeTextField();
        textField.setX(getX());
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        initializeTextField();
        textField.setY(getY());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        initializeTextField();
        textField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        return isFocused() && textField.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        return isFocused() && textField.mouseReleased(click);
    }

    @Override
    public boolean charTyped(CharInput input) {
        return isFocused() && textField.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return isFocused() && textField.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        return isFocused() && textField.keyReleased(input);
    }

    @Override
    public void setMaxRenderWidth(int maxRenderWidth) {
        super.setMaxRenderWidth(maxRenderWidth);
        initializeTextField();
        textField.setWidth(maxRenderWidth);
    }

    @Override
    public int getWidth() {
        return getMaxRenderWidth();
    }

    @Override
    public int getHeight() {
        return 20;
    }
}
