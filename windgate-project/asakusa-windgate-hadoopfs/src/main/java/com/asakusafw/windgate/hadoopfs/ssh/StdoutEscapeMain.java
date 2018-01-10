/**
 * Copyright 2011-2018 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.windgate.hadoopfs.ssh;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * Invokes a main method of the class specified in first program argument by its FQN.
 * This class initializes {@link StdioHelper} before escape stdout,
 * so that clients can obtain the original stdout using {@link StdioHelper#getOriginalStdout()}.
 * @since 0.4.0
 * @see StdioHelper
 */
public final class StdoutEscapeMain {

    static {
        StdioHelper.load();
    }

    private StdoutEscapeMain() {
        return;
    }

    /**
     * Program entry.
     * @param args {@code main-class-name}, {@code program-arguments...}
     * @throws Throwable if failed to execute program
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Usage: java {0} <main-class> [<arguments>]",
                    StdoutEscapeMain.class.getName()));
        }
        String mainClassName = args[0];
        String[] mainArgs = new String[args.length - 1];
        System.arraycopy(args, 1, mainArgs, 0, mainArgs.length);
        System.setOut(System.err);
        try {
            launch(mainClassName, mainArgs);
        } finally {
            StdioHelper.reset();
        }
    }

    private static void launch(String className, String[] args) throws Throwable {
        Class<?> mainClass = Class.forName(className);
        Method method = mainClass.getMethod("main", String[].class);
        try {
            method.invoke(null, new Object[] { args });
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
