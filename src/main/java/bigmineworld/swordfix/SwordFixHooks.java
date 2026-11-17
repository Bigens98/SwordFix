package bigmineworld.swordfix;

import java.lang.reflect.Method;

/**
 * SwordFix - Core Hook Handler
 *
 * Intercepts attack cooldown queries and re-equip triggers specifically inside
 * ItemRenderer.updateEquippedItem() so held weapons and tools never drop down.
 *
 * @author Bigens
 * @version 1.0.0
 */
public class SwordFixHooks {

    private static Method forgeReequipMethod = null;
    private static Method getItemMethod = null;
    private static Method isEmptyMethod = null;

    /**
     * Intercepts EntityPlayerSP.getCooledAttackStrength() inside ItemRenderer.updateEquippedItem().
     *
     * Returning 1.0F forces the target equip progress for the held weapon to always remain 1.0F
     * during attack cooldown, completely eliminating the annoying visual weapon drop.
     *
     * Note: Server-side attack damage calculations and GUI cooldown indicators remain 100% vanilla.
     *
     * @param player      The player instance
     * @param adjustTicks Partial tick adjustment
     * @return Always 1.0F inside ItemRenderer
     */
    public static float getCooledAttackStrength(Object player, float adjustTicks) {
        return 1.0f;
    }

    /**
     * Intercepts ForgeHooksClient.shouldCauseReequipAnimation().
     *
     * Prevents the weapon from twitching or re-equipping when its durability/damage updates upon hitting mobs,
     * while still preserving smooth equip animations when actually switching hotbar slots or item types.
     *
     * @param from Previous ItemStack
     * @param to   Current ItemStack
     * @param slot Active inventory slot index
     * @return false if the same item is held in the same slot; otherwise delegates to Forge
     */
    public static boolean shouldCauseReequipAnimation(Object from, Object to, int slot) {
        if (from != null && to != null) {
            try {
                if (isEmptyMethod == null) {
                    try {
                        isEmptyMethod = from.getClass().getMethod("func_190926_b");
                    } catch (NoSuchMethodException e) {
                        try {
                            isEmptyMethod = from.getClass().getMethod("isEmpty");
                        } catch (NoSuchMethodException e2) {
                            isEmptyMethod = from.getClass().getMethod("b"); // Notch obf name
                        }
                    }
                }
                boolean fromEmpty = (Boolean) isEmptyMethod.invoke(from);
                boolean toEmpty = (Boolean) isEmptyMethod.invoke(to);

                if (!fromEmpty && !toEmpty) {
                    if (getItemMethod == null) {
                        try {
                            getItemMethod = from.getClass().getMethod("func_77973_b");
                        } catch (NoSuchMethodException e) {
                            try {
                                getItemMethod = from.getClass().getMethod("getItem");
                            } catch (NoSuchMethodException e2) {
                                getItemMethod = from.getClass().getMethod("c"); // Notch obf name
                            }
                        }
                    }
                    Object fromItem = getItemMethod.invoke(from);
                    Object toItem = getItemMethod.invoke(to);

                    // Same item in the same slot (e.g. durability change on hit): do not drop hand!
                    if (fromItem != null && fromItem == toItem && slot != -1) {
                        return false;
                    }
                }
            } catch (Throwable ignored) {
            }

            // Fallback to ForgeHooksClient if switching slots or items
            try {
                if (forgeReequipMethod == null) {
                    Class<?> forgeHooksClass = Class.forName("net.minecraftforge.client.ForgeHooksClient");
                    for (Method m : forgeHooksClass.getMethods()) {
                        if ("shouldCauseReequipAnimation".equals(m.getName())) {
                            forgeReequipMethod = m;
                            break;
                        }
                    }
                }
                if (forgeReequipMethod != null) {
                    return (Boolean) forgeReequipMethod.invoke(null, from, to, slot);
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public static void log(String msg) {
        System.out.println("[SwordFix] " + msg);
    }
}
