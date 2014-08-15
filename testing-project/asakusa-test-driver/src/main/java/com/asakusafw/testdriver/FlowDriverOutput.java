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
import java.util.Collections;
import java.util.List;

import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.DataModelSourceFilter;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.ModelTester;
import com.asakusafw.testdriver.core.ModelTransformer;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;

/**
 * An abstract super class which represents an output port of data flow on testing.
 * Clients should not inherit this class directly.
 * @param <T> the output data model type
 * @param <S> the implementation class type
 * @since 0.6.0
 * @version 0.7.0
 */
public abstract class FlowDriverOutput<T, S extends FlowDriverOutput<T, S>> extends DriverOutputBase<T> {

    /**
     * Creates a new instance.
     * @param callerClass the current context class
     * @param testTools the test data tools
     * @param name the original input name
     * @param modelType the data model type
     * @since 0.6.0
     */
    public FlowDriverOutput(Class<?> callerClass, TestDataToolProvider testTools, String name, Class<T> modelType) {
        super(callerClass, testTools, name, modelType);
    }

    /**
     * Returns this object.
     * @return this
     * @since 0.6.0
     */
    protected abstract S getThis();

    /**
     * テスト実行時に使用する初期データを指定する。
     * @param sourceFactory 初期データを提供するファクトリ
     * @return this
     * @since 0.6.0
     */
    public S prepare(DataModelSourceFactory sourceFactory) {
        if (sourceFactory == null) {
            throw new IllegalArgumentException("sourceFactory must not be null"); //$NON-NLS-1$
        }
        setSource(sourceFactory);
        return getThis();
    }

    /**
     * テスト結果の検証データを指定する。
     * @param factory 検証エンジンのファクトリ
     * @return this
     * @since 0.2.3
     */
    public S verify(VerifierFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        setVerifier(factory);
        return getThis();
    }

