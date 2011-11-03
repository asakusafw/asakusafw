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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.testdriver.core.ModelTester;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;

/**
 * フロー部品のテスト出力データオブジェクト。
 * @since 0.2.0
 *
 * @param <T> モデルクラス
 */
public class FlowPartDriverOutput<T> extends DriverOutputBase<T> implements Out<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowPartDriverOutput.class);

    /** フロー記述ドライバ */
    protected FlowDescriptionDriver descDriver;

    private final Out<T> out;

    /**
     * コンストラクタ
     *
     * @param driverContext テストドライバコンテキスト。
     * @param descDriver フロー定義ドライバ。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public FlowPartDriverOutput(TestDriverContext driverContext, FlowDescriptionDriver descDriver, String name,
            Class<T> modelType) {
        this.driverContext = driverContext;
        this.descDriver = descDriver;
        this.name = name;
        this.modelType = modelType;

        String exportPath = FlowPartDriverUtils.createOutputLocation(driverContext, name).toPath('/');
        LOG.info("Export Path=" + exportPath);
        exporterDescription = new DirectExporterDescription(modelType, exportPath);
        out = descDriver.createOut(name, exporterDescription);
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     * @param sourcePath 入力データのパス
     * @return this。
     */
    public FlowPartDriverOutput<T> prepare(String sourcePath) {
        LOG.info("prepare - ModelType:" + getModelType());
        setSourceUri(sourcePath);
        return this;
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedPath 期待値データのパス
     * @param verifyRulePath 検証ルールのパス
     * @return this
     */
    public FlowPartDriverOutput<T> verify(String expectedPath, String verifyRulePath) {
        LOG.info("verify - ModelType:" + modelType);
        try {
            setVerifier(expectedPath, verifyRulePath, Collections.<ModelTester<T>>emptyList());
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Failed to build a verifier: output={0}, expected={1}, rule={2}",
                    name,
                    expectedPath,
                    verifyRulePath), e);
        }
        return this;
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedPath 期待値データのパス
     * @param verifyRulePath 検証ルールのパス
     * @param tester 追加検証ルール
     * @return this
     * @since 0.2.3
     */
    public FlowPartDriverOutput<T> verify(String expectedPath, String verifyRulePath, ModelTester<? super T> tester) {
        LOG.info("verify - ModelType:" + modelType);
        try {
            setVerifier(expectedPath, verifyRulePath, Collections.singletonList(tester));
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Failed to build a verifier: output={0}, expected={1}, rule={2}, tester={3}",
                    name,
                    expectedPath,
                    verifyRulePath,
                    tester), e);
        }
        return this;
    }

    /**
     * テスト結果の検証データを指定する。
     * @param expectedPath 期待値データのパス
     * @param modelVerifier 検証ルール
     * @return this
     */
    public FlowPartDriverOutput<T> verify(String expectedPath, ModelVerifier<? super T> modelVerifier) {
        LOG.info("verify - ModelType:" + modelType);
        try {
            setVerifier(expectedPath, modelVerifier);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Failed to build a verifier: output={0}, expected={1}, modelVerifier={2}",
                    name,
                    expectedPath,
                    modelVerifier), e);
        }
        return this;
    }

    /**
     * テスト結果の検証データを指定する。
     * @param factory 検証エンジンのファクトリ
     * @return this
     * @since 0.2.3
     */
    public FlowPartDriverOutput<T> verify(VerifierFactory factory) {
        LOG.info("verify - ModelType:" + modelType);
        setVerifier(factory);
        return this;
    }

    /**
     * テスト結果のデータを書き出す先を指定する。
     * @param outputPath テスト結果データの出力先
     * @return this
     * @since 0.2.3
     */
    public FlowPartDriverOutput<T> dumpActual(String outputPath) {
        setResultSinkUri(outputPath);
        return this;
    }

    /**
     * テスト結果のデータを書き出す先を指定する。
     * @param outputPath テスト結果データの出力先
     * @return this
     * @since 0.2.3
     */
    public FlowPartDriverOutput<T> dumpActual(File outputPath) {
        setResultSinkUri(outputPath.toURI());
        return this;
    }

    /**
     * テスト結果のデータを受け取るオブジェクトのファクトリを指定する。
     * @param factory テスト結果のデータを受け取るオブジェクトのファクトリ
     * @return this
     * @since 0.2.3
     */
    public FlowPartDriverOutput<T> dumpActual(DataModelSinkFactory factory) {
        setResultSink(factory);
        return this;
    }

    /**
     * テスト結果の差異を書き出す先を指定する。
     * @param outputPath 差分データの出力先
     * @return this
     * @since 0.2.3
     */
    public FlowPartDriverOutput<T> dumpDifference(String outputPath) {
        setDifferenceSinkUri(outputPath);
        return this;
    }

    /**
     * テスト結果の差異を書き出す先を指定する。
     * @param outputPath 差分データの出力先
     * @return this
     * @since 0.2.3
     */
    public FlowPartDriverOutput<T> dumpDifference(File outputPath) {
        setDifferenceSinkUri(outputPath.toURI());
        return this;
    }

    /**
     * テスト結果の差異を受け取るオブジェクトのファクトリを指定する。
     * @param factory テスト結果の差異を受け取るオブジェクトのファクトリ
     * @return this
     * @since 0.2.3
     */
    public FlowPartDriverOutput<T> dumpDifference(DifferenceSinkFactory factory) {
        setDifferenceSink(factory);
        return this;
    }

    @Override
    public void add(Source<T> upstream) {
        out.add(upstream);
    }
}
