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
package com.asakusafw.cleaner.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.asakusafw.cleaner.common.Constants;


/**
 * ログメッセージファイルの読み込むためのクラス。
 * @author yuta.shirai
 *
 */
final class LogMessageLoader {
    /** 重要度を保持するプロパティキーのメッセージID以降の文字列。 */
    private static final String LEVEL_KEY_END = ".level";
    /** メッセージを保持するプロパティキーのメッセージID以降の文字列。 */
    private static final String TEMPLATE_KEY_END = ".message";
    /** メッセージ引数の個数を保持するプロパティキーのメッセージID以降の文字列。 */
    private static final String SIZE_KEY_END = ".size";

    /**
     * 本クラスはインスタンス化せず、staticメソッドを直接呼び出してください。
     */
    private LogMessageLoader() {
    	return;
    }

    /**
     * クラスパス上からログメッセージプロパティを読み込む。
     * ログメッセージ管理オブジェクトに重要度、メッセージテンプレートを登録します。
     * <p>
     * 指定されたディレクトリ配下に.propertiesファイルが存在しなければ
     * そのディレクトリ配下のファイル読み込みは行いません。
     * </p>
     * @param manager ログメッセージ管理オブジェクト
     * @throws IOException ログメッセージプロパティが存在しない場合
     */
    static void loadFile(LogMessageManager manager) throws IOException {
        // ログメッセージプロパティを読み込む
        InputStream in = null;
        Properties props = new Properties();
        try {
            in = LogMessageLoader.class.getClassLoader().getResourceAsStream(Constants.LOG_MESSAGE_FILE);
            props.load(in);
            Enumeration<?> keys = props.propertyNames();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                if (key.endsWith(LEVEL_KEY_END)) {
                    String messageId = LogMessageLoader.getMessageId(key, LEVEL_KEY_END);
                    manager.putLevel(messageId, props.getProperty(key));
                } else if (key.endsWith(TEMPLATE_KEY_END)) {
                    String messageId = LogMessageLoader.getMessageId(key, TEMPLATE_KEY_END);
                    manager.putTemplate(messageId, props.getProperty(key));
                } else if (key.endsWith(SIZE_KEY_END)) {
                    String messageId = LogMessageLoader.getMessageId(key, SIZE_KEY_END);
                    String sizeStr = props.getProperty(key);
                    if (NumberUtils.isNumber(sizeStr)) {
                        manager.putSize(messageId, Integer.valueOf(sizeStr));
                    }
                }
            }
        } catch (IOException ex) {
            throw new IOException("クラスパス上にログメッセージプロパティが存在しませんでした。ログメッセージプロパティファイル名：" + Constants.LOG_MESSAGE_FILE, ex);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * プロパティのキーからメッセージIDを取得します。
     * @param key プロパティキー
     * @param endStr プロパティキーのメッセージIDの文字列
     * @return メッセージID
     */
    private static String getMessageId(String key, String endStr) {
        String[] splits = key.split("\\.");
        return splits[0];
    }
}
