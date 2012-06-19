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
package com.asakusafw.compiler.flow;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.flow.ArrayListBuffer;
import com.asakusafw.runtime.flow.FileMapListBuffer;
import com.asakusafw.runtime.flow.ListBuffer;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.processor.InputBuffer;

/**
 * フロー要素を処理するプロセッサ。
 * <p>
 * このインターフェースを直接実装すべきでない。
 * </p>
 */
public interface FlowElementProcessor extends FlowCompilingEnvironment.Initializable {

    /**
     * 結果オブジェクトに結果を追加する際のメソッド名。
     */
    String RESULT_METHOD_NAME = "add";

    /**
     * このプロセッサの種類を返す。
     * @return このプロセッサの種類
     */
    FlowElementProcessor.Kind getKind();

    /**
     * このプロセッサが対象とする注釈の型を返す。
     * @return このプロセッサが対象とする注釈の型
     */
    Class<? extends Annotation> getTargetAnnotationType();

    /**
     * 処理の文脈の基底となるクラス。
     */
    public abstract static class AbstractProcessorContext {

        /**
         * コンパイル環境。
         */
        protected final FlowCompilingEnvironment environment;

        /**
         * Javaの構造を表すモデルオブジェクトを生成する。
         */
        protected final ModelFactory factory;

        /**
         * インポート宣言を行う。
         */
        protected final ImportBuilder importer;

        /**
         * 名前を生成する。
         */
        protected final NameGenerator names;

        /**
         * 処理対象の演算子の定義記述。
         */
        protected final OperatorDescription description;

        /**
         * リソースと式の対応表。
         */
        protected final Map<FlowResourceDescription, Expression> resources;

        /**
         * 生成されたフィールドの一覧。
         */
        protected final List<FieldDeclaration> generatedFields;

        /**
         * インスタンスを生成する。
         * @param environment 環境
         * @param importer インポート
         * @param names 名前生成
         * @param desc 演算子の定義記述
         * @param resources リソースと式の対応表
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public AbstractProcessorContext(
                FlowCompilingEnvironment environment,
                ImportBuilder importer,
                NameGenerator names,
                OperatorDescription desc,
                Map<FlowResourceDescription, Expression> resources) {
            Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(names, "names"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(desc, "desc"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(resources, "resources"); //$NON-NLS-1$
            this.environment = environment;
            this.factory = environment.getModelFactory();
            this.importer = importer;
            this.names = names;
            this.description = desc;
            this.resources = resources;
            this.generatedFields = Lists.create();
        }

        /**
         * 処理中の演算子の定義記述を返す。
         * @return 処理中の演算子の定義記述
         */
        public OperatorDescription getOperatorDescription() {
            return description;
        }

        /**
         * 指定の番号に割り振られた入力ポートの定義記述を返す。
         * @param portNumber 対象のポート番号
         * @return 入力ポートの定義記述
         * @throws NoSuchElementException 指定のポートが見つからない場合
         * @throws IllegalArgumentException 存在しないポート番号が指定され他場合
         */
        public FlowElementPortDescription getInputPort(int portNumber) {
            if (portNumber < 0 || portNumber >= description.getInputPorts().size()) {
                throw new IllegalArgumentException("invalid port number"); //$NON-NLS-1$
            }
            return description.getInputPorts().get(portNumber);
        }

        /**
         * 指定の番号に割り振られた出力ポートの定義記述を返す。
         * @param portNumber 対象のポート番号
         * @return 出力ポートの定義記述
         * @throws NoSuchElementException 指定のポートが見つからない場合
         * @throws IllegalArgumentException 存在しないポート番号が指定され他場合
         */
        public FlowElementPortDescription getOutputPort(int portNumber) {
            if (portNumber < 0 || portNumber >= description.getOutputPorts().size()) {
                throw new IllegalArgumentException("invalid port number"); //$NON-NLS-1$
            }
            return description.getOutputPorts().get(portNumber);
        }

