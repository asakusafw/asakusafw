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
package com.asakusafw.compiler.flow;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.util.TypeUtil;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * {@link JobFlow}が付与されている要素について、フローの構造を解析する。
 */
public final class JobFlowDriver {

    static final Logger LOG = LoggerFactory.getLogger(JobFlowDriver.class);

    private Class<? extends FlowDescription> description;

    private JobFlowClass jobFlowClass;

    private List<String> diagnostics;

    private JobFlowDriver(Class<? extends FlowDescription> description) {
        assert description != null;
        this.description = description;
        this.diagnostics = Lists.create();
    }

    /**
     * インスタンスを生成する。
     * @param description 対象のジョブフロークラス
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static JobFlowDriver analyze(Class<? extends FlowDescription> description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        JobFlowDriver analyzer = new JobFlowDriver(description);
        analyzer.analyze();
        return analyzer;
    }

    /**
     * 解析結果のジョブフロークラス情報を返す。
     * @return 解析結果のジョブフロークラス情報、解析に失敗した場合は{@code null}
     */
    public JobFlowClass getJobFlowClass() {
        return jobFlowClass;
    }

    /**
     * 解析対象のジョブフロークラスを返す。
     * @return 解析対象のジョブフロークラス
     */
    public Class<? extends FlowDescription> getDescription() {
        return description;
    }

    /**
     * 解析結果の診断メッセージを返す。
     * @return 解析結果の診断メッセージ
     */
    public List<String> getDiagnostics() {
        return diagnostics;
    }

    private void analyze() {
        JobFlow config = findConfig();
        Constructor<? extends FlowDescription> ctor = findConstructor();
        if (ctor == null) {
            return;
        }
        FlowDescriptionDriver driver = parseParameters(ctor);
        if (hasError()) {
            return;
        }
        FlowDescription instance = newInstance(ctor, driver.getPorts());
        if (hasError()) {
            return;
        }
        try {
            FlowGraph graph = driver.createFlowGraph(instance);
            this.jobFlowClass = new JobFlowClass(config, graph);
        } catch (Exception e) {
            error(e, "{0}の解析に失敗しました ({1})", description.getName(), e.toString());
        }
    }

    private FlowDescription newInstance(
            Constructor<? extends FlowDescription> ctor,
            List<?> ports) {
        assert ctor != null;
        assert ports != null;
        try {
            return ctor.newInstance(ports.toArray());
        } catch (Exception e) {
            error(e, "{0}のインスタンス生成に失敗しました ({1})", description.getName(), e.toString());
            return null;
        }
    }

    private JobFlow findConfig() {
        if (description.getEnclosingClass() != null) {
            error(null, "ジョブフロークラスはトップレベルクラスとして宣言する必要があります");
        }
        if (Modifier.isPublic(description.getModifiers()) == false) {
            error(null, "ジョブフロークラスはpublicで宣言する必要があります");
        }
        if (Modifier.isAbstract(description.getModifiers())) {
            error(null, "ジョブフロークラスはabstractで宣言できません");
        }
        JobFlow conf = description.getAnnotation(JobFlow.class);
        if (conf == null) {
            error(null, "ジョブフロークラスには@JobFlow注釈の付与が必要です");
        }
        return conf;
    }

    private Constructor<? extends FlowDescription> findConstructor() {
        @SuppressWarnings("unchecked")
        Constructor<? extends FlowDescription>[] ctors =
            (Constructor<? extends FlowDescription>[]) description.getConstructors();
        if (ctors.length == 0) {
            error(null, "ジョブフロークラスにはpublicコンストラクターの宣言が必要です");
            return null;
        } else if (ctors.length >= 2) {
            error(null, "ジョブフロークラスにはpublicコンストラクターを一つだけ宣言できます");
            return ctors[0];
        } else {
            return ctors[0];
        }
    }

    /**
     * この解析結果にエラーが含まれている場合のみ{@code true}を返す。
     * @return 解析結果にエラーが含まれている場合のみ{@code true}
     */
    public boolean hasError() {
        return diagnostics.isEmpty() == false;
    }

    private FlowDescriptionDriver parseParameters(Constructor<?> ctor) {
        assert ctor != null;
        List<Parameter> rawParams = parseRawParameters(ctor);
        FlowDescriptionDriver driver = new FlowDescriptionDriver();
        for (Parameter raw : rawParams) {
            analyzeParameter(raw, driver);
        }
        return driver;
    }

    private void analyzeParameter(Parameter parameter, FlowDescriptionDriver driver) {
        assert parameter != null;
        assert driver != null;
        if (parameter.raw == In.class) {
            analyzeInput(parameter, driver);
        } else if (parameter.raw == Out.class) {
            analyzeOutput(parameter, driver);
        } else {
            error(
                    null,
                    "ジョブフローのコンストラクターにはInかOutの入出力のみを指定できます ({0}番目の引数)",
                    parameter.getPosition());
        }
    }

