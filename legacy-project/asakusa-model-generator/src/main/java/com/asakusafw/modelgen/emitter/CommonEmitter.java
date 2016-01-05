/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.modelgen.emitter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.Text;

import com.asakusafw.modelgen.Constants;
import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.modelgen.model.ModelReference;
import com.asakusafw.modelgen.model.PropertyType;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.JoinedModel;
import com.asakusafw.vocabulary.model.SummarizedModel;

/**
 * 名前を構築する。
 */
public class CommonEmitter {

    private final ModelFactory f;

    private final Name baseNamespace;

    /**
     * インスタンスを生成する。
     * @param factory 利用するファクトリ
     * @param baseNamespace 基本となるJavaの名前空間
     */
    public CommonEmitter(
            ModelFactory factory,
            String baseNamespace) {
        this.f = factory;
        this.baseNamespace = Models.toName(f, baseNamespace);
    }

    /**
     * 対象のモデルに対する型を返す。
     * @param reference 対象のモデルへの参照
     * @return 対応する型
     */
    public Type getModelType(ModelReference reference) {
        Name pkg = getPackageNameOf(reference, Constants.CATEGORY_MODEL);
        if (pkg == null) {
            return f.newNamedType(getTypeNameOf(reference));
        } else {
            return f.newNamedType(
                    f.newQualifiedName(
                            pkg,
                            getTypeNameOf(reference)));
        }
    }

    /**
     * 対象のプロパティに対する自然な初期値を返す。
     * @param type プロパティの型
     * @param importer インポートを制御するオブジェクト、不要な場合は{@code null}
     * @return 対応する初期値
     */
    public Expression getInitialValue(
            PropertyType type,
            ImportBuilder importer) {
        switch (type.getKind()) {
        case BOOLEAN:
            return newInstance(importer, BooleanOption.class);
        case BYTE:
            return newInstance(importer, ByteOption.class);
        case SHORT:
            return newInstance(importer, ShortOption.class);
        case INT:
            return newInstance(importer, IntOption.class);
        case LONG:
            return newInstance(importer, LongOption.class);
        case BIG_DECIMAL:
            return newInstance(importer, DecimalOption.class); // TODO precise
        case STRING:
            return newInstance(importer, StringOption.class); // TODO size
        case DATE:
            return newInstance(importer, DateOption.class);
        case DATETIME:
            return newInstance(importer, DateTimeOption.class);

        default:
            throw new IllegalStateException(MessageFormat.format(
                    "Unknown Type: {0}",
                    type));
        }
    }

    private Expression newInstance(
            ImportBuilder importer,
            Class<?> klass,
            Expression... arguments) {
        Type type = Models.toType(f, klass);
        if (importer != null) {
            type = importer.resolve(type);
        }
        return types(type).newObject(arguments).toExpression();
    }

    private TypeBuilder types(Type type) {
        assert type != null;
        return new TypeBuilder(f, type);
    }

    /**
     * 対象のプロパティに対する型を返す。
     * @param type プロパティの型
     * @return 対応する型
     */
    public Type getOptionType(PropertyType type) {
        switch (type.getKind()) {
        case BOOLEAN:
            return Models.toType(f, BooleanOption.class);
        case BYTE:
            return Models.toType(f, ByteOption.class);
        case SHORT:
            return Models.toType(f, ShortOption.class);
        case INT:
            return Models.toType(f, IntOption.class);
        case LONG:
            return Models.toType(f, LongOption.class);
        case BIG_DECIMAL:
            return Models.toType(f, DecimalOption.class);
        case STRING:
            return Models.toType(f, StringOption.class);
        case DATE:
            return Models.toType(f, DateOption.class);
        case DATETIME:
            return Models.toType(f, DateTimeOption.class);

        default:
            throw new IllegalStateException(MessageFormat.format(
                    "Unknown Type: {0}",
                    type));
        }
    }