        /**
         * 指定の番号に割り振られたリソースを表すオブジェクトを返す。
         * @param resourceNumber 対象のリソース番号
         * @return リソースを表す式
         * @throws IllegalArgumentException 存在しないリソース番号が指定された場合
         */
        public FlowResourceDescription getResourceDescription(int resourceNumber) {
            if (resourceNumber < 0 || resourceNumber >= description.getResources().size()) {
                throw new IllegalArgumentException("invalid resource number"); //$NON-NLS-1$
            }
            FlowResourceDescription resource = description.getResources().get(resourceNumber);
            return resource;
        }

        /**
         * 指定のリソースを表す式を返す。
         * @param resource 対象のリソース
         * @return リソースを表す式
         * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
         */
        public Expression getResource(FlowResourceDescription resource) {
            Precondition.checkMustNotBeNull(resource, "resource"); //$NON-NLS-1$
            Expression expression = resources.get(resource);
            assert expression != null;
            return expression;
        }

        /**
         * JavaのDOM構造を構築するためのファクトリーを返す。
         * @return DOM構造を構築するためのファクトリー
         */
        public ModelFactory getModelFactory() {
            return factory;
        }

        private Expression addField(Type type, String name, Expression init) {
            assert type != null;
            assert name != null;
            SimpleName fieldName = createName(name);
            FieldDeclaration field = factory.newFieldDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .toAttributes(),
                    type,
                    fieldName,
                    init);
            generatedFields.add(field);
            return factory.newFieldAccessExpression(
                    factory.newThis(),
                    fieldName);
        }

        /**
         * ここまでにこの文脈で生成されたフィールド宣言の一覧を返す。
         * @return この文脈で生成されたフィールド宣言の一覧
         */
        public List<FieldDeclaration> getGeneratedFields() {
            return generatedFields;
        }

        /**
         * 指定のヒント名を含む衝突しない新しい名前を返す。
         * @param hint ヒント名
         * @return 衝突しない新しい名前
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public SimpleName createName(String hint) {
            Precondition.checkMustNotBeNull(hint, "hint"); //$NON-NLS-1$
            return names.create(hint);
        }

        /**
         * 演算子実装クラスのインスタンスを生成し、それを参照するための式を返す。
         * @return 生成した式
         */
        public Expression createImplementation() {
            Class<?> implementing = description.getDeclaration().getImplementing();
            Type type = convert(implementing);
            return addField(type, "op", new TypeBuilder(factory, type)
                .newObject()
                .toExpression());
        }

        /**
         * 任意の型を持つフィールドを生成する。
         * @param type 対象の型
         * @param name 名前のヒント
         * @return 生成したフィールドを参照するための式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Expression createField(java.lang.reflect.Type type, String name) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
            return addField(
                    importer.toType(type),
                    name,
                    null);
        }

        /**
         * モデルのキャッシュインスタンスを生成し、それを参照するための式を返す。
         * @param type モデルの型
         * @return 生成した式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public DataObjectMirror createModelCache(java.lang.reflect.Type type) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            DataClass data = environment.getDataClasses().load(type);
            if (data == null) {
                environment.error("{0}のデータモデルを解析できませんでした", type);
                data = new DataClass.Unresolved(factory, type);
            }
            Type domType = importer.toType(type);
            Expression cache = addField(
                    domType,
                    "cache",
                    data.createNewInstance(domType));
            return new DataObjectMirror(factory, cache, data);
        }

        /**
         * {@link ListBuffer}のインスタンスを生成し、それを参照するための式を返す。
         * @param type リストの要素型
         * @param bufferKind the input buffer kind
         * @return 生成した式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ListBufferMirror createListBuffer(java.lang.reflect.Type type, InputBuffer bufferKind) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(bufferKind, "bufferKind"); //$NON-NLS-1$
            Type elementType = importer.toType(type);
            Class<?> bufferType = inputBufferTypeFromKind(bufferKind);
            Type listType = importer.resolve(factory.newParameterizedType(
                    Models.toType(factory, bufferType),
                    Collections.singletonList(elementType)));
            Expression list = addField(
                    listType,
                    "list",
                    new TypeBuilder(factory, listType)
                        .newObject()
                        .toExpression());
            DataClass component = environment.getDataClasses().load(type);
            if (component == null) {
                environment.error("{0}のデータモデルを解析できませんでした", type);
                component = new DataClass.Unresolved(factory, type);
            }
            return new ListBufferMirror(factory, list, component, elementType);
        }

        private Class<?> inputBufferTypeFromKind(InputBuffer kind) {
            assert kind != null;
            switch (kind) {
            case EXPAND:
                return ArrayListBuffer.class;
            case ESCAPE:
                return FileMapListBuffer.class;
            default:
                throw new AssertionError(kind);
            }
        }

        /**
         * 指定の型をインポートし、JavaのDOMの表現に変換して返す。
         * @param type 対象の型
         * @return 変換後の型
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Type convert(java.lang.reflect.Type type) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            return importer.toType(type);
        }

        /**
         * 指定の型をインポートして返す。
         * @param type 対象の型
         * @return 変換後の型
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Type simplify(Type type) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            return importer.resolve(type);
        }
    }

    /**
     * データオブジェクトを操作するためのミラー。
     */
    public static class DataObjectMirror {

