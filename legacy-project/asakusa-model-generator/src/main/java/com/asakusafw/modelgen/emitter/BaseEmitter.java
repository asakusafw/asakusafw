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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.modelgen.Constants;
import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.CommentEmitTrait;
import com.asakusafw.utils.java.model.util.Emitter;
import com.asakusafw.utils.java.model.util.Filer;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * 全てのエミッタの基底となるエミッタ。
 * @param <T> 対象とするモデルの種類
 */
public abstract class BaseEmitter<T extends ModelDescription> {

    /**
     * ソースコードを生成するファクトリ。
     */
    protected final ModelFactory f;

    /**
     * 共通部分のエミッタ。
     */
    protected final CommonEmitter common;

    private List<String> headerComment;

    /**
     * インポート宣言を構築する。
     */
    protected ImportBuilder imports;

    private Emitter emitter;

    /**
     * インスタンスを生成する。
     * @param factory ソースコードを生成するファクトリ
     * @param output 出力先のベースディレクトリ
     * @param rootPackageName 出力先のパッケージ名
     * @param headerComment ファイルのヘッダコメント、不要の場合は{@code null}
     */
    public BaseEmitter(
            ModelFactory factory,
            File output,
            String rootPackageName,
            List<String> headerComment) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (rootPackageName == null) {
            throw new IllegalArgumentException("packageName must not be null"); //$NON-NLS-1$
        }
        this.emitter = new Filer(output, Constants.OUTPUT_ENCODING);
        this.f = factory;
        this.common = new CommonEmitter(factory, rootPackageName);
        this.headerComment = headerComment == null ? null : new ArrayList<String>(headerComment);
    }

    /**
     * 指定のモデルを適切なファイルに出力する。
     * @param model 出力するモデル
     * @throws IOException 出力に失敗した場合
     */
    public void emit(T model) throws IOException {
        CompilationUnit source = createSource(model);
        PrintWriter writer = openOutputFor(source);
        try {
            Models.emit(source, writer);
        } finally {
            writer.close();
        }
    }

    /**
     * 対象のモデルに対するソースプログラムを返す。
     * @param model 対象のモデル
     * @return ソースプログラム
     */
    protected CompilationUnit createSource(T model) {

        // パッケージの計算
        PackageDeclaration packageDecl = createPackageDeclaration(model);

        // インポートビルダーの初期化
        imports = new ImportBuilder(f, packageDecl, ImportBuilder.Strategy.TOP_LEVEL);

        // 型宣言の計算
        TypeDeclaration type = createTypeDeclaration(model);

        // コンパイル単位化
        CompilationUnit unit = f.newCompilationUnit(
                packageDecl,
                imports.toImportDeclarations(),
                Collections.singletonList(type),
                Collections.<Comment>emptyList());

        // ヘッダコメントの追加
        if (headerComment != null) {
            unit.putModelTrait(
                    CommentEmitTrait.class,
                    new CommentEmitTrait(headerComment));
        }
        return unit;
    }

    /**
     * 対象のモデルに対するパッケージ宣言を返す。
     * @param model 対象のモデル
     * @return 対応するパッケージ宣言、デフォルトパッケージの場合は{@code null}
     */
    protected abstract PackageDeclaration createPackageDeclaration(T model);

    /**
     * 対象のモデルに対する型の宣言を返す。
     * @param model 対象のモデル
     * @return 対応する型
     */
    protected abstract TypeDeclaration createTypeDeclaration(T model);

    /**
     * 指定のコンパイル単位に対する出力を開く。
     * @param source 対象のコンパイル単位
     * @return 開いた出力
     * @throws IOException 出力先の作成に失敗した場合
     */
    protected PrintWriter openOutputFor(CompilationUnit source) throws IOException {
        return emitter.openFor(source);
    }

    /**
     * 指定の型を現在のコンテキストに結びつける。
     * @param type 対象の型
     * @return 結果の型
     */
    protected Type bless(java.lang.reflect.Type type) {
        return bless(Models.toType(f, type));
    }

    /**
     * 指定の型を現在のコンテキストに結びつける。
     * @param type 対象の型
     * @return 結果の型
     */
    protected Type bless(Type type) {
        return imports.resolve(type);
    }
}
