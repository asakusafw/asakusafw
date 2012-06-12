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
package com.asakusafw.compiler.operator.flow;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.ashigeru.lang.java.jsr269.bridge.Jsr269;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.ImportDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.PackageDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeDeclaration;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder.Strategy;

/**
 * {@link FlowPartClass}をファイルに出力する。
 */
public class FlowClassEmitter {

    static final Logger LOG = LoggerFactory.getLogger(FlowClassEmitter.class);

    private final OperatorCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public FlowClassEmitter(OperatorCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のフロー部品クラスから演算子ファクトリークラスを生成して出力する。
     * @param aClass フロー部品クラス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public void emit(FlowPartClass aClass) {
        assert aClass != null;
        ModelFactory f = environment.getFactory();
        PackageDeclaration packageDecl = getPackage(f, aClass);
        ImportBuilder imports = getImportBuilder(f, packageDecl);
        FlowFactoryClassGenerator generator = new FlowFactoryClassGenerator(
                environment,
                f,
                imports,
                aClass);
        TypeDeclaration type = generator.generate();
        List<ImportDeclaration> decls = imports.toImportDeclarations();

        try {
            emit(f, packageDecl, decls, type);
        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            "{0}に対する演算子ファクトリークラスの作成に失敗しました ({1})",
                            aClass.getElement().getQualifiedName().toString(),
                            e.getMessage()));
        }
    }

    private void emit(
            ModelFactory factory,
            PackageDeclaration packageDecl,
            List<ImportDeclaration> importDecls,
            TypeDeclaration typeDecl) throws IOException {

        CompilationUnit unit = factory.newCompilationUnit(
                packageDecl,
                importDecls,
                Collections.singletonList(typeDecl),
                Collections.<Comment>emptyList());

        environment.emit(unit);
    }

    private PackageDeclaration getPackage(
            ModelFactory factory,
            FlowPartClass aClass) {
        PackageElement parent = (PackageElement) aClass.getElement().getEnclosingElement();
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
