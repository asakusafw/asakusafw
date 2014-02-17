/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ServiceLoader;

/**
 * システムにレポートを通知するためのAPI。
 * <p>
 * レポートの内容によってワークフロー制御が変わることはない。
 * ただし、ワークフローエンジンによっては、このレポートの情報をもとに
 * ジョブの結果にメタ情報を付与することも想定される。
 * </p>
 * <p>
 * このクラスのメソッドを利用する演算子では、以下のように<code>&#64;Sticky</code>を付与する必要がある。
 * この指定が無い場合、コンパイラの最適化によって、演算子の処理そのものが行われなくなる可能性がある。
 * </p>
<pre><code>
&#64;Sticky
&#64;Update
public void updateWithReport(Hoge hoge) {
    if (hoge.getValue() &lt; 0) {
        Report.error("invalid valud");
    } else {
        hoge.setValue(0);
    }
}
</code></pre>
 * @since 0.1.0
 * @version 0.5.1
 */
public final class Report {

    /**
     * {@code -D}で指定する{@link Report.Delegate}の実装クラスを指定するプロパティ名。
     * <p>
     * デフォルトを利用したい場合には、同プロパティに
     * {@code com.asakusafw.runtime.core.Report$Default}
     * を指定すること。
     * </p>
<pre><code>
例:
hadoop jar ... -D com.asakusafw.runtime.core.Report.Delegate=com.example.MockReportDelegate
</code></pre>
     */
    public static final String K_DELEGATE_CLASS = "com.asakusafw.runtime.core.Report.Delegate";

    private static final ThreadLocal<Delegate> DELEGATE = new ThreadLocal<Delegate>() {
        @Override
        protected Delegate initialValue() {
            throw new FailedException(
                    "Report is not initialized (report plugin may be not registered)");
        }
    };

    /**
     * このAPIに委譲先のオブジェクトを設定する。
     * <p>
     * 以降、このスレッドからこのメソッドを除く各種メソッドを起動した場合、
     * 指定の移譲オブジェクトに定義された所定のメソッドが起動する。
     * </p>
     * @param delegate 委譲先のオブジェクト、登録を解除する場合は{@code null}
     */
    public static void setDelegate(Delegate delegate) {
        if (delegate == null) {
            DELEGATE.remove();
        } else {
            DELEGATE.set(delegate);
        }
    }

