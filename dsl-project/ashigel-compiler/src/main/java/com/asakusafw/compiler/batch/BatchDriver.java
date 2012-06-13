/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;

/**
 * {@link Batch}が付与されている要素について、フローの構造を解析する。
 */
public final class BatchDriver {

    static final Logger LOG = LoggerFactory.getLogger(BatchDriver.class);

    private Class<? extends BatchDescription> description;

    private BatchClass batchClass;

    private List<String> diagnostics;

    private BatchDriver(Class<? extends BatchDescription> description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        this.description = description;
        this.diagnostics = new ArrayList<String>();
    }

    /**
     * インスタンスを生成する。
     * @param description 対象のバッチクラス
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static BatchDriver analyze(Class<? extends BatchDescription> description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        BatchDriver analyzer = new BatchDriver(description);
        analyzer.analyze();
        return analyzer;
    }

    /**
     * 解析結果のバッチクラス情報を返す。
     * @return 解析結果のバッチクラス情報、解析に失敗した場合は{@code null}
     */
    public BatchClass getBatchClass() {
        return batchClass;
    }

    /**
     * 解析対象のバッチクラスを返す。
     * @return 解析対象のバッチクラス
     */
    public Class<? extends BatchDescription> getDescription() {
        return this.description;
    }

    /**
     * 解析結果の診断メッセージを返す。
     * @return 解析結果の診断メッセージ
     */
    public List<String> getDiagnostics() {
        return diagnostics;
    }

    private void analyze() {
        Batch config = findConfig();
        BatchDescription instance = describe();
        if (hasError()) {
            return;
        }
        this.batchClass = new BatchClass(config, instance);
    }

    private Batch findConfig() {
        if (description.getEnclosingClass() != null) {
            error(null, "バッチクラスはトップレベルクラスとして宣言する必要があります");
        }
        if (Modifier.isPublic(description.getModifiers()) == false) {
            error(null, "バッチクラスはpublicで宣言する必要があります");
        }
        if (Modifier.isAbstract(description.getModifiers())) {
            error(null, "バッチクラスはabstractで宣言できません");
        }
        Batch conf = description.getAnnotation(Batch.class);
        if (conf == null) {
            error(null, "バッチクラスには@Batch注釈の付与が必要です");
        }
        return conf;
    }

    private BatchDescription describe() {
        Constructor<? extends BatchDescription> ctor;
        try {
            ctor = description.getConstructor();
        } catch (Exception e) {
            error(
                    e,
                    "バッチクラス{0}には引数を取らないコンストラクター(またはデフォルトコンストラクター)が必要です ({1})",
                    description.getName(),
                    e.toString());
            return null;
        }
        BatchDescription instance;
        try {
            instance = ctor.newInstance();
        } catch (Exception e) {
            error(e, "{0}のインスタンス生成に失敗しました ({1})", description.getName(), e.toString());
            return null;
        }
        try {
            instance.start();
        } catch (Exception e) {
            error(e, "{0}の解析に失敗しました ({1})", description.getName(), e.toString());
            return null;
        }
        return instance;
    }

    private void error(
            Throwable reason,
            String message,
            Object... args) {
        String text = format(message, args);
        diagnostics.add(text);
        if (reason == null) {
            LOG.error(text);
        } else {
            LOG.error(text, reason);
        }
    }

    private String format(String message, Object... args) {
        assert message != null;
        assert args != null;
        if (args.length == 0) {
            return message;
        } else {
            return MessageFormat.format(message, args);
        }
    }

    /**
     * この解析結果にエラーが含まれている場合のみ{@code true}を返す。
     * @return 解析結果にエラーが含まれている場合のみ{@code true}
     */
    public boolean hasError() {
        return getDiagnostics().isEmpty() == false;
    }
}