    /**
     * テスト結果のデータを検証前に変形するフィルタを指定する。
     * @param filter 変形に利用するフィルタ
     * @return this
     * @since 0.7.0
     */
    public S filter(DataModelSourceFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null"); //$NON-NLS-1$
        }
        setResultFilter(filter);
        return getThis();
    }

    /**
     * テスト結果のデータを受け取るオブジェクトのファクトリを指定する。
     * @param factory テスト結果のデータを受け取るオブジェクトのファクトリ
     * @return this
     * @since 0.2.3
     */
    public S dumpActual(DataModelSinkFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        setResultSink(factory);
        return getThis();
    }

    /**
     * テスト結果の差異を受け取るオブジェクトのファクトリを指定する。
     * @param factory テスト結果の差異を受け取るオブジェクトのファクトリ
     * @return this
     * @since 0.2.3
     */
    public S dumpDifference(DifferenceSinkFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        setDifferenceSink(factory);
        return getThis();
    }

    /**
     * テスト実行時に使用する初期データを指定する。
     * @param sourcePath 初期データのパス
     * @return this
     * @throws IllegalArgumentException 初期データのパスに対するリソースが見つからない場合
     * @since 0.2.0
     */
    public S prepare(String sourcePath) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("sourcePath must not be null"); //$NON-NLS-1$
        }
        return prepare(toDataModelSourceFactory(sourcePath));
    }

    /**
     * テスト実行時に使用する初期データを指定する。
     * @param sourceObjects 初期データを表すオブジェクト一覧
     * @return this
     * @since 0.6.0
     */
    public S prepare(Iterable<? extends T> sourceObjects) {
        if (sourceObjects == null) {
            throw new IllegalArgumentException("sourceObjects must not be null"); //$NON-NLS-1$
        }
        return prepare(toDataModelSourceFactory(sourceObjects));
    }

    /**
     * テスト実行時に使用する初期データを指定する。
     * @param sourceProvider 初期データを提供するプロバイダー
     * @return this
     * @since 0.6.0
     */
    public S prepare(Provider<? extends Source<? extends T>> sourceProvider) {
        if (sourceProvider == null) {
            throw new IllegalArgumentException("sourceProvider must not be null"); //$NON-NLS-1$
        }
        return prepare(toDataModelSourceFactory(sourceProvider));
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedFactory 期待値データを提供するファクトリ
     * @param verifyRulePath 検証ルールのパス
     * @return this
     * @throws IllegalArgumentException 検証ルールのパスに対するリソースが見つからない場合
     * @since 0.6.0
     */
    public S verify(DataModelSourceFactory expectedFactory, String verifyRulePath) {
        if (expectedFactory == null) {
            throw new IllegalArgumentException("expectedFactory must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(expectedFactory, verifyRulePath, null);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedPath 期待値データのパス
     * @param verifyRulePath 検証ルールのパス
     * @return this
     * @throws IllegalArgumentException 期待値データまたは検証ルールのパスに対するリソースが見つからない場合
     * @since 0.2.0
     */
    public S verify(String expectedPath, String verifyRulePath) {
        if (expectedPath == null) {
            throw new IllegalArgumentException("expectedPath must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedPath), verifyRulePath, null);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedObjects 期待値データを表すオブジェクト一覧
     * @param verifyRulePath 検証ルールのパス
     * @return this
     * @throws IllegalArgumentException 検証ルールのパスに対するリソースが見つからない場合
     * @since 0.6.0
     */
    public S verify(Iterable<? extends T> expectedObjects, String verifyRulePath) {
        if (expectedObjects == null) {
            throw new IllegalArgumentException("expectedObjects must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedObjects), verifyRulePath, null);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedProvider 期待値データを提供するプロバイダー
     * @param verifyRulePath 検証ルールのパス
     * @return this
     * @throws IllegalArgumentException 検証ルールのパスに対するリソースが見つからない場合
     * @since 0.6.0
     */
    public S verify(Provider<? extends Source<? extends T>> expectedProvider, String verifyRulePath) {
        if (expectedProvider == null) {
            throw new IllegalArgumentException("expectedProvider must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedProvider), verifyRulePath, null);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedFactory 期待値データを提供するファクトリ
     * @param verifyRulePath 検証ルールのパス
     * @param tester 追加検証ルール (省略可能)
     * @return this
     * @throws IllegalArgumentException 検証ルールのパスに対するリソースが見つからない場合
     * @since 0.6.0
     */
    public S verify(DataModelSourceFactory expectedFactory, String verifyRulePath, ModelTester<? super T> tester) {
        if (expectedFactory == null) {
            throw new IllegalArgumentException("expectedFactory must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        List<? extends ModelTester<? super T>> extraRules;
        if (tester == null) {
            extraRules = Collections.emptyList();
        } else {
            extraRules = Collections.singletonList(tester);
        }
        setVerifier(toVerifierFactory(expectedFactory, toVerifyRuleFactory(verifyRulePath, extraRules)));
        return getThis();
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedPath 期待値データのパス
     * @param verifyRulePath 検証ルールのパス
     * @param tester 追加検証ルール
     * @return this
     * @throws IllegalArgumentException 期待値データまたは検証ルールのパスに対するリソースが見つからない場合
     * @since 0.2.3
     */
    public S verify(String expectedPath, String verifyRulePath, ModelTester<? super T> tester) {
        if (expectedPath == null) {
            throw new IllegalArgumentException("expectedPath must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        if (tester == null) {
            throw new IllegalArgumentException("tester must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedPath), verifyRulePath, tester);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedObjects 期待値データを表すオブジェクト一覧
     * @param verifyRulePath 検証ルールのパス
     * @param tester 追加検証ルール
     * @return this
     * @throws IllegalArgumentException 検証ルールのパスに対するリソースが見つからない場合
     * @since 0.6.0
     */
    public S verify(Iterable<? extends T> expectedObjects, String verifyRulePath, ModelTester<? super T> tester) {
        if (expectedObjects == null) {
            throw new IllegalArgumentException("expectedObjects must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        if (tester == null) {
            throw new IllegalArgumentException("tester must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedObjects), verifyRulePath, tester);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedProvider 期待値データを提供するプロバイダー
     * @param verifyRulePath 検証ルールのパス
     * @param tester 追加検証ルール
     * @return this
     * @throws IllegalArgumentException 検証ルールのパスに対するリソースが見つからない場合
     * @since 0.6.0
     */
    public S verify(
            Provider<? extends Source<? extends T>> expectedProvider,
            String verifyRulePath, ModelTester<? super T> tester) {
        if (expectedProvider == null) {
            throw new IllegalArgumentException("expectedProvider must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        if (tester == null) {
            throw new IllegalArgumentException("tester must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedProvider), verifyRulePath, tester);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedFactory 期待値データを提供するファクトリ
     * @param modelVerifier 検証ルール
     * @return this
     * @since 0.6.0
     */
    public S verify(DataModelSourceFactory expectedFactory, ModelVerifier<? super T> modelVerifier) {
        if (expectedFactory == null) {
            throw new IllegalArgumentException("expectedFactory must not be null"); //$NON-NLS-1$
        }
        if (modelVerifier == null) {
            throw new IllegalArgumentException("modelVerifier must not be null"); //$NON-NLS-1$
        }
        setVerifier(toVerifierFactory(expectedFactory, toVerifyRuleFactory(modelVerifier)));
        return getThis();
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedPath 期待値データのパス
     * @param modelVerifier 検証ルール
     * @return this
     * @throws IllegalArgumentException 期待値データのパスに対するリソースが見つからない場合
     * @since 0.2.0
     */
    public S verify(String expectedPath, ModelVerifier<? super T> modelVerifier) {
        if (expectedPath == null) {
            throw new IllegalArgumentException("expectedPath must not be null"); //$NON-NLS-1$
        }
        if (modelVerifier == null) {
            throw new IllegalArgumentException("modelVerifier must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedPath), modelVerifier);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedObjects 期待値データ
     * @param modelVerifier 検証ルール
     * @return this
     * @since 0.6.0
     */
    public S verify(Iterable<? extends T> expectedObjects, ModelVerifier<? super T> modelVerifier) {
        if (expectedObjects == null) {
            throw new IllegalArgumentException("expectedObjects must not be null"); //$NON-NLS-1$
        }
        if (modelVerifier == null) {
            throw new IllegalArgumentException("modelVerifier must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedObjects), modelVerifier);
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedProvider 期待値データを提供するプロバイダー
     * @param modelVerifier 検証ルール
     * @return this
     * @since 0.6.0
     */
    public S verify(Provider<? extends Source<? extends T>> expectedProvider, ModelVerifier<? super T> modelVerifier) {
        if (expectedProvider == null) {
            throw new IllegalArgumentException("expectedProvider must not be null"); //$NON-NLS-1$
        }
        if (modelVerifier == null) {
            throw new IllegalArgumentException("modelVerifier must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedProvider), modelVerifier);
    }

    /**
     * テスト結果のデータを検証前に変形する。
     * @param transformer データモデルを変形する規則
     * @return this
     * @since 0.7.0
     */
    public S transform(ModelTransformer<? super T> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("transformer must not be null"); //$NON-NLS-1$
        }
        return filter(toDataModelSourceFilter(transformer));
    }

    /**
     * テスト結果のデータを書き出す先を指定する。
     * @param outputPath テスト結果データの出力先
     * @return this
     * @since 0.2.3
     */
    public S dumpActual(String outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        return dumpActual(toDataModelSinkFactory(outputPath));
    }

    /**
     * テスト結果のデータを書き出す先を指定する。
     * @param outputPath テスト結果データの出力先
     * @return this
     * @since 0.2.3
     */
    public S dumpActual(File outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        return dumpActual(toDataModelSinkFactory(outputPath));
    }

    /**
     * テスト結果の差異を書き出す先を指定する。
     * @param outputPath 差分データの出力先
     * @return this
     * @since 0.2.3
     */
    public S dumpDifference(String outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        return dumpDifference(toDifferenceSinkFactory(outputPath));
    }

    /**
     * テスト結果の差異を書き出す先を指定する。
     * @param outputPath 差分データの出力先
     * @return this
     * @since 0.2.3
     */
    public S dumpDifference(File outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        return dumpDifference(toDifferenceSinkFactory(outputPath));
    }
}
