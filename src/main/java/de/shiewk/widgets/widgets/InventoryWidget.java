package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.RGBAColorWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static net.minecraft.text.Text.translatable;

public class InventoryWidget extends ResizableWidget {

    private static final Identifier VANILLA_INVENTORY = Identifier.of("widgets", "textures/vanilla_inventory.png");
    private static final Identifier TEXTURE_PACK_INVENTORY = Identifier.ofVanilla("textures/gui/container/inventory.png");

    public enum InventoryMode {
        VANILLA,
        TEXTURE_PACK,
        TRANSPARENT(true),
        GRID(true),
        BOXES(true);

        public final boolean canDisableHotbar;

        InventoryMode(){
            this(false);
        }

        InventoryMode(boolean canDisableHotbar){
            this.canDisableHotbar = canDisableHotbar;
        }

        public Text display() {
            return translatable("widgets.widgets.inventory.mode." + name().toLowerCase());
        }
    }

    public InventoryWidget(Identifier id) {
        super(id, List.of(
                new EnumWidgetSetting<>("mode", translatable("widgets.widgets.inventory.mode"), InventoryMode.class, InventoryMode.TEXTURE_PACK, InventoryMode::display),
                new ToggleWidgetSetting("show_hotbar", translatable("widgets.widgets.inventory.showHotbar"), true),
                new ToggleWidgetSetting("rainbow_grid", translatable("widgets.widgets.inventory.rainbowGrid"), false),
                new IntSliderWidgetSetting("grid_rainbow_speed", translatable("widgets.widgets.common.rainbow.speed"), 1, 3, 10),
                new RGBAColorWidgetSetting("grid_color", translatable("widgets.widgets.inventory.gridColor"), 0, 0, 0, 255),
                new ToggleWidgetSetting("rainbow_boxes", translatable("widgets.widgets.inventory.rainbowBoxes"), false),
                new IntSliderWidgetSetting("box_rainbow_speed", translatable("widgets.widgets.common.rainbow.speed"), 1, 3, 10),
                new RGBAColorWidgetSetting("box_color", translatable("widgets.widgets.inventory.boxColor"), 80, 80, 80, 128)
        ));
        getSettings().optionById("rainbow_grid").setShowCondition(() -> this.mode == InventoryMode.GRID);
        getSettings().optionById("grid_rainbow_speed").setShowCondition(() -> this.mode == InventoryMode.GRID && this.rainbowGrid);
        getSettings().optionById("grid_color").setShowCondition(() -> this.mode == InventoryMode.GRID && !this.rainbowGrid);

        getSettings().optionById("rainbow_boxes").setShowCondition(() -> this.mode == InventoryMode.BOXES);
        getSettings().optionById("box_rainbow_speed").setShowCondition(() -> this.mode == InventoryMode.BOXES && this.rainbowBoxes);
        getSettings().optionById("box_color").setShowCondition(() -> this.mode == InventoryMode.BOXES && !this.rainbowBoxes);

        getSettings().optionById("show_hotbar").setShowCondition(() -> this.mode.canDisableHotbar);
    }

    private InventoryMode mode = InventoryMode.TEXTURE_PACK;
    private PlayerInventory inventory;

    private boolean rainbowGrid = false;
    private int gridColor = 0xff000000;
    private int gridRainbowSpeed = 3;

    private boolean rainbowBoxes = false;
    private int boxColor = 0xff000000;
    private int boxRainbowSpeed = 3;
    private boolean showHotbar = false;

    @Override
    public void renderScaled(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY) {
        drawBackground(context, measuringTimeNano, posX, posY);
        if (inventory != null){
            drawItems(context, textRenderer, switch (mode){
                case VANILLA, TEXTURE_PACK -> posX+8;
                case GRID -> posX+1;
                case TRANSPARENT, BOXES -> posX;
            }, switch (mode){
                case VANILLA, TEXTURE_PACK -> posY+9;
                case GRID -> posY+1;
                case TRANSPARENT, BOXES -> posY;
            });
        }
    }

