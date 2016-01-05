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
package com.asakusafw.bulkloader.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.xml.DOMConfigurator;

/**
 * ログ出力関連のパラメータを初期化するクラス。
 * @author yuta.shirai
 */
public final class LogInitializer {

    /**
     * 初期化完了フラグ。
     */
    private static boolean isInitialized = false;

    /**
     * 本クラスはインスタンス化せず、staticメソッドを直接呼び出してください。
     */
    private LogInitializer() {
        return;
    }

    /**
     * ログ出力関連のパラメータを初期化します。
     * <p>
     * log4j.xmlファイルとログメッセージファイルを読み込みます。
     * 初期化完了後、初期化完了フラグを立てます。
     * </p>
     * @param logConfFilePath log4j.xmlのフルパス
     * @throws IOException ログの初期化に失敗した場合
     */
    public static void execute(String logConfFilePath) throws IOException {
        loadFile(logConfFilePath);
        isInitialized = true;
    }

    /**
     * log4j.xmlの読み込みを行います。
     *
     * @param filePath log4j.xmlのフルパス
     * @throws FileNotFoundException ファイルがlog4j.xmlではない、またはファイルがパス上に存在しない場合
     */
    static void loadFile(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        }
        DOMConfigurator.configure(filePath);
    }

    /**
     * ログ出力関連のパラメータが初期化されているか判断します。
     * @return 初期化が完了していればtrue, 未完了であればfalseを返します。
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}
