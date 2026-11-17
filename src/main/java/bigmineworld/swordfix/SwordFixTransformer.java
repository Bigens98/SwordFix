package bigmineworld.swordfix;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * SwordFix - Bytecode Class Transformer
 *
 * Patches net.minecraft.client.renderer.ItemRenderer (buu) at runtime to redirect
 * getCooledAttackStrength and shouldCauseReequipAnimation inside updateEquippedItem().
 *
 * Compatible with Forge (LaunchWrapper) and CleanroomMC (Foundation TransformerDelegate).
 *
 * @author Bigens
 * @version 1.0.0
 */
public class SwordFixTransformer implements IClassTransformer {

    private static final String TARGET_DEOBF = "net.minecraft.client.renderer.ItemRenderer";
    private static final String TARGET_OBF = "buu";

    public static final String[] TARGETS = new String[] { TARGET_DEOBF, TARGET_OBF };

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (!TARGET_DEOBF.equals(transformedName) && !TARGET_OBF.equals(name)) {
            return basicClass;
        }
        return transformItemRenderer(transformedName != null ? transformedName : name, basicClass);
    }

    public static byte[] transformItemRenderer(String className, byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            int patchedCooldown = 0;
            int patchedReequip = 0;

            for (MethodNode method : classNode.methods) {
                // updateEquippedItem in MCP, func_78441_a in SRG, a in Notch
                if (("updateEquippedItem".equals(method.name) || "func_78441_a".equals(method.name) || "a".equals(method.name))
                        && "()V".equals(method.desc)) {

                    for (AbstractInsnNode insn : method.instructions.toArray()) {
                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode minsn = (MethodInsnNode) insn;
                            // Check for getCooledAttackStrength / func_184825_o / n with desc (F)F
                            if (("getCooledAttackStrength".equals(minsn.name) || "func_184825_o".equals(minsn.name) || "n".equals(minsn.name))
                                    && "(F)F".equals(minsn.desc)) {

                                method.instructions.set(insn, new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "bigmineworld/swordfix/SwordFixHooks",
                                        "getCooledAttackStrength",
                                        "(Ljava/lang/Object;F)F",
                                        false
                                ));
                                patchedCooldown++;
                            }
                        } else if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                            MethodInsnNode minsn = (MethodInsnNode) insn;
                            // Check for ForgeHooksClient.shouldCauseReequipAnimation
                            if ("shouldCauseReequipAnimation".equals(minsn.name)
                                    && minsn.owner.contains("ForgeHooksClient")) {

                                method.instructions.set(insn, new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "bigmineworld/swordfix/SwordFixHooks",
                                        "shouldCauseReequipAnimation",
                                        "(Ljava/lang/Object;Ljava/lang/Object;I)Z",
                                        false
                                ));
                                patchedReequip++;
                            }
                        }
                    }
                }
            }

            if (patchedCooldown > 0 || patchedReequip > 0) {
                SwordFixHooks.log("Successfully patched ItemRenderer (" + className + "): replaced "
                        + patchedCooldown + " cooldown call(s) and " + patchedReequip + " reequip hook(s)!");
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(classWriter);
                return classWriter.toByteArray();
            } else {
                SwordFixHooks.log("WARNING: ItemRenderer updateEquippedItem targets not found in " + className);
            }
        } catch (Throwable t) {
            SwordFixHooks.log("ERROR while patching ItemRenderer: " + t);
            t.printStackTrace();
        }
        return basicClass;
    }
}
