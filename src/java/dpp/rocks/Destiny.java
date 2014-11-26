package dpp.rocks;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.Enumeration;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.lang.instrument.IllegalClassFormatException;

public class Destiny {
    public static class Tuple<X, Y> {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return "Tuple(bytes, "+y+")";
        }
    }

    public static void premain(String args, Instrumentation inst) {
        inst.addTransformer(new DynTransformer());
    }

    public static void main(String[] argv) throws Exception {
        rewriteDynamicClassloader();
        Enumeration<URL> resources = Destiny.class.getClassLoader()
            .getResources("META-INF/MANIFEST.MF");

        boolean run = true;
        String pkg = null;
        String func = null;

        while (run && resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            Manifest manifest = new Manifest();

            manifest.read(resource.openStream());

            // check that this is your manifest and do what you need or get the next one

            java.util.jar.Attributes attributes =
                manifest.getMainAttributes();

            String mainClass = attributes.getValue("Main-Class");
            String cljPkg = attributes.getValue("Implementation-Package");
            String cljFunc = attributes.getValue("Implementation-Function");

            // we've found our manifest file
            if ("dpp.rocks.Destiny".equals(mainClass) &&
                cljPkg != null &&
                cljFunc != null) {
                run = false;
                pkg = cljPkg;
                func = cljFunc;
            }

            resource = null;
            manifest = null;
            attributes = null;
            mainClass = null;
            cljPkg = null;
            cljFunc = null;
        }

        if (pkg != null && func != null) {
            IFn require = Clojure.var("clojure.core", "require");
            require.invoke(Clojure.read(pkg));

            IFn toRun = Clojure.var(pkg, func);
            toRun.invoke(argv);
        } else {
            throw new Exception("The properties 'Implementation-Package' "+
                                "and 'Implementation-Function' "+
                                "must be set in the manifest.mf file");
        }
    }

    public static interface TestClass {
        public boolean test(String name, Object form);
    }

    public static TestClass addTest = new TestClass() {
            public boolean test(String name, Object form) {
                return true;
            }
        };

    public static ConcurrentHashMap<String, Tuple<byte[], Object>> classInfo =
        new ConcurrentHashMap<String, Tuple<byte[], Object> >();

    public static boolean addInfo = true;

    public static void addClassInfo(String name, byte[] bytes, Object form) {
        if (addInfo && addTest.test(name, form)) {
            classInfo.put(name, new Tuple(bytes, form));
        }
    }

    private static boolean done = false;

    /**
     * REwrite the bytecode for DynamicClassLoader to register each created class
     * with this class
     */
    public static synchronized void rewriteDynamicClassloader() throws Exception {

        if (!done) {
            done = true;
            ClassPool cp = ClassPool.getDefault();
            CtClass ct = cp.get("clojure.lang.DynamicClassLoader");
            CtMethod m = ct.getDeclaredMethod("defineClass");
            m.insertBefore("{dpp.rocks.Destiny.addClassInfo(name, bytes, srcForm);}");
            Class c = ct.toClass();
        }
    }

    public static class DynTransformer implements ClassFileTransformer {
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (!className.equals("clojure/lang/DynamicClassLoader"))
                return null;

            System.out.println("Working it...");

            try {
                ClassPool cp = ClassPool.getDefault();
                CtClass ct = cp.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
                CtMethod m = ct.getDeclaredMethod("defineClass");
                m.insertBefore("{dpp.rocks.Destiny.addClassInfo(name, bytes, srcForm);}");
                System.err.println("rewrote it!!");
                return ct.toBytecode();
            } catch (Exception e) {
                throw new RuntimeException("Could not rewrite the classloader",e);
            }

        }
    }


}
