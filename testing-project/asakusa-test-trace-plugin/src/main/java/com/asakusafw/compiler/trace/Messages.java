package com.asakusafw.compiler.trace;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

final class Messages {

    private static final String BUNDLE_NAME = "com.asakusafw.compiler.trace.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
        return;
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
