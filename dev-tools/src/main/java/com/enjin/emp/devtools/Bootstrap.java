package com.enjin.emp.devtools;

public class Bootstrap {

    public static void main(String... args) throws Exception {
        JavaVersion javaVersion = JavaVersion.getCurrentVersion();

        if (javaVersion == JavaVersion.UNKNOWN) {
            System.err.println("*** WARNING *** Unsupported Java detected (" + System.getProperty("java.class.version")
                                       + "). DevTools has only been tested up to Java 11. Use of development Java version is not supported");
            System.err.println("*** WARNING *** You may use java -version to double check your Java version.");
        }

        Application.main(args);
    }

}
