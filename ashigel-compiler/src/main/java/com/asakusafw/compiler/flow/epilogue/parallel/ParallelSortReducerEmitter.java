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
package com.asakusafw.compiler.flow.epilogue.parallel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.stage.CompiledType;
import com.asakusafw.runtime.stage.collector.SlotSorter;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Name;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.syntax.TypeDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder.Strategy;
import com.ashigeru.lang.java.model.util.JavadocBuilder;
import com.ashigeru.lang.java.model.util.Models;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * parallel reduceを行うレデューサークラスを出力ごとに生成する。
 */
public class ParallelSortReducerEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ParallelSortReducerEmitter.class);

    private FlowCompilingEnvironment environment;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ParallelSortReducerEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * 指定のスロットに対するエピローグ用のレデューサーを生成する。
     * @param moduleId モジュール識別子
     * @param slots 対象のスロット一覧
     * @return 生成したクラス
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public CompiledType emit(String moduleId, List<ResolvedSlot> slots) throws IOException {
        LOG.debug("\"{}\"エピローグ用のレデューサーを生成します", moduleId);
        Engine engine = new Engine(environment, moduleId, slots);
        CompilationUnit source = engine.generate();
        environment.emit(source);
        Name packageName = source.getPackageDeclaration().getName();
        SimpleName simpleName = source.getTypeDeclarations().get(0).getName();
        Name name = environment.getModelFactory().newQualifiedName(packageName, simpleName);
        LOG.debug("エピローグ用レデューサーには{}が利用されます", name);
        return new CompiledType(name);
    }

    private static class Engine {

        private List<ResolvedSlot> slots;

        private ModelFactory factory;

        private ImportBuilder importer;

        Engine(FlowCompilingEnvironment envinronment, String moduleId, List<ResolvedSlot> slots) {
            assert envinronment != null;
            assert moduleId != null;
            assert slots != null;
            this.slots = slots;
            this.factory = envinronment.getModelFactory();
            this.importer = new ImportBuilder(
                    factory,
                    factory.newPackageDeclaration(envinronment.getEpiloguePackageName(moduleId)),
                    Strategy.TOP_LEVEL);
        }

        public CompilationUnit generate() {
            TypeDeclaration type = createType();
            return factory.newCompilationUnit(
                    importer.getPackageDeclaration(),
                    importer.toImportDeclarations(),
                    Collections.singletonList(type),
                    Collections.<Comment>emptyList());
        }

        private TypeDeclaration createType() {
            SimpleName name = factory.newSimpleName(Naming.getReduceClass());
            importer.resolvePackageMember(name);
            return factory.newClassDeclaration(
                    new JavadocBuilder(factory)
                        .text("エピローグ用のレデューサー。")
                        .toJavadoc(),
                    new AttributeBuilder(factory)
                        .Public()
                        .toAttributes(),
                    name,
                    Collections.<TypeParameterDeclaration>emptyList(),
                    importer.toType(SlotSorter.class),
                    Collections.<Type>emptyList(),
                    Arrays.asList(createSlotNames(), createSlotObjects()));
        }

        private MethodDeclaration createSlotNames() {
            SimpleName resultName = factory.newSimpleName("results");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new TypeBuilder(factory, importer.toType(String.class))
                .array(1)
                .newArray(getSlotCount())
                .toLocalVariableDeclaration(
                        importer.toType(String[].class),
                        resultName));
            for (ResolvedSlot slot : slots) {
                statements.add(new ExpressionBuilder(factory, resultName)
                    .array(slot.getSlotNumber())
                    .assignFrom(Models.toLiteral(factory, slot.getSource().getOutputName()))
                    .toStatement());
            }
            statements.add(new ExpressionBuilder(factory, resultName)
                .toReturnStatement());
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(importer.toType(Override.class))
                        .Protected()
                        .toAttributes(),
                    importer.toType(String[].class),
                    factory.newSimpleName(SlotSorter.NAME_GET_OUTPUT_NAMES),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private MethodDeclaration createSlotObjects() {
            SimpleName resultName = factory.newSimpleName("results");
            List<Statement> statements = new ArrayList<Statement>();
            statements.add(new TypeBuilder(factory, importer.toType(Writable.class))
                .array(1)
                .newArray(getSlotCount())
                .toLocalVariableDeclaration(
                        importer.toType(Writable[].class),
                        resultName));
            for (ResolvedSlot slot : slots) {
                DataClass slotClass = slot.getValueClass();
                statements.add(new ExpressionBuilder(factory, resultName)
                    .array(slot.getSlotNumber())
                    .assignFrom(slotClass.createNewInstance(
                            importer.toType(slotClass.getType())))
                    .toStatement());
            }
            statements.add(new ExpressionBuilder(factory, resultName)
                .toReturnStatement());
            return factory.newMethodDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .annotation(importer.toType(Override.class))
                        .Protected()
                        .toAttributes(),
                    importer.toType(Writable[].class),
                    factory.newSimpleName(SlotSorter.NAME_CREATE_SLOT_OBJECTS),
                    Collections.<FormalParameterDeclaration>emptyList(),
                    statements);
        }

        private int getSlotCount() {
            int max = 0;
            for (ResolvedSlot slot : slots) {
                max = Math.max(max, slot.getSlotNumber() + 1);
            }
            return max;
        }
    }
}
