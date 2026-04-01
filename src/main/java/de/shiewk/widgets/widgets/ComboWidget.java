package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

public class ComboWidget extends BasicTextWidget implements AttackEntityCallback {

    public ComboWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("show_label", translatable("widgets.widgets.common.showLabel"), true),
                new IntSliderWidgetSetting("display_threshold", translatable("widgets.widgets.combo.displayThreshold"), 0, 0, 5)
        ));
        if (INSTANCE != null) throw new IllegalStateException("Instance already initialized");
        INSTANCE = this;
    }

    private static ComboWidget INSTANCE;

    private boolean showLabel = true;
    private int targetEntityId = 0;
    private boolean hitPlanned = false;
    private int combo = 0;
    private int ticksSinceUpdate = 0;
    private int displayThreshold = 0;

    @Override
    public @NonNull InteractionResult interact(@NonNull Player playerEntity, @NonNull Level world, @NonNull InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        clientHitEntity(entity.getId());
        return InteractionResult.PASS;
    }

    private void clientHitEntity(int targetEntityId) {
        int oldTarget = this.targetEntityId;
        if (oldTarget != targetEntityId){
            resetCombo();
        }
        this.targetEntityId = targetEntityId;
        hitPlanned = true;
    }

    public static void entityTakeDamage(int clientEntityId, int damagedEntityId){
        INSTANCE.entityTakeDamage0(clientEntityId, damagedEntityId);
    }

    private void entityTakeDamage0(int clientEntityId, int damagedEntityId) {
        if (damagedEntityId == clientEntityId){
            resetCombo();
        } else if (damagedEntityId == this.targetEntityId && this.hitPlanned){
            hitPlanned = false;
            combo++;
            ticksSinceUpdate = 0;
            updateComboText();
        }
    }

    @Override
    public void tickWidget() {
        if (ticksSinceUpdate > 40){
            resetCombo();
            hitPlanned = false;
        }
        ticksSinceUpdate++;
    }

    private void resetCombo() {
        combo = 0;
        ticksSinceUpdate = 0;
        updateComboText();
    }

    private void updateComboText() {
        shouldRender = combo >= displayThreshold;
        if (showLabel){
            formatAndSetRenderText(translatable("widgets.widgets.combo.combo", combo));
        } else {
            formatAndSetRenderText(""+combo);
        }
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.combo");
    }

    @Override
    public Component getDescription() {
        return translatable("widgets.widgets.combo.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.displayThreshold = (int) settings.optionById("display_threshold").getValue();
        this.showLabel = (boolean) settings.optionById("show_label").getValue();
        updateComboText();
    }
}