        private final Expression object;

        private final DataClass dataClass;

        /**
         * インスタンスを生成する。
         * @param factory ファクトリ
         * @param object データオブジェクトを参照するための式
         * @param dataClass データオブジェクトの型
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public DataObjectMirror(
                ModelFactory factory,
                Expression object,
                DataClass dataClass) {
            Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(dataClass, "dataClass"); //$NON-NLS-1$
            this.object = object;
            this.dataClass = dataClass;
        }

        /**
         * 操作対象のオブジェクトを表す式を返す。
         * @return 操作対象のオブジェクトを表す式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Expression get() {
            return object;
        }

        /**
         * このデータオブジェクトに、別のデータオブジェクトの内容を設定する文を返す。
         * @param value 別のデータオブジェクトを表す式
         * @return 生成した文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Statement createSet(Expression value) {
            Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
            return dataClass.assign(object, value);
        }

        /**
         * このデータオブジェクトの内容を消去する文を返す。
         * @return 生成した文
         */
        public Statement createReset() {
            return dataClass.reset(object);
        }
    }

    /**
     * 結果オブジェクトを操作するミラー。
     */
    public static class ResultMirror {

        private final ModelFactory factory;

        private final Expression object;

        /**
         * インスタンスを生成する。
         * @param factory ファクトリ
         * @param object 結果オブジェクトを参照するための式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ResultMirror(ModelFactory factory, Expression object) {
            Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
            this.factory = factory;
            this.object = object;
        }

        /**
         * 操作対象のオブジェクトを表す式を返す。
         * @return 操作対象のオブジェクトを表す式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Expression get() {
            return object;
        }

        /**
         * この結果オブジェクトに、指定の式を追加する文を返す。
         * @param value 追加する式
         * @return 生成した文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Statement createAdd(Expression value) {
            Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
            return new ExpressionBuilder(factory, object)
                .method(RESULT_METHOD_NAME, value)
                .toStatement();
        }
    }

    /**
     * {@link ListBuffer}を操作するミラー。
     */
    public static class ListBufferMirror {

        private static final String BEGIN = "begin";

        private static final String ADVANCE = "advance";

        private static final String END = "end";

        private static final String EXPAND = "expand";

        private static final String IS_EXPAND_REQUIRED = "isExpandRequired";

        private static final String SHRINK = "shrink";

        private final ModelFactory factory;

        private final Expression object;

        private final DataClass dataClass;

        private final Type elementType;

