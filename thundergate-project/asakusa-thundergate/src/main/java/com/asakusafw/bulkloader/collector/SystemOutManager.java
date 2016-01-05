/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.bulkloader.collector;

import java.io.PrintStream;

/**
 * 標準出力の出力先を管理するクラス。
 * @author yuta.shirai
 */
public final class SystemOutManager {
    /**
     * 標準出力。
     */
    private static final PrintStream OUT = System.out;
    /**
     * 標準エラー出力。
     */
    private static final PrintStream ERR = System.err;
    /**
     * 現在の標準出力の出力先が標準出力かどうか。
     */
    private static boolean systemOut = true;
    /**
     * 現在の標準出力の出力先が標準エラー出力かどうか。
     */
    private static boolean systemErr = false;
    /**
     * 標準出力の出力先を標準エラー出力に切替える。
     */
    public static synchronized void changeSystemOutToSystemErr() {
        System.setOut(ERR);
        systemOut = false;
        systemErr = true;
    }
    /**
     * 標準出力の出力先を標準出力に切替える。
     */
    public static synchronized void changeSystemOutToSystemOut() {
        System.setOut(OUT);
        systemOut = true;
        systemErr = false;
    }
    /**
     * 現在の出力先が標準出力である場合のみ{@code true}を返す。
     * @return 現在の出力先が標準出力である場合のみ{@code true}、そうでなければ{@code false}
     * @see #isSystemErr()
     */
    public static synchronized boolean isSystemOut() {
        return systemOut;
    }
    /**
     * 現在の出力先が標準エラー出力である場合のみ{@code true}を返す。
     * @return 現在の出力先が標準エラー出力である場合のみ{@code true}、そうでなければ{@code false}
     * @see #isSystemErr()
     */
    public static synchronized boolean isSystemErr() {
        return systemErr;
    }
    /**
     * 標準出力へのストリームを返す。
     * @return 標準出力へのストリーム
     */
    public static synchronized PrintStream getOut() {
        return OUT;
    }
    /**
     * 標準エラー出力へのストリームを返す。
     * @return 標準エラー出力へのストリーム
     */
    public static synchronized PrintStream getErr() {
        return ERR;
    }

    private SystemOutManager() {
        return;
    }
}