    /**
     * ジョブフロー全体の終了状態に影響のない「情報」レポートを通知する。
     * <p>
     * このクラスのメソッドを利用する演算子では、メソッド宣言に<code>&#64;Sticky</code>を付与する必要がある。
     * </p>
     * @param message 通知するメッセージ
     * @throws Report.FailedException レポートの通知に失敗した場合
     * @see Report
     */
    public static void info(String message) {
        try {
            DELEGATE.get().report(Level.INFO, message);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * ジョブフロー全体の終了状態に影響のない「情報」レポートを通知する。
     * <p>
     * このクラスのメソッドを利用する演算子では、メソッド宣言に<code>&#64;Sticky</code>を付与する必要がある。
     * </p>
     * @param message 通知するメッセージ
     * @param throwable 例外情報 (省略可能)
     * @throws Report.FailedException レポートの通知に失敗した場合
     * @see Report
     * @since 0.5.1
     */
    public static void info(String message, Throwable throwable) {
        try {
            DELEGATE.get().report(Level.INFO, message, throwable);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * ジョブフロー全体の終了状態を警告状態以上にする「警告」レポートを通知する。
     * <p>
     * このクラスのメソッドを利用する演算子では、メソッド宣言に<code>&#64;Sticky</code>を付与する必要がある。
     * </p>
     * @param message 通知するメッセージ
     * @throws Report.FailedException レポートの通知に失敗した場合
     * @see Report
     */
    public static void warn(String message) {
        try {
            DELEGATE.get().report(Level.WARN, message);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * ジョブフロー全体の終了状態を警告状態以上にする「警告」レポートを通知する。
     * <p>
     * このクラスのメソッドを利用する演算子では、メソッド宣言に<code>&#64;Sticky</code>を付与する必要がある。
     * </p>
     * @param message 通知するメッセージ
     * @param throwable 例外情報 (省略可能)
     * @throws Report.FailedException レポートの通知に失敗した場合
     * @see Report
     * @since 0.5.1
     */
    public static void warn(String message, Throwable throwable) {
        try {
            DELEGATE.get().report(Level.WARN, message, throwable);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * ジョブフロー全体の終了状態を異常状態にする「エラー」レポートを通知する。
     * <p>
     * このクラスのメソッドを利用する演算子では、メソッド宣言に<code>&#64;Sticky</code>を付与する必要がある。
     * </p>
     * <p>
     * このレポート通知を行った後も、処理は継続される。
     * 処理を即座に終了させる場合には、演算子内で適切な例外をスローすること。
     * </p>
     * @param message 通知するメッセージ
     * @throws Report.FailedException レポートの通知に失敗した場合
     * @see Report
     */
    public static void error(String message) {
        try {
            DELEGATE.get().report(Level.ERROR, message);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * ジョブフロー全体の終了状態を異常状態にする「エラー」レポートを通知する。
     * <p>
     * このクラスのメソッドを利用する演算子では、メソッド宣言に<code>&#64;Sticky</code>を付与する必要がある。
     * </p>
     * <p>
     * このレポート通知を行った後も、処理は継続される。
     * 処理を即座に終了させる場合には、演算子内で適切な例外をスローすること。
     * </p>
     * @param message 通知するメッセージ
     * @param throwable 例外情報 (省略可能)
     * @throws Report.FailedException レポートの通知に失敗した場合
     * @see Report
     * @since 0.5.1
     */
    public static void error(String message, Throwable throwable) {
        try {
            DELEGATE.get().report(Level.ERROR, message, throwable);
        } catch (IOException e) {
            throw new FailedException(e);
        }
    }

    /**
     * インスタンス化の禁止。
     */
    private Report() {
        throw new AssertionError();
    }

    /**
     * レポートに失敗したことを表す例外。
     */
    public static class FailedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * インスタンスを生成する。
         */
        public FailedException() {
            super();
        }

        /**
         * インスタンスを生成する。
         * @param message メッセージ (省略可)
         * @param cause この例外の原因となった例外 (省略可)
         */
        public FailedException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * インスタンスを生成する。
         * @param message メッセージ (省略可)
         */
        public FailedException(String message) {
            super(message);
        }

        /**
         * インスタンスを生成する。
         * @param cause この例外の原因となった例外 (省略可)
         */
        public FailedException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * {@link Report}クラスの実体。
     * @since 0.1.0
     * @version 0.5.1
     */
    public abstract static class Delegate implements RuntimeResource {

        @Override
        public void setup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            return;
        }

        @Override
        public void cleanup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            return;
        }

        /**
         * 指定のレベルのレポートを通知する。
         * @param level レポートのレベル
         * @param message メッセージ
         * @throws IOException レポートの通知に失敗した場合
         */
        protected abstract void report(Level level, String message) throws IOException;

        /**
         * Notifies a report.
         * @param level report level
         * @param message report message
         * @param throwable optional exception info (nullable)
         * @throws IOException if failed to notify this report by I/O error
         * @since 0.5.1
         */
        protected void report(Level level, String message, Throwable throwable) throws IOException {
            report(level, message);
        }
    }

    /**
     * レポートのレベル。
     */
    public enum Level {

        /**
         * ジョブフロー全体の終了状態に影響のない「情報」レポート。
         */
        INFO,

        /**
         * ジョブフロー全体の終了状態を警告状態以上にする「警告」レポート。
         */
        WARN,

        /**
         * ジョブフロー全体の終了状態を異常状態にする「エラー」レポートを通知する。
         */
        ERROR,
    }

    /**
     * {@link Report}クラスの移譲オブジェクトを初期化する。
     * <p>
     * このクラスを{@link ServiceLoader}に登録すると、
     * {@link Report#K_DELEGATE_CLASS}で指定した
     * {@link Report.Delegate}クラスのインスタンスを委譲オブジェクトとして登録する。
     * </p>
     */
    public static class Initializer extends RuntimeResource.DelegateRegisterer<Delegate> {

        @Override
        protected String getClassNameKey() {
            return K_DELEGATE_CLASS;
        }

        @Override
        protected Class<? extends Delegate> getInterfaceType() {
            return Delegate.class;
        }

        @Override
        protected void register(Delegate delegate, ResourceConfiguration configuration) throws IOException,
                InterruptedException {
            delegate.setup(configuration);
            setDelegate(delegate);
        }

        @Override
        protected void unregister(Delegate delegate, ResourceConfiguration configuration) throws IOException,
                InterruptedException {
            setDelegate(null);
            delegate.cleanup(configuration);
        }
    }

    /**
     * {@link Report.Delegate}の標準的な実装。
     * @since 0.1.0
     * @version 0.5.1
     */
    public static class Default extends Delegate {

        @Override
        protected void report(Level level, String message) {
            switch (level) {
            case INFO:
                System.out.println(message);
                break;
            case WARN:
                System.err.println(message);
                new Exception("Warning").printStackTrace();
                break;
            case ERROR:
                System.err.println(message);
                new Exception("Error").printStackTrace();
                break;
            default:
                throw new AssertionError(MessageFormat.format(
                        "[{0}] {1}",
                        level,
                        message));
            }
        }

        @Override
        protected void report(Level level, String message, Throwable throwable) {
            switch (level) {
            case INFO:
                System.out.println(message);
                if (throwable != null) {
                    throwable.printStackTrace(System.out);
                }
                break;
            case WARN:
            case ERROR:
                System.err.println(message);
                if (throwable != null) {
                    throwable.printStackTrace(System.err);
                }
                break;
            default:
                throw new AssertionError(MessageFormat.format(
                        "[{0}] {1}",
                        level,
                        message));
            }
        }
    }
}
