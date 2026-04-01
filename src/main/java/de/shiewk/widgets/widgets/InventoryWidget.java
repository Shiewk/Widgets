package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

import static net.minecraft.network.chat.Component.translatable;

public class InventoryWidget extends ResizableWidget {

    private static final Identifier VANILLA_INVENTORY = Identifier.fromNamespaceAndPath("widgets", "textures/vanilla_inventory.png");
    private static final Identifier TEXTURE_PACK_INVENTORY = Identifier.withDefaultNamespace("textures/gui/container/inventory.png");

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

        public Component display() {
            return translatable("widgets.widgets.inventory.mode." + name().toLowerCase(Locale.ROOT));
        }
    }

    public InventoryWidget(Identifier id) {
        super(id, List.of(
                new EnumWidgetSetting<>("mode", translatable("widgets.widgets.inventory.mode"), InventoryMode.class, InventoryMode.TEXTURE_PACK, InventoryMode::display),
                new ToggleWidgetSetting("show_hotbar", translatable("widgets.widgets.inventory.showHotbar"), true),
                new GradientWidgetSetting("grid_color", translatable("widgets.widgets.inventory.gridColor"), 0xff000000),
                new GradientWidgetSetting("box_color", translatable("widgets.widgets.inventory.boxColor"), 0x88505050)
        ));
        getSettings().optionById("grid_color").setShowCondition(() -> this.mode == InventoryMode.GRID);
        getSettings().optionById("box_color").setShowCondition(() -> this.mode == InventoryMode.BOXES);
        getSettings().optionById("show_hotbar").setShowCondition(() -> this.mode.canDisableHotbar);
    }

    private InventoryMode mode = InventoryMode.TEXTURE_PACK;
    private Inventory inventory;

    private GradientOptions gridColor, boxColor;
    private boolean showHotbar = false;

    @Override
    public void renderScaled(GuiGraphicsExtractor context, long measuringTimeNano, Font textRenderer, int posX, int posY) {
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

    private void drawBackground(GuiGraphicsExtractor context, long mt, int posX, int posY) {
        switch (mode){
            case VANILLA -> context.blit(RenderPipelines.GUI_TEXTURED, VANILLA_INVENTORY, posX, posY, 0, 0, 176, 91, 176, 91);
            case TEXTURE_PACK -> {
                context.enableScissor(posX, posY, posX + width(), posY + 6);
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_PACK_INVENTORY, posX, posY, 0, 0, 256, 256, 256, 256);
                context.disableScissor();
                context.enableScissor(posX, posY + 6, posX + width(), posY + height());
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_PACK_INVENTORY, posX, posY - 75, 0, 0, 256, 256, 256, 256);
                context.disableScissor();
            }
            case GRID -> {
                gridColor.drawHorizontalLine(context, mt, posX, posX+width(), posY);
                gridColor.drawHorizontalLine(context, mt, posX, posX+width(), posY + 18);
                gridColor.drawHorizontalLine(context, mt, posX, posX+width(), posY + 36);
                gridColor.drawHorizontalLine(context, mt, posX, posX+width(), posY + 54);

                if (showHotbar){
                    gridColor.drawHorizontalLine(context, mt, posX, posX+width(), posY + 58);
                    gridColor.drawHorizontalLine(context, mt, posX, posX+width(), posY + 76);
                }

                gridColor.drawVerticalLine(context, mt, posX, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18*2, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18*3, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18*4, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18*5, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18*6, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18*7, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18*8, posY, posY+height());
                gridColor.drawVerticalLine(context, mt, posX+18*9, posY, posY+height());
            }
            case BOXES -> {
                for (int ry = 0; ry < 4; ry++) {
                    if (ry == 0 && !showHotbar) continue;
                    for (int rx = 0; rx < 9; rx++) {

                        int itemY = ry == 0 ? posY + 58 : posY + (ry-1) * 18;
                        int itemX = posX + rx * 18;

                        boxColor.fillHorizontal(context, mt, itemX, itemY, itemX + 16, itemY + 16);
                    }
                }
            }
        }
    }

    private void drawItems(GuiGraphicsExtractor context, Font textRenderer, int posX, int posY) {
        for (int ry = 0; ry < 4; ry++) {
            if (ry == 0 && !showHotbar) continue;
            for (int rx = 0; rx < 9; rx++) {

                int slot = ry * 9 + rx;
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty()) continue;

                int itemY = ry == 0 ? posY + 58 : posY + (ry-1) * 18;
                int itemX = posX + rx * 18;

                context.item(stack, itemX, itemY);
                context.itemDecorations(textRenderer, stack, itemX, itemY);
            }
        }
    }

    @Override
    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            inventory = player.getInventory();
        }
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.inventory");
    }

    @Override
    public Component getDescription() {
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
        this.mode = (InventoryMode) settings.optionById("mode").getValue();

        this.gridColor = (GradientOptions) settings.optionById("grid_color").getValue();
        this.boxColor = (GradientOptions) settings.optionById("box_color").getValue();

        this.showHotbar = (boolean) settings.optionById("show_hotbar").getValue() || !mode.canDisableHotbar;
    }
}
