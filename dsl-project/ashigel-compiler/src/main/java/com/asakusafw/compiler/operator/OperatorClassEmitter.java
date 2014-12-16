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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder.Strategy;

/**
 * {@link OperatorClass}をファイルに出力する。
 */
public class OperatorClassEmitter {

    static final Logger LOG = LoggerFactory.getLogger(OperatorClassEmitter.class);

    private static final String KEY_SUFFIX_FACTORY = "FACTORY"; //$NON-NLS-1$

    private static final String KEY_SUFFIX_IMPLEMENTATION = "IMPLEMENTATION"; //$NON-NLS-1$

    private final OperatorCompilingEnvironment environment;

    private boolean sawError;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public OperatorClassEmitter(OperatorCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定の演算子クラスから演算子実装クラスと演算子ファクトリークラスを生成して出力する。
     * @param operatorClass 演算子クラス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public void emit(OperatorClass operatorClass) {
        Precondition.checkMustNotBeNull(operatorClass, "operatorClass"); //$NON-NLS-1$
        emitFactory(operatorClass);
        if (sawError == false) {
            emitImplementation(operatorClass);
        }
    }

    /**
     * これまでの処理でエラーが発生していた場合にのみ{@code true}を返す。
     * @return これまでの処理でエラーが発生していた場合にのみ{@code true}
     */
    public boolean hasError() {
        return sawError;
    }

    private void emitImplementation(OperatorClass operatorClass) {
        assert operatorClass != null;
        String key = getResourceKey(operatorClass, KEY_SUFFIX_IMPLEMENTATION);
        if (key != null && environment.isResourceGenerated(key)) {
            return;
        }
        ModelFactory f = environment.getFactory();
        PackageDeclaration packageDecl = getPackage(f, operatorClass);
        ImportBuilder imports = getImportBuilder(f, packageDecl);
        OperatorClassGenerator generator = new OperatorImplementationClassGenerator(
                environment,
                f,
                imports,
                operatorClass);
        TypeDeclaration type = generator.generate();
        if (type == null) {
            sawError = true;
            return;
        }
        List<ImportDeclaration> decls = imports.toImportDeclarations();

        try {
            emit(key, f, packageDecl, decls, type, operatorClass.getElement());
        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            "{0}に対する演算子実装クラスの作成に失敗しました ({1})",
                            operatorClass.getElement().getQualifiedName().toString(),
                            e.getMessage()));
        }
    }

    private void emitFactory(OperatorClass operatorClass) {
        assert operatorClass != null;
        String key = getResourceKey(operatorClass, KEY_SUFFIX_FACTORY);
        if (key != null && environment.isResourceGenerated(key)) {
            return;
        }
        ModelFactory f = environment.getFactory();
        PackageDeclaration packageDecl = getPackage(f, operatorClass);
        ImportBuilder imports = getImportBuilder(f, packageDecl);
        OperatorClassGenerator generator = new OperatorFactoryClassGenerator(
                environment,
                f,
                imports,
                operatorClass);
        TypeDeclaration type = generator.generate();
        if (type == null) {
            sawError = true;
            return;
        }
        List<ImportDeclaration> decls = imports.toImportDeclarations();

        try {
            emit(key, f, packageDecl, decls, type, operatorClass.getElement());
        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            "{0}に対する演算子ファクトリークラスの作成に失敗しました ({1})",
                            operatorClass.getElement().getQualifiedName().toString(),
                            e.getMessage()));
        }
    }

    private String getResourceKey(OperatorClass operatorClass, String suffix) {
        assert operatorClass != null;
        return String.format("%s::%s", operatorClass.getElement().getQualifiedName(), suffix); //$NON-NLS-1$
    }

    private void emit(
            String key,
            ModelFactory factory,
            PackageDeclaration packageDecl,
            List<ImportDeclaration> importDecls,
            TypeDeclaration typeDecl,
            TypeElement originating) throws IOException {

        CompilationUnit unit = factory.newCompilationUnit(
                packageDecl,
                importDecls,
                Collections.singletonList(typeDecl),
                Collections.<Comment>emptyList());
        try {
            environment.emit(unit, originating);
            if (key != null) {
                environment.setResourceGenerated(key);
            }
        } catch (FilerException e) {
            LOG.debug(MessageFormat.format(
                    "{0} has been already created in this session", //$NON-NLS-1$
                    typeDecl.getName().toNameString()), e);
        }
    }

    private PackageDeclaration getPackage(
            ModelFactory factory,
            OperatorClass operatorClass) {
        PackageElement parent = (PackageElement) operatorClass.getElement().getEnclosingElement();
        return new Jsr269(factory).convert(parent);
    }

    private ImportBuilder getImportBuilder(
            ModelFactory factory,
            PackageDeclaration packageDecl) {
        assert factory != null;
        assert packageDecl != null;
        return new ImportBuilder(factory, packageDecl, Strategy.TOP_LEVEL);
    }
}