        /**
         * インスタンスを生成する。
         * @param factory ファクトリ
         * @param object 結果オブジェクトを参照するための式
         * @param dataClass リスト要素のデータ型
         * @param elementType リスト要素のDOMでの型表現
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public ListBufferMirror(
                ModelFactory factory,
                Expression object,
                DataClass dataClass,
                Type elementType) {
            Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(dataClass, "dataClass"); //$NON-NLS-1$
            this.factory = factory;
            this.object = object;
            this.dataClass = dataClass;
            this.elementType = elementType;
        }

        /**
         * 操作対象のオブジェクトを表す式を返す。
         * @return 操作対象のオブジェクトを表す式
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Expression get() {
            return object;
        }

        /**
         * {@link ListBuffer}の開始処理を行う文を返す。
         * @return 生成した文
         * @see ListBuffer#begin()
         */
        public Statement createBegin() {
            return new ExpressionBuilder(factory, object)
                .method(BEGIN)
                .toStatement();
        }

        /**
         * {@link ListBuffer}へのデータ追加処理を行う文を返す。
         * <p>
         * バッファの拡張などは自動的に行う。
         * </p>
         * @param value 追加するデータを表す式
         * @return 生成した文
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         * @see ListBuffer#advance()
         * @see ListBuffer#expand(Object)
         * @see ListBuffer#isExpandRequired()
         */
        public Statement createAdvance(Expression value) {
            Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
            List<Statement> thenBlock = Arrays.asList(new Statement[] {
                    new ExpressionBuilder(factory, object)
                        .method(EXPAND, dataClass.createNewInstance(elementType))
                        .toStatement(),
                    dataClass.assign(
                            new ExpressionBuilder(factory, object)
                                .method(ADVANCE)
                                .toExpression(),
                            value),
            });
            List<Statement> elseBlock = Arrays.asList(new Statement[] {
                    dataClass.assign(
                            new ExpressionBuilder(factory, object)
                                .method(ADVANCE)
                                .toExpression(),
                            value),
            });
            return factory.newIfStatement(
                    new ExpressionBuilder(factory, object)
                        .method(IS_EXPAND_REQUIRED)
                        .toExpression(),
                    factory.newBlock(thenBlock),
                    factory.newBlock(elseBlock));
        }

        /**
         * {@link ListBuffer}の更新終了処理を行う文を返す。
         * @return 生成した文
         * @see ListBuffer#end()
         */
        public Statement createEnd() {
            return new ExpressionBuilder(factory, object)
                .method(END)
                .toStatement();
        }

        /**
         * {@link ListBuffer}の参照終了処理を行う文を返す。
         * @return 生成した文
         * @see ListBuffer#end()
         */
        public Statement createShrink() {
            return new ExpressionBuilder(factory, object)
                .method(SHRINK)
                .toStatement();
        }
    }

    /**
     * {@link FlowElementProcessor}を取得するためのリポジトリ。
     */
    interface Repository extends FlowCompilingEnvironment.Initializable {

        /**
         * 空要素に対するプロセッサを返す。
         * @return 空要素に対するプロセッサ
         */
        LinePartProcessor getEmptyProcessor();

        /**
         * 指定の要素に関するプロセッサを返す。
         * @param description 対象の要素記述
         * @return 対応するプロセッサ、存在しない場合は{@code null}
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        FlowElementProcessor findProcessor(FlowElementDescription description);

        /**
         * 指定のライン要素に関するプロセッサを返す。
         * @param description 対象の要素記述
         * @return 対応するプロセッサ、存在しない場合は{@code null}
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        LineProcessor findLineProcessor(FlowElementDescription description);

        /**
         * 指定の合流要素に関するプロセッサを返す。
         * @param description 対象の要素記述
         * @return 対応するプロセッサ、存在しない場合は{@code null}
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        RendezvousProcessor findRendezvousProcessor(FlowElementDescription description);
    }

    /**
     * プロセッサの種類。
     */
    enum Kind {

        /**
         * {@link LinePartProcessor}として利用される。
         */
        LINE_PART,

        /**
         * {@link LineEndProcessor}として利用される。
         */
        LINE_END,

        /**
         * {@link RendezvousProcessor}として利用される。
         */
        RENDEZVOUS,
    }

}