    /**
     * 対象のプロパティに対する型を返す。
     * @param type プロパティの型
     * @return 対応する型
     */
    public Type getRawType(PropertyType type) {
        switch (type.getKind()) {
        case BOOLEAN:
            return Models.toType(f, boolean.class);
        case BYTE:
            return Models.toType(f, byte.class);
        case SHORT:
            return Models.toType(f, short.class);
        case INT:
            return Models.toType(f, int.class);
        case LONG:
            return Models.toType(f, long.class);
        case BIG_DECIMAL:
            return Models.toType(f, BigDecimal.class);
        case STRING:
            return Models.toType(f, Text.class);
        case DATE:
            return Models.toType(f, Date.class);
        case DATETIME:
            return Models.toType(f, DateTime.class);

        default:
            throw new IllegalStateException(MessageFormat.format(
                    "Unknown Type: {0}",
                    type));
        }
    }

    /**
     * 対象のプロパティに対する型を返す。
     * @param type プロパティの型
     * @return 対応する型
     */
    public Type getAltType(PropertyType type) {
        switch (type.getKind()) {
        case STRING:
            return Models.toType(f, String.class);

        case BOOLEAN:
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
        case BIG_DECIMAL:
        case DATE:
        case DATETIME:
            return null;

        default:
            throw new IllegalStateException(MessageFormat.format(
                    "Unknown Type: {0}",
                    type));
        }
    }

    /**
     * 対象のモデルに対する型の単純名を返す。
     * @param reference 対象のモデル
     * @param categoryName 対象のカテゴリ名
     * @return 対応する名前
     */
    public Name getPackageNameOf(ModelReference reference, String categoryName) {
        // ルートパッケージ: com.example
        // ネームスペース: hoge
        // カテゴリネーム: model
        // -> com.example.hoge.model
        if (reference.isDefaultNameSpace()) {
            return Models.append(f, baseNamespace, categoryName);
        }
        return Models.append(f,
                baseNamespace,
                Models.toName(f, reference.getNamespace()),
                Models.toName(f, categoryName));
    }

    /**
     * 対象のモデルに対する型の単純名を返す。
     * @param reference 対象のモデル
     * @return 対応する名前
     */
    public SimpleName getTypeNameOf(ModelReference reference) {
        JavaName name = JavaName.of(reference.getSimpleName());
        return f.newSimpleName(name.toTypeName());
    }

    /**
     * 対象のプロパティに対するフィールド名を返す。
     * @param propertyName 対象のプロパティ名
     * @param type 対象プロパティの型
     * @return 対応する名前
     */
    public SimpleName getFieldNameOf(String propertyName, PropertyType type) {
        JavaName name = JavaName.of(propertyName);
        return f.newSimpleName(name.toMemberName());
    }

    /**
     * 対象のプロパティに対するゲッター名を返す。
     * @param propertyName 対象のプロパティ名
     * @param type 対象プロパティの型
     * @return 対応する名前
     */
    public SimpleName getGetterNameOf(String propertyName, PropertyType type) {
        JavaName name = JavaName.of(propertyName);
        switch (type.getKind()) {
        case BOOLEAN:
            name.addFirst("is");
            return f.newSimpleName(name.toMemberName());
        default:
            name.addFirst("get");
            return f.newSimpleName(name.toMemberName());
        }
    }

    /**
     * 対象のプロパティに対するセッター名を返す。
     * @param propertyName 対象のプロパティ名
     * @param type 対象プロパティの型
     * @return 対応する名前
     */
    public SimpleName getSetterNameOf(String propertyName, PropertyType type) {
        JavaName name = JavaName.of(propertyName);
        switch (type.getKind()) {
        default:
            name.addFirst("set");
            return f.newSimpleName(name.toMemberName());
        }
    }

    /**
     * 対象のプロパティに対する代替のゲッター名を返す。
     * @param propertyName 対象のプロパティ名
     * @param type 対象プロパティの型
     * @return 対応する名前、存在しない場合は{@code null}
     */
    public SimpleName getAltGetterNameOf(String propertyName, PropertyType type) {
        String original = getGetterNameOf(propertyName, type).getToken();
        return toAltMemberName(original, type);
    }