    private void drawBackground(DrawContext context, long mt, int posX, int posY) {
        switch (mode){
            case VANILLA -> context.drawTexture(VANILLA_INVENTORY, posX, posY, 0, 0, 176, 91, 176, 91);
            case TEXTURE_PACK -> {
                context.enableScissor(posX, posY, posX + width(), posY + 6);
                context.drawTexture(TEXTURE_PACK_INVENTORY, posX, posY, 0, 0, 256, 256, 256, 256);
                context.disableScissor();
                context.enableScissor(posX, posY + 6, posX + width(), posY + height());
                context.drawTexture(TEXTURE_PACK_INVENTORY, posX, posY - 75, 0, 0, 256, 256, 256, 256);
                context.disableScissor();
            }
            case GRID -> {
                int gridColor = rainbowGrid ? BasicTextWidget.rainbowColor(mt, gridRainbowSpeed) : this.gridColor;
                context.drawHorizontalLine(posX, posX+width(), posY, gridColor);
                context.drawHorizontalLine(posX, posX+width(), posY + 18, gridColor);
                context.drawHorizontalLine(posX, posX+width(), posY + 36, gridColor);
                context.drawHorizontalLine(posX, posX+width(), posY + 54, gridColor);

                if (showHotbar){
                    context.drawHorizontalLine(posX, posX+width(), posY + 58, gridColor);
                    context.drawHorizontalLine(posX, posX+width(), posY + 76, gridColor);
                }

                context.drawVerticalLine(posX, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18*2, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18*3, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18*4, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18*5, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18*6, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18*7, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18*8, posY, posY+height(), gridColor);
                context.drawVerticalLine(posX+18*9, posY, posY+height(), gridColor);
            }
            case BOXES -> {
                int boxColor = rainbowBoxes ? ((BasicTextWidget.rainbowColor(mt, boxRainbowSpeed) & 0xffffff) | (this.boxColor & 0xff000000)) : this.boxColor;
                for (int ry = 0; ry < 4; ry++) {
                    if (ry == 0 && !showHotbar) continue;
                    for (int rx = 0; rx < 9; rx++) {

                        int itemY = ry == 0 ? posY + 58 : posY + (ry-1) * 18;
                        int itemX = posX + rx * 18;

                        context.fill(itemX, itemY, itemX + 16, itemY + 16, boxColor);
                    }
                }
            }
        }
    }

    private void drawItems(DrawContext context, TextRenderer textRenderer, int posX, int posY) {
        for (int ry = 0; ry < 4; ry++) {
            if (ry == 0 && !showHotbar) continue;
            for (int rx = 0; rx < 9; rx++) {

                int slot = ry * 9 + rx;
                ItemStack stack = inventory.getStack(slot);
                if (stack.isEmpty()) continue;

                int itemY = ry == 0 ? posY + 58 : posY + (ry-1) * 18;
                int itemX = posX + rx * 18;

                context.drawItem(stack, itemX, itemY);
                context.drawItemInSlot(textRenderer, stack, itemX, itemY);
            }
        }
    }

    @Override
    public void tick() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            PlayerInventory clientPlayerEntityInventory = player.getInventory();
            if (clientPlayerEntityInventory != null) inventory = clientPlayerEntityInventory;
        }
    }

    @Override
    public Text getName() {
        return translatable("widgets.widgets.inventory");
    }

    @Override
    public Text getDescription() {
        return translatable("widgets.widgets.inventory.description");
    }

    @Override
    public int width() {
        return switch (mode){
            case VANILLA, TEXTURE_PACK -> 176;
            case TRANSPARENT, BOXES -> 160;
            case GRID -> 162;
        };
    }

    @Override
    public int height() {
        return switch (mode){
            case VANILLA, TEXTURE_PACK -> 91;
            case TRANSPARENT, BOXES -> showHotbar ? 74 : 53;
            case GRID -> showHotbar ? 76 : 55;
        };
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.mode = ((InventoryMode) ((EnumWidgetSetting<?>) settings.optionById("mode")).getValue());
        this.rainbowGrid = ((ToggleWidgetSetting) settings.optionById("rainbow_grid")).getValue();
        this.gridRainbowSpeed = ((IntSliderWidgetSetting) settings.optionById("grid_rainbow_speed")).getValue();
        this.gridColor = ((RGBAColorWidgetSetting) settings.optionById("grid_color")).getColor();

        this.rainbowBoxes = ((ToggleWidgetSetting) settings.optionById("rainbow_boxes")).getValue();
        this.boxRainbowSpeed = ((IntSliderWidgetSetting) settings.optionById("box_rainbow_speed")).getValue();
        this.boxColor = ((RGBAColorWidgetSetting) settings.optionById("box_color")).getColor();

        this.showHotbar = ((ToggleWidgetSetting) settings.optionById("show_hotbar")).getValue() || !mode.canDisableHotbar;
    }
}
