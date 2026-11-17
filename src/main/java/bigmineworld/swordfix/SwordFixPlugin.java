package bigmineworld.swordfix;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

/**
 * SwordFix - FML CorePlugin & Foundation Bridge
 *
 * Automatically detects whether the game is running on standard Forge (using LaunchWrapper)
 * or CleanroomMC (using Foundation TransformerDelegate) and registers the transformer accordingly.
 *
 * @author Bigens
 * @version 1.0.0
 */
@IFMLLoadingPlugin.Name("SwordFix")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
public class SwordFixPlugin implements IFMLLoadingPlugin {

    private static final String[] TRANSFORMER_CLASSES = new String[] {
        "bigmineworld.swordfix.SwordFixTransformer"
    };

    @Override
    public String[] getASMTransformerClass() {
        return TRANSFORMER_CLASSES;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {
        try {
            Launch.classLoader.addTransformerExclusion("bigmineworld.swordfix.");
        } catch (Throwable t) {
            System.err.println("[SwordFix] Could not add transformer exclusion: " + t);
        }

        // Support CleanroomMC Foundation TransformerDelegate
        try {
            Class<?> delegateClass = Class.forName("top.outlands.foundation.TransformerDelegate");
            Class<?> explicitClass = Class.forName("top.outlands.foundation.IExplicitTransformer");
            Method registerMethod = delegateClass.getMethod("registerExplicitTransformer", explicitClass, String[].class);

            for (final String target : SwordFixTransformer.TARGETS) {
                Object proxy = Proxy.newProxyInstance(
                    SwordFixPlugin.class.getClassLoader(),
                    new Class<?>[] { explicitClass },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            String mname = method.getName();
                            if ("transform".equals(mname) && args != null && args.length == 1) {
                                return SwordFixTransformer.transformItemRenderer(target, (byte[]) args[0]);
                            }
                            if ("getPriority".equals(mname)) {
                                return 0;
                            }
                            if ("hashCode".equals(mname)) {
                                return System.identityHashCode(proxy);
                            }
                            if ("equals".equals(mname)) {
                                return args != null && proxy == args[0];
                            }
                            if ("toString".equals(mname)) {
                                return "SwordFix(" + target + ")";
                            }
                            return null;
                        }
                    }
                );
                registerMethod.invoke(null, proxy, new String[] { target });
            }
            System.out.println("[SwordFix] Registered via Foundation TransformerDelegate (CleanroomMC)");
        } catch (ClassNotFoundException e) {
            System.out.println("[SwordFix] Foundation not detected, using standard Forge IClassTransformer");
        } catch (Throwable t) {
            System.err.println("[SwordFix] Error registering via Foundation: " + t);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
