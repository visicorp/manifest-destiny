package dpp.rocks;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.Enumeration;

public class Destiny {
    public static void main(String[] argv) throws Exception {
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
}
