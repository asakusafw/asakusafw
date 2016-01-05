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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * ログを出力するユーティリティクラス。
 * @author yuta.shirai
 *
 */
public final class Log {
    /** ログタイムスタンプがnullの場合に表示する文字列。 */
    private static final String LOG_TSTAMP_NULL_STR = "---------- --:--:--.---";
    /** メッセージIDがnullの場合に表示する文字列。 */
    private static final String LOG_MESSAGE_ID_NULL_STR = "-";
    /** メッセージ引数がnullの場合に表示する文字列。 */
    private static final String LOG_MESSAGE_ARG_NULL_STR = "(null)";

    /**
     * 本クラスはインスタンス化せず、staticメソッドを直接呼び出してください。
     */
    private Log() {
    	return;
    }

    /**
     * ログ出力を行います。
     * <p>
     * ログが初期化されていない場合は、何も処理を行いません。
     * </p>
     * @param clazz 発生クラス
     * @param messageId メッセージId
     * @param messageArgs メッセージ引数
     * @return 出力したログメッセージ。ログが出力されていない場合は <code>null</code>
     */
    public static String log(Class<?> clazz, String messageId, Object... messageArgs) {
        return Log.log(null, clazz, messageId, messageArgs);
    }

    /**
     * スタックトレース付きで、ログ出力を行います。
     * <p>
     * ログが初期化されていない場合は、何も処理を行いません。
     * </p>
     * @param t 例外
     * @param clazz 発生クラス
     * @param messageId メッセージId
     * @param messageArgs メッセージ引数
     * @return 出力したログメッセージ。ログが出力されていない場合は <code>null</code>
     */
    public static String log(Throwable t, Class<?> clazz, String messageId, Object... messageArgs) {
        if (!LogInitializer.isInitialized()) {
            return null;
        }

        String message = LogMessageManager.getInstance().createLogMessage(messageId,
                Log.changeNullToStr(messageArgs, LOG_MESSAGE_ARG_NULL_STR));
        Level level = LogMessageManager.getInstance().getLogLevel(messageId);
        Timestamp logTime = new Timestamp(System.currentTimeMillis());

        Log.setMDC(messageId, logTime);
        Log.writeLog(t, clazz, level, message);
        Log.resetMDC();

        return message;
    }

    /**
     * MDCに値を設定します。
     *
     * @param messageId メッセージId
     * @param logTime ログ発生日時
     */
    private static void setMDC(String messageId, Timestamp logTime) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        MDC.put("LOG_TSTAMP", dateFormat.format(logTime));
        MDC.put("MESSAGE_ID", messageId);
    }

    /**
     * MDCの値をクリアします。
     *
     */
    private static void resetMDC() {
        MDC.put("LOG_TSTAMP", LOG_TSTAMP_NULL_STR);
        MDC.put("MESSAGE_ID", LOG_MESSAGE_ID_NULL_STR);
    }

    /**
     * ログをファイルに書き込みます。
     *
     * @param t 例外
     * @param clazz 呼び出し元クラス
     * @param level 重要度
     * @param message メッセージ
     */
    private static void writeLog(Throwable t, Class<?> clazz, Level level, String message) {
        Logger logger = Logger.getLogger(clazz);
        if (logger.isEnabledFor(level)) {

            if (Level.DEBUG == level) {
                logger.debug(message, t);
            } else if (Level.INFO == level) {
                logger.info(message, t);
            } else if (Level.WARN == level) {
                logger.warn(message, t);
            } else if (Level.ERROR == level) {
                logger.error(message, t);
            } else if (Level.FATAL == level) {
                logger.fatal(message, t);
            }
        }
    }

    /**
     * オブジェクト配列の要素がnullの場合、指定された文字列に変換します。
     * @param objects 変換前オブジェクト
     * @param str <code>null</code> を置き換える文字列
     * @return 変換後オブジェクト
     */
    private static Object[] changeNullToStr(Object[] objects, String str) {
        Object[] newObjects = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                newObjects[i] = str;
            } else {
                newObjects[i] = objects[i];
            }
        }
        return newObjects;
    }
}
