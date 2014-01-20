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
package com.asakusafw.testdriver;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.trace.TracepointWeaveRewriter;
import com.asakusafw.trace.io.TraceSettingSerializer;
import com.asakusafw.trace.model.TraceSetting;
import com.asakusafw.trace.model.TraceSetting.Mode;
import com.asakusafw.trace.model.Tracepoint;
import com.asakusafw.trace.model.Tracepoint.PortKind;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;

/**
 * テストドライバの基底クラス。
 * @since 0.2.0
 * @version 0.5.2
 */
public abstract class TestDriverBase {

    private static final String FLOW_OPERATOR_FACTORY_METHOD_NAME = "create";

    /** テストドライバコンテキスト。テスト実行時のコンテキスト情報が格納される。 */
    protected TestDriverContext driverContext;

    /**
     * Creates a new instance.
     * @param callerClass the caller class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestDriverBase(Class<?> callerClass) {
        if (callerClass == null) {
            throw new IllegalArgumentException("callerClass must not be null"); //$NON-NLS-1$
        }
        this.driverContext = new TestDriverContext(callerClass);
    }

    /**
     * 設定項目を追加する。
     *
     * @param key
     *            追加する項目のキー名
     * @param value
     *            追加する項目の値、{@code null}の場合には項目を削除する
     * @throws IllegalArgumentException
     *             引数に{@code null}が指定された場合
     */
    public void configure(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value != null) {
            driverContext.getExtraConfigurations().put(key, value);
        } else {
            driverContext.getExtraConfigurations().remove(key);
        }
    }

    /**
     * バッチ実行時引数を設定する。
     *
     * @param key
     *            追加する項目のキー名
     * @param value
     *            追加する項目の値、{@code null}の場合には項目を削除する
     * @throws IllegalArgumentException
     *             引数に{@code null}が指定された場合
     */
    public void setBatchArg(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        if (value != null) {
            driverContext.getBatchArgs().put(key, value);
        } else {
            driverContext.getBatchArgs().remove(key);
        }
    }

    /**
     * コンパイラの最適化レベルを変更する。
     *
     * @param level
     *            0: 設定可能な最適化を全て行わない、1: デフォルト、2~: 最適化を積極的に行う
     */
    public void setOptimize(int level) {
        FlowCompilerOptions options = driverContext.getOptions();
        if (level <= 0) {
            options.setCompressConcurrentStage(false);
            options.setCompressFlowPart(false);
            options.setHashJoinForSmall(false);
            options.setHashJoinForTiny(false);
            options.setEnableCombiner(false);
        } else if (level == 1) {
            options.setCompressConcurrentStage(FlowCompilerOptions.Item.compressConcurrentStage.defaultValue);
            options.setCompressFlowPart(FlowCompilerOptions.Item.compressFlowPart.defaultValue);
            options.setHashJoinForSmall(FlowCompilerOptions.Item.hashJoinForSmall.defaultValue);
            options.setHashJoinForTiny(FlowCompilerOptions.Item.hashJoinForTiny.defaultValue);
            options.setEnableCombiner(FlowCompilerOptions.Item.enableCombiner.defaultValue);
        } else {
            options.setCompressConcurrentStage(true);
            options.setCompressFlowPart(true);
            options.setHashJoinForSmall(true);
            options.setHashJoinForTiny(true);
            options.setEnableCombiner(true);
        }
    }

    /**
     * コンパイラが生成するコードのデバッグ情報について変更する。
     *
     * @param enable
     *            デバッグ情報を残す場合に{@code true}、捨てる場合に{@code false}
     */
    public void setDebug(boolean enable) {
        driverContext.getOptions().setEnableDebugLogging(enable);
    }

    /**
     * フレームワークのホームパス({@code ASAKUSA_HOME})を設定する。
     * <p>
     * この値が未設定の場合、環境変数に設定された値を利用する。
     * </p>
     * @param frameworkHomePath フレームワークのホームパス、未設定に戻す場合は{@code null}
     */
    public void setFrameworkHomePath(File frameworkHomePath) {
        driverContext.setFrameworkHomePath(frameworkHomePath);
    }


    /**
     * 外部ライブラリを格納するディレクトリのパスを設定する。
     * <p>
     * この値が未設定の場合、 {@link TestDriverContext#EXTERNAL_LIBRARIES_PATH} で指定されたパスを利用する。
     * </p>
     * @param librariesPath 外部ライブラリを格納するディレクトリのパス
     * @since 0.5.1
     */
    public void setLibrariesPath(File librariesPath) {
        driverContext.setLibrariesPath(librariesPath);
    }

    /**
     * このテストで利用するDSLコンパイラの作業ディレクトリを設定する。
     * <p>
     * ワーキングディレクトリが設定されていない場合、テストドライバは一時的なディレクトリを作成し、
     * テスト終了時に作成したディレクトリを自動的に削除する。
     * </p>
     * <p>
     * 通常この設定を行う必要はないが、コンパイラが生成したプログラムなどを分析する場合などに設定されることを想定している。
     * </p>
     * @param path 設定するコンパイラの作業ディレクトリ
     * @since 0.5.2
     */
    public void setCompilerWorkingDirectory(File path) {
        driverContext.setCompilerWorkingDirectory(path);
    }

    /**
     * 入力データのクリーニング(truncate)をスキップするかを設定する。
     *
     * @param skip
     *            入力データのクリーニング(truncate)をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipCleanInput(boolean skip) {
        driverContext.setSkipCleanInput(skip);
    }

    /**
     * 出力データのクリーニング(truncate)をスキップするかを設定する。
     *
     * @param skip
     *            出力データのクリーニング(truncate)をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipCleanOutput(boolean skip) {
        driverContext.setSkipCleanOutput(skip);
    }

    /**
     * 入力データのセットアップ(prepare)をスキップするかを設定する。
     *
     * @param skip
     *            入力データのセットアップ(prepare)をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipPrepareInput(boolean skip) {
        driverContext.setSkipPrepareInput(skip);
    }

    /**
     * 出力データのセットアップ(prepare)をスキップするかを設定する。
     *
     * @param skip
     *            出力データのセットアップ(prepare)をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipPrepareOutput(boolean skip) {
        driverContext.setSkipPrepareOutput(skip);
    }

    /**
     * ジョブフローの実行をスキップするかを設定する。
     *
     * @param skip
     *            ジョブフローの実行をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipRunJobflow(boolean skip) {
        driverContext.setSkipRunJobflow(skip);
    }

    /**
     * テスト結果の検証をスキップするかを設定する。
     *
     * @param skip
     *            テスト結果の検証をスキップする場合は{@code true}、スキップしない場合は{@code false}
     */
    public void skipVerify(boolean skip) {
        driverContext.setSkipVerify(skip);
    }

    /**
     * Adds a new tracepoint to the target operator input.
     * @param operatorClass target operator class
     * @param methodName target operator method name
     * @param portName target operator input port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void addInputTrace(Class<?> operatorClass, String methodName, String portName) {
        if (operatorClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        if (methodName == null) {
            throw new IllegalArgumentException("methodName must not be null"); //$NON-NLS-1$
        }
        if (portName == null) {
            throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
        }
        TraceSetting setting = createTraceSetting(
                operatorClass, methodName,
                PortKind.INPUT, portName,
                Collections.<String, String>emptyMap());
        appendTrace(setting);
    }

    /**
     * Adds a new tracepoint to the target operator output.
     * @param operatorClass target operator class
     * @param methodName target operator method name
     * @param portName target operator input port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void addOutputTrace(Class<?> operatorClass, String methodName, String portName) {
        if (operatorClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        if (methodName == null) {
            throw new IllegalArgumentException("methodName must not be null"); //$NON-NLS-1$
        }
        if (portName == null) {
            throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
        }
        TraceSetting setting = createTraceSetting(
                operatorClass, methodName,
                PortKind.OUTPUT, portName,
                Collections.<String, String>emptyMap());
        appendTrace(setting);
    }

    /**
     * Adds a new tracepoint to the target operator input.
     * @param flowpartClass target flow-part class
     * @param portName target operator input port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void addInputTrace(Class<? extends FlowDescription> flowpartClass, String portName) {
        if (flowpartClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        if (portName == null) {
            throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
        }
        if (flowpartClass.isAnnotationPresent(FlowPart.class) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"flowpartClass\" must be a flow-part: {0}",
                    flowpartClass.getName()));
        }
        TraceSetting setting = createTraceSetting(
                flowpartClass, FLOW_OPERATOR_FACTORY_METHOD_NAME,
                PortKind.INPUT, portName,
                Collections.<String, String>emptyMap());
        appendTrace(setting);
    }

    /**
     * Adds a new tracepoint to the target operator output.
     * @param flowpartClass target flow-part class
     * @param portName target operator input port name
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public void addOutputTrace(Class<? extends FlowDescription> flowpartClass, String portName) {
        if (flowpartClass == null) {
            throw new IllegalArgumentException("operatorClass must not be null"); //$NON-NLS-1$
        }
        if (portName == null) {
            throw new IllegalArgumentException("portName must not be null"); //$NON-NLS-1$
        }
        if (flowpartClass.isAnnotationPresent(FlowPart.class) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The \"flowpartClass\" must be a flow-part: {0}",
                    flowpartClass.getName()));
        }
        TraceSetting setting = createTraceSetting(
                flowpartClass, FLOW_OPERATOR_FACTORY_METHOD_NAME,
                PortKind.OUTPUT, portName,
                Collections.<String, String>emptyMap());
        appendTrace(setting);
    }

    private void appendTrace(TraceSetting setting) {
        assert setting != null;
        String optionValue = driverContext.getOptions().getExtraAttribute(TracepointWeaveRewriter.KEY_COMPILER_OPTION);
        optionValue = appendTraceSetting(optionValue, setting);
        driverContext.getOptions().putExtraAttribute(TracepointWeaveRewriter.KEY_COMPILER_OPTION, optionValue);
    }

    private String appendTraceSetting(String option, TraceSetting setting) {
        List<TraceSetting> settings = new ArrayList<TraceSetting>();
        if (option != null) {
            Collection<? extends TraceSetting> loaded = TraceSettingSerializer.deserialize(option);
            settings.addAll(loaded);
        }
        settings.add(setting);
        return TraceSettingSerializer.serialize(settings);
    }

    static TraceSetting createTraceSetting(
            Class<?> operatorClass,
            String methodName,
            PortKind portKind,
            String portName,
            Map<String, String> attributes) {
        assert operatorClass != null;
        assert methodName != null;
        assert portKind != null;
        assert portName != null;
        assert attributes != null;
        return new TraceSetting(
                new Tracepoint(operatorClass.getName(), methodName, portKind, portName),
                Mode.STRICT, attributes);
    }
}
