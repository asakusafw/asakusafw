/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * ツールを起動するプログラムエントリ。
 */
public final class ToolLauncher {

    /**
     * 処理の成功が確定したことを表す結果。
     */
    public static final int JOB_SUCCEEDED = 0;

    /**
     * 処理の失敗が確定したことを表す結果。
     */
    public static final int JOB_FAILED = 1;

    /**
     * 起動自体に失敗したことを表す結果。
     */
    public static final int LAUNCH_ERROR = -2;

    /**
     * クライアントがエラーを返したことを表す結果。
     */
    public static final int CLIENT_ERROR = -1;

    /**
     * プログラムを実行する。
     * @param args {@code Tool-class-name [optional-arguments]}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void main(String...args) {
        if (args == null) {
            throw new IllegalArgumentException("args must not be null"); //$NON-NLS-1$
        }
        LinkedList<String> arguments = new LinkedList<String>();
        Collections.addAll(arguments, args);
        int result = main(arguments);
        System.exit(result);
    }

    private static int main(LinkedList<String> args) {
        assert args != null;
        if (args.isEmpty()) {
            throw new IllegalArgumentException(
                    "The first argument of StageLauncher must be a class name of Tool");
        }
        String main = args.removeFirst();
        Tool tool;
        try {
            URL[] libraries = parseLibraries(args);
            ClassLoader loader = createLoader(libraries);
            tool = newTool(main, loader);
        } catch (Exception e) {
            e.printStackTrace();
            return LAUNCH_ERROR;
        }
        try {
            return ToolRunner.run(tool, args.toArray(new String[args.size()]));
        } catch (Exception e) {
            e.printStackTrace();
            return CLIENT_ERROR;
        }
    }

    private static URL[] parseLibraries(LinkedList<String> args) throws IOException {
        assert args != null;
        GenericOptionsParser options =
            new GenericOptionsParser(args.toArray(new String[args.size()]));
        Configuration conf = options.getConfiguration();
        return GenericOptionsParser.getLibJars(conf);
    }

    private static ClassLoader createLoader(URL[] libraries) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ToolLauncher.class.getClassLoader();
        }
        if (libraries == null || libraries.length == 0) {
            return loader;
        }
        return new URLClassLoader(libraries, loader);
    }

    private static Tool newTool(String className, ClassLoader loader) {
        assert className != null;
        assert loader != null;
        Class<?> aClass;
        try {
            aClass = Class.forName(
                    className,
                    false,
                    loader);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to load a Tool class ({0})",
                    className),
                    e);
        }
        if (Tool.class.isAssignableFrom(aClass) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The first argument of ToolLauncher must be a subclass of Tool ({0})",
                    className));
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            return aClass.asSubclass(Tool.class).newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to instantiate a Tool class ({0})",
                    aClass));
        } finally {
            Thread.currentThread().setContextClassLoader(context);
        }
    }

    private ToolLauncher() {
        return;
    }
}
