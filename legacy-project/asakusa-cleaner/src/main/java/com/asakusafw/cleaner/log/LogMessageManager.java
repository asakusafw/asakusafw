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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

/**
 * ログメッセージを管理するためのクラス。
 * @author yuta.shirai
 *
 */
public class LogMessageManager {

    /**
     * 指定されたメッセージIDがメッセージプロパティファイルに
     * 存在しない場合に返すメッセージです。
     */
    private static final String MESSAGE_ID_NOT_FOUND =
        "不正なメッセージIDが指定されました。メッセージID={0}、メッセージ引数={1}";

    /**
     * 指定されたメッセージ引数がテンプレートのインデックスより
     * 多い/少ない場合に表示するメッセージです。
     */
    private static final String ILLEGAL_SIZE =
        "{0, date} {0, time} メッセージ引数の数がテンプレートと一致しません。メッセージID={1}、メッセージ引数={2}";

    /**
     * メッセージIDと重要度のマップ。
     */
    private Map<String, Level> levelMap = new HashMap<String, Level>();

    /**
     * メッセージIDとメッセージテンプレートのマップ。
     */
    private Map<String, String> templateMap = new HashMap<String, String>();

    /**
     * メッセージIDとメッセージ引数のインデックスのマップ。
     */
    private Map<String, Integer> sizeMap = new HashMap<String, Integer>();


    /**
     * ログメッセージ管理クラス。
     */
    private static LogMessageManager instance = new LogMessageManager();

    /**
     * インスタンスを取得する際は、getInstance()メソッドを呼び出してください。
     * <p>
     * 本来であればSingletonパターンクラスのコンストラクタはprivateにするところですが、
     * 単体テスト時に専用のインスタンスを生成できるようにprotectedにしています。
     * </p>
     */
    protected LogMessageManager() {
    	return;
    }

    /**
     * ログメッセージ管理クラスのインスタンスを取得します。
     * @return ログメッセージ管理クラスのインスタンス
     */
    public static LogMessageManager getInstance() {
        return LogMessageManager.instance;
    }

    /**
     * メッセージIDをキーにして、重要度を追加します。
     * @param messageId メッセージID
     * @param level 重要度
     */
    public void putLevel(String messageId, String level) {
        levelMap.put(messageId, Level.toLevel(level));
    }

    /**
     * メッセージIDをキーにして、メッセージテンプレートを追加します。
     * @param messageId メッセージID
     * @param templates テンプレート
     */
    public void putTemplate(String messageId, String templates) {
        templateMap.put(messageId, templates);
    }

    /**
     * メッセージIDをキーにして、メッセージ引数を追加します。
     * @param messageId メッセージID
     * @param index メッセージ引数の個数
     */
    public void putSize(String messageId, Integer index) {
        sizeMap.put(messageId, index);
    }

    /**
     * メッセージIDと引数からログメッセージを構築します。
     * <p>
     * 指定されたメッセージIDがメッセージプロパティに存在しない場合には、
     * 下記のメッセージを返します。
     * 「不正なメッセージIDが指定されました。メッセージID=xxx、メッセージ引数=xxx, xxx, xxx」
     * </p>
     * <p>
     * メッセージの引数とメッセージプロパティに記述された引数の個数が異なる場合には
     * 標準出力にエラーメッセージを出力します。
     * </p>
     * @param messageId メッセージID
     * @param messageArgs メッセージ引数
     * @return ログメッセージ
     */
    public String createLogMessage(String messageId, Object... messageArgs) {

        Object[] messageArgsConverted = toStringMessageArgs(messageArgs);

        String templateStr = templateMap.get(messageId);
        if (templateStr == null) {
            String message = MessageFormat.format(MESSAGE_ID_NOT_FOUND,
                    messageId, StringUtils.join(messageArgsConverted, ", "));
            return message;
        }
        Integer index = sizeMap.get(messageId);
        if (index != null) {
            if (messageArgsConverted.length != index.intValue()) {
                String message = MessageFormat.format(ILLEGAL_SIZE,
                        new Date(), messageId, StringUtils.join(messageArgsConverted, ", "));
                System.err.println(message);
            }
        }

        return MessageFormat.format(templateStr, messageArgsConverted);
    }

    /**
     * ログレベルを取得します。
     * <p>
     * 指定されたメッセージIDがメッセージプロパティに存在しない場合には、
     * ERRORを返します。
     * </p>
     * @param messageId メッセージID
     * @return ログレベル
     */
    public Level getLogLevel(String messageId) {
        Level level = levelMap.get(messageId);
        if (level == null) {
            return Level.ERROR;
        }

        return level;
    }

    /**
     * メッセージ引数を文字列化します。
     *
     * @param messageArgs メッセージ引数
     * @return 文字列化したメッセージ引数
     */
    private Object[] toStringMessageArgs(Object[] messageArgs) {
        if (messageArgs == null) {
            return messageArgs;
        }
        Object[] messageArgsConverted = new Object[messageArgs.length];
        for (int i = 0; i < messageArgs.length; i++) {
            Object obj = messageArgs[i];
            if (obj == null) {
                messageArgsConverted[i] = null;
            } else if (obj.getClass().isArray()) {
                messageArgsConverted[i] = ArrayUtils.toString(obj);
            } else if (obj instanceof Long
                    || obj instanceof Integer
                    || obj instanceof BigDecimal) {
                messageArgsConverted[i] = ObjectUtils.toString(obj, "null");
            } else {
                messageArgsConverted[i] = messageArgs[i];
            }
        }
        return messageArgsConverted;
    }

}
