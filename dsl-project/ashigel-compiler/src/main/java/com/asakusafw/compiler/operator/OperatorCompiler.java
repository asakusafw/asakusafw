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
package com.asakusafw.compiler.operator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.java.model.util.Models;

/**
 * 演算子DSLで利用する演算子に関するプロセッサ。
 * <p>
 * このプロセッサは、サブプロセッサとして
 * {@link com.asakusafw.compiler.operator.OperatorProcessor}
 * インターフェースを実装したクラスを利用する。
 * 同クラスを{@code META-INF/services/}下に指定のインターフェースに対する
 * サービスとして登録しておくことで、このプロセッサから自動的に参照する。
 * </p>
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorCompiler implements Processor {

    /**
     * このコンパイラのバージョン。
     */
    public static final String VERSION = "0.1.0"; //$NON-NLS-1$

    static final Logger LOG = LoggerFactory.getLogger(OperatorCompiler.class);

    private OperatorCompilingEnvironment environment;

    private Set<OperatorProcessor> subProcessors;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        try {
            OperatorCompilerOptions options = loadOptions(processingEnv);
            this.environment = new OperatorCompilingEnvironment(
                    processingEnv,
                    Models.getModelFactory(),
                    options);
            this.subProcessors = loadSubProcessors(environment);
        } catch (RuntimeException e) {
            environment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    e.getMessage());
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * このコンパイラに対するオプション項目の一覧を返す。
     * @param processingEnv 処理環境
     * @return オプション項目の一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     * @throws OperatorCompilerException 引数の解析に失敗した場合
     */
    protected OperatorCompilerOptions loadOptions(ProcessingEnvironment processingEnv) {
        Precondition.checkMustNotBeNull(processingEnv, "processingEnv"); //$NON-NLS-1$
        return OperatorCompilerOptions.parse(processingEnv.getOptions());
    }

    private Set<OperatorProcessor> loadSubProcessors(OperatorCompilingEnvironment env) {
        assert env != null;
        Map<Class<?>, OperatorProcessor> results = Maps.create();
        for (OperatorProcessor proc : findOperatorProcessors(env)) {
            proc.initialize(env);
            Class<? extends Annotation> target = proc.getTargetAnnotationType();
            if (target == null) {
                env.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    MessageFormat.format(
                        "{0}は正しくロードされなかったため、スキップされます",
                        proc.getClass().getName()));
            } else if (results.containsKey(target)) {
                env.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        MessageFormat.format(
                            "{0}の対象演算子{1}はすでに{2}によって対象となっているため、スキップされます",
                            proc.getClass().getName(),
                            target.getName(),
                            results.get(target).getClass().getName()));
            } else {
                results.put(target, proc);
            }
        }
        return Sets.from(results.values());
    }

    /**
     * この注釈プロセッサが内部的に利用する演算子プロセッサの一覧を返す。
     * @param env コンパイラの実行環境
     * @return 演算子プロセッサの一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    protected Iterable<OperatorProcessor> findOperatorProcessors(OperatorCompilingEnvironment env) {
        List<OperatorProcessor> results = Lists.create();
        Iterator<OperatorProcessor> iter = ServiceLoader
            .load(OperatorProcessor.class, env.getServiceClassLoader())
            .iterator();
        while (iter.hasNext()) {
            try {
                results.add(iter.next());
            } catch (RuntimeException e) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR, toDetailString(e));
            }
        }
        return results;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    /**
     * Returns the current supported source version.
     * @return the supported version
     * @since 0.5.3
     */
    public static SourceVersion computeSupportedVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return computeSupportedVersion();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> results = Sets.create();
        for (OperatorProcessor proc : subProcessors) {
            Class<? extends Annotation> type = proc.getTargetAnnotationType();
            results.add(type.getName());
        }
        return results;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
            Element element,
            AnnotationMirror annotation,
            ExecutableElement member,
            String userText) {
        return Collections.emptyList();
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        assert annotations != null;
        assert roundEnv != null;
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            start(roundEnv);
        } catch (OperatorCompilerException e) {
            if (e.getKind() != null) {
                environment.getMessager().printMessage(
                        e.getKind(),
                        e.getMessage());
            }
            LOG.debug(e.getMessage(), e);
        } catch (RuntimeException e) {
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR, toDetailString(e));
        }
        return false;
    }

    private String toDetailString(RuntimeException e) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        pw.println("演算子のコンパイルに失敗しました:");
        e.printStackTrace(pw);
        pw.close();
        return writer.toString();
    }

    private void start(RoundEnvironment roundEnv) {
        assert roundEnv != null;

        // 演算子クラスの情報を収集
        OperatorClassCollector collector = new OperatorClassCollector(environment, roundEnv);
        for (OperatorProcessor proc : subProcessors) {
            collector.add(proc);
        }
        List<OperatorClass> classes = collector.collect();

        // 演算子クラスを出力
        OperatorClassEmitter emitter = new OperatorClassEmitter(environment);
        for (OperatorClass operatorClass : classes) {
            emitter.emit(operatorClass);
        }
    }
}