    /**
     * 対象のプロパティに対する代替のセッター名を返す。
     * @param propertyName 対象のプロパティ名
     * @param type 対象プロパティの型
     * @return 対応する名前、存在しない場合は{@code null}
     */
    public SimpleName getAltSetterNameOf(String propertyName, PropertyType type) {
        String original = getSetterNameOf(propertyName, type).getToken();
        return toAltMemberName(original, type);
    }

    /**
     * 対象の名前に対する代替のメンバー名を返す。
     * @param original 元の名前
     * @param type メンバーの型
     * @return 代替のメンバー名、存在しない場合は{@code null}
     */
    public SimpleName toAltMemberName(String original, PropertyType type) {
        JavaName name = JavaName.of(original);
        switch (type.getKind()) {
        case STRING:
            name.addLast("as");
            name.addLast("string");
            return f.newSimpleName(name.toMemberName());

        case BOOLEAN:
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
        case BIG_DECIMAL:
        case DATE:
        case DATETIME:
            return null;

        default:
            throw new IllegalStateException(MessageFormat.format(
                    "Unknown Type: {0}",
                    type));
        }
    }

    /**
     * 対象のプロパティに対するオプションオブジェクトのゲッター名を返す。
     * @param propertyName 対象のプロパティ名
     * @param type 対象プロパティの型
     * @return 対応する名前
     */
    public SimpleName getOptionGetterNameOf(
            String propertyName,
            PropertyType type) {
        JavaName name = JavaName.of(propertyName);
        name.addFirst("get");
        name.addLast("option");
        return f.newSimpleName(name.toMemberName());
    }

    /**
     * 対象のプロパティに対するオプションオブジェクトのゲッター名を返す。
     * @param propertyName 対象のプロパティ名
     * @param type 対象プロパティの型
     * @return 対応する名前
     */
    public SimpleName getOptionSetterNameOf(
            String propertyName,
            PropertyType type) {
        JavaName name = JavaName.of(propertyName);
        name.addFirst("set");
        name.addLast("option");
        return f.newSimpleName(name.toMemberName());
    }

    /**
     * 他のオブジェクトの内容を取り込むメソッドの名前を返す。
     * @return 対応する名前
     */
    public SimpleName getCopierName() {
        return f.newSimpleName(DataModel.Interface.METHOD_NAME_COPY_FROM);
    }

    /**
     * 結合メソッドの名前を返す。
     * @return 対応する名前
     */
    public SimpleName getJoinerName() {
        return f.newSimpleName(JoinedModel.Interface.METHOD_NAME_JOIN_FROM);
    }

    /**
     * 分割メソッドの名前を返す。
     * @return 対応する名前
     */
    public SimpleName getSplitterName() {
        return f.newSimpleName(JoinedModel.Interface.METHOD_NAME_SPLIT_INTO);
    }

    /**
     * 集計開始メソッドの名前を返す。
     * @return 対応する名前
     */
    public SimpleName getStartSummarizerName() {
        return f.newSimpleName(SummarizedModel.Interface.METHOD_NAME_START_SUMMARIZATION);
    }

    /**
     * 集計追加メソッドの名前を返す。
     * @return 対応する名前
     */
    public SimpleName getCombineSummarizerName() {
        return f.newSimpleName(SummarizedModel.Interface.METHOD_NAME_COMBINE_SUMMARIZATION);
    }

    /**
     * フィールド名と衝突しない変数名を返す。
     * @param model 対象のモデル
     * @param hint 変数名のヒント
     * @return 対応する名前
     */
    public SimpleName getVariableNameOf(ModelDescription model, String hint) {
        Set<String> used = new HashSet<String>();
        for (ModelProperty p : model.getProperties()) {
            used.add(getFieldNameOf(p.getName(), p.getType()).getToken());
        }
        StringBuilder name = new StringBuilder(hint);
        while (used.contains(name.toString())) {
            name.append('_');
        }
        return f.newSimpleName(name.toString());
    }
}

