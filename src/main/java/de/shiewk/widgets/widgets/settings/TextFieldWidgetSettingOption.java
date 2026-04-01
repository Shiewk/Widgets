package de.shiewk.widgets.widgets.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.shiewk.widgets.WidgetsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class TextFieldWidgetSettingOption extends WidgetSettingOption<String> {

    private TextField textField = null;
    private final Component initialValue;
    private final Component placeholder;
    private final boolean trim;
    private final int maxLength;
    private String value = "";

    private void setValue(String value){
        this.value = value;
    }

    public class TextField extends EditBox {

        public TextField(Font textRenderer, int width, int height, Component text) {
            super(textRenderer, width, height, text);
            this.setResponder(value -> TextFieldWidgetSettingOption.this.setValue(TextFieldWidgetSettingOption.this.trim ? value.trim() : value));
        }

        @Override
        public boolean isFocused() {
            return TextFieldWidgetSettingOption.this.isFocused();
        }
    }

    public TextFieldWidgetSettingOption(String id, Component name, Component initialValue, Component placeholder, boolean trim, int maxLength) {
        super(id, name);
        this.initialValue = initialValue;
        this.placeholder = placeholder;
        this.trim = trim;
        this.maxLength = maxLength;
    }

    private void initializeTextField() {
        if (textField != null) return;
        textField = new TextField(Minecraft.getInstance().font, this.getWidth(), this.getHeight(), Component.empty());
        textField.setHint(placeholder);
        textField.setMaxLength(maxLength);
        textField.setValue(value.isEmpty() ? initialValue.getString() : value);
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
            if (textField != null) textField.setValue(state.getAsString());
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
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        initializeTextField();
        textField.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        return isFocused() && textField.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        return isFocused() && textField.mouseReleased(click);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return isFocused() && textField.charTyped(input);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        return isFocused() && textField.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
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