    private void analyzeInput(Parameter parameter, FlowDescriptionDriver driver) {
        assert parameter != null;
        assert driver != null;
        Type dataType = invoke(In.class, parameter.type);
        if (dataType == null) {
            error(
                    null,
                    "ジョブフローの入力には型引数としてデータの種類を指定する必要があります ({0}番目の引数)",
                    parameter.getPosition());
            return;
        }
        if (parameter.exporter != null) {
            error(
                    null,
                    "ジョブフローの入力には@Exportを指定できません ({0}番目の引数)",
                    parameter.getPosition());
        }
        if (parameter.importer == null) {
            error(
                    null,
                    "ジョブフローの入力には@Importを指定する必要があります ({0}番目の引数)",
                    parameter.getPosition());
            return;
        } else {
            String name = parameter.importer.name();
            if (driver.isValidName(name) == false) {
                error(
                        null,
                        "ジョブフローの入力の名前 \"{1}\" が正しくありません ({0}番目の引数)",
                        parameter.getPosition(),
                        name);
                return;
            }
            Class<? extends ImporterDescription> aClass = parameter.importer.description();
            ImporterDescription importer;
            try {
                importer = aClass.newInstance();
            } catch (Exception e) {
                error(
                        e,
                        "インポーター記述{0} ({1}番目の引数) の解析に失敗しました (インスタンス化に失敗しました)",
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }

            if (importer.getModelType() == null) {
                error(
                        null,
                        "インポーター記述{0} ({1}番目の引数) の解析に失敗しました (インポーター記述にデータの種類が指定されていません)",
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }
            if (dataType.equals(importer.getModelType()) == false) {
                error(
                        null,
                        "インポーター記述{0} ({1}番目の引数) の解析に失敗しました (入力の型とインポーター記述の型が一致しません)",
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }
            driver.createIn(name, importer);
        }
    }

    private void analyzeOutput(Parameter parameter, FlowDescriptionDriver driver) {
        assert parameter != null;
        assert driver != null;
        Type dataType = invoke(Out.class, parameter.type);
        if (dataType == null) {
            error(
                    null,
                    "ジョブフローの出力には型引数としてデータの種類を指定する必要があります ({0}番目の引数)",
                    parameter.getPosition());
            return;
        }
        if (parameter.importer != null) {
            error(
                    null,
                    "ジョブフローの出力には@Importを指定できません ({0}番目の引数)",
                    parameter.getPosition());
        }
        if (parameter.exporter == null) {
            error(
                    null,
                    "ジョブフローの出力には@Exportを指定する必要があります ({0}番目の引数)",
                    parameter.getPosition());
            return;
        } else {
            String name = parameter.exporter.name();
            if (driver.isValidName(name) == false) {
                error(
                        null,
                        "ジョブフローの出力の名前 \"{1}\" が正しくありません ({0}番目の引数)",
                        parameter.getPosition(),
                        name);
                return;
            }
            Class<? extends ExporterDescription> aClass = parameter.exporter.description();

            ExporterDescription exporter;
            try {
                exporter = aClass.newInstance();
            } catch (Exception e) {
                error(
                        e,
                        "エクスポーター記述{0} ({1}番目の引数) の解析に失敗しました (インスタンス化に失敗しました)",
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }

            if (exporter.getModelType() == null) {
                error(
                        null,
                        "エクスポーター記述{0} ({1}番目の引数) の解析に失敗しました (エクスポーター記述にデータの種類が指定されていません)",
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }
            if (dataType.equals(exporter.getModelType()) == false) {
                error(
                        null,
                        "エクスポーター記述{0} ({1}番目の引数) の解析に失敗しました (出力の型とエクスポーター記述の型が一致しません)",
                        aClass.getName(),
                        parameter.getPosition());
                return;
            }
            driver.createOut(name, exporter);
        }
    }

    private void error(
            Throwable reason,
            String message,
            Object... args) {
        StringBuilder buf = new StringBuilder();
        buf.append(format(message, args));
        buf.append(" - "); //$NON-NLS-1$
        buf.append(description.getName());
        String text = buf.toString();
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

    private Type invoke(Class<?> target, Type subtype) {
        assert target != null;
        assert subtype != null;
        List<Type> invoked = TypeUtil.invoke(target, subtype);
        if (invoked == null || invoked.size() != 1) {
            return null;
        }
        return invoked.get(0);
    }

    private List<Parameter> parseRawParameters(Constructor<?> ctor) {
        assert ctor != null;
        Class<?>[] rawTypes = ctor.getParameterTypes();
        Type[] types = ctor.getGenericParameterTypes();
        Annotation[][] annotations = ctor.getParameterAnnotations();
        List<Parameter> results = Lists.create();
        for (int i = 0; i < types.length; i++) {
            Import importer = null;
            Export expoter = null;
            for (Annotation a : annotations[i]) {
                if (a.annotationType() == Import.class) {
                    importer = (Import) a;
                } else if (a.annotationType() == Export.class) {
                    expoter = (Export) a;
                }
            }
            results.add(new Parameter(
                    i,
                    rawTypes[i],
                    types[i],
                    importer,
                    expoter));
        }
        return results;
    }

    private static class Parameter {

        final int index;

        final Class<?> raw;

        final Type type;

        final Import importer;

        final Export exporter;

        Parameter(
                int index,
                Class<?> raw,
                Type type,
                Import importer,
                Export exporter) {
            assert raw != null;
            assert type != null;
            this.index = index;
            this.raw = raw;
            this.type = type;
            this.importer = importer;
            this.exporter = exporter;
        }

        int getPosition() {
            return index + 1;
        }
    }
}
