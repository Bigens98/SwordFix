package bigmineworld.swordfix.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * ItemInHandRendererMixin - Minecraft 1.20.1 Forge
 *
 * Prevents weapons and tools from dropping down off-screen during attack cooldown.
 *
 * @author Bigens
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Redirect(
        method = {"tick()V", "m_109311_()V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;m_36403_(F)F"
        ),
        require = 0
    )
    private float swordFix_ignoreAttackCooldown(LocalPlayer player, float adjustTicks) {
        // Return 1.0F inside ItemInHandRenderer.tick() so the weapon remains raised at full height
        return 1.0F;
    }

    @Redirect(
        method = {"tick()V", "m_109311_()V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/client/ForgeHooksClient;shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z"
        ),
        require = 0,
        remap = false
    )
    private boolean swordFix_ignoreDurabilityReequip(ItemStack from, ItemStack to, int slot) {
        // If holding the same item type in the same hand slot, ignore durability/damage updates
        if (!from.m_41619_() && !to.m_41619_()) {
            if (from.m_41720_() == to.m_41720_() && slot != -1) {
                return false;
            }
        }
        return ForgeHooksClient.shouldCauseReequipAnimation(from, to, slot);
    }
}
