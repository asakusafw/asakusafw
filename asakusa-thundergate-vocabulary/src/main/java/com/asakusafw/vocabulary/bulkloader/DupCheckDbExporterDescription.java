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
package com.asakusafw.vocabulary.bulkloader;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.asakusafw.vocabulary.model.TableModel;


/**
 * 重複検査付きのDBへのエクスポーターの処理内容を記述するクラスの基底。
 * <p>
 * この記述を利用した出力は、{@link #getModelType()}と同じ型でなければならない。
 * ただし、実際にエクスポートされる先は
 * {@link #getNormalModelType()}, {@link #getErrorModelType()}
 * に指定したモデルに対応するテーブルである。
 * </p>
 * <p>
 * サブクラスでは以下のメソッドを継承して、エクスポーターの処理方法を記述する。
 * </p>
 * <ul>
 * <li> {@link #getTableName()} 接続先データベースを表す識別子 </li>
 * <li>
 *   {@link #getModelType()} 重複チェック成功時と失敗時のカラムをすべて持つテーブルのモデル、
 *   ただし、{@link #getErrorCodeColumnName() エラーコードを格納するカラム名}を含める必要はない。
 * </li>
 * <li> {@link #getNormalModelType()} 重複チェック成功時にレコードを挿入するテーブルのモデル </li>
 * <li> {@link #getErrorModelType()} 重複チェック失敗時にレコードを挿入するテーブルのモデル </li>
 * <li> {@link #getCheckColumnNames()} 重複チェックに利用するカラム名の一覧 </li>
 * <li> {@link #getErrorCodeValue()} 重複チェック失敗時のエラーコード </li>
 * <li> {@link #getErrorColumnNames()} エラーコードを格納するカラム名 </li>
 * </ul>
 * <p>
 * このクラスを継承するクラスは次のような要件を満たす必要がある。
 * </p>
 * <ul>
 * <li> {@code public}で宣言されている </li>
 * <li> {@code abstract}で宣言されていない </li>
 * <li> 型引数が宣言されていない </li>
 * <li> 明示的なコンストラクターが宣言されていない </li>
 * <li>
 *   {@link #getModelType()}に含まれるすべてのプロパティが
 *   {@link #getErrorModelType()}にも同じ型で含まれる
 * </li>
 * <li>
 *   {@link #getModelType()}に含まれるすべてのプロパティが
 *   {@link #getNormalModelType()}にも同じ型で含まれる
 * </li>
 * <li> {@link #getErrorCodeColumnName()}で指定したカラムは文字列型である </li>
 * <li>
 *   {@link #getErrorCodeValue()}で指定した内容は文字列である(ただし、クウォートする必要はない)
 * </li>
 * </ul>
 */
public abstract class DupCheckDbExporterDescription extends BulkLoadExporterDescription {

    /**
     * 重複チェックに成功した際に出力する先のテーブルモデルクラスを返す。
     * <p>
     * なお、指定したクラスが持つすべてのプロパティは、
     * {@link #getErrorModelType()}にも含まれていなければならない。
     * </p>
     * @return 重複チェックに成功した際に出力する先のテーブルモデルクラス
     */
    protected abstract Class<?> getNormalModelType();

    /**
     * 重複チェックに失敗した際に出力する先のテーブルモデルクラスを返す。
     * @return 重複チェックに失敗した際に出力する先のテーブルモデルクラス
     */
    protected abstract Class<?> getErrorModelType();

    /**
     * 重複チェックに利用するカラム名の一覧を返す。
     * @return 重複チェックに利用するカラム名の一覧
     */
    protected abstract List<String> getCheckColumnNames();

    /**
     * 重複チェックに失敗した際に、エラーコードを格納するカラム名を返す。
     * @return 重複チェックに失敗した際に、エラーコードを格納するカラム名
     */
    protected abstract String getErrorCodeColumnName();

    /**
     * 重複チェックに失敗した際のエラーコードを返す。
     * @return 重複チェックに失敗した際のエラーコード
     */
    protected abstract String getErrorCodeValue();

    /**
     * {@inheritDoc}
     * <p>
     * この実装では、テーブル名をモデルクラスの注釈から自動的に抽出する。
     * 注釈が指定されていない場合は例外がスローされる。
     * </p>
     */
    @Override
    public String getTableName() {
        return getNormalTableName();
    }

    /**
     * 重複チェックに成功した際に、エクスポート対象となるテーブルの名称を返す。
     * <p>
     * この実装では、テーブル名をモデルクラスの注釈から自動的に抽出する。
     * 注釈が指定されていない場合は例外がスローされる。
     * </p>
     * @return 重複チェックに成功した際に、エクスポート対象となるテーブルの名称
     */
    protected String getNormalTableName() {
        TableModel meta = getNormalModelType().getAnnotation(TableModel.class);
        if (meta == null) {
            throw new UnsupportedOperationException(MessageFormat.format(
                    "クラス{0}には@{1}の指定がないため、テーブル名を自動的に判別できませんでした。{2}#{3}()をオーバーライドして明示的に指定して下さい",
                    getClass().getName(),
                    TableModel.class.getSimpleName(),
                    DupCheckDbExporterDescription.class.getSimpleName(),
                    "getNormalTableName"));
        }
        return meta.name();
    }

    /**
     * 重複チェックに失敗した際に、エクスポート対象となるテーブルの名称を返す。
     * <p>
     * この実装では、テーブル名をモデルクラスの注釈から自動的に抽出する。
     * 注釈が指定されていない場合は例外がスローされる。
     * </p>
     * @return 重複チェックに失敗した際に、エクスポート対象となるテーブルの名称
     */
    protected String getErrorTableName() {
        TableModel meta = getErrorModelType().getAnnotation(TableModel.class);
        if (meta == null) {
            throw new UnsupportedOperationException(MessageFormat.format(
                    "クラス{0}には@{1}の指定がないため、テーブル名を自動的に判別できませんでした。{2}#{3}()をオーバーライドして明示的に指定して下さい",
                    getClass().getName(),
                    TableModel.class.getSimpleName(),
                    DupCheckDbExporterDescription.class.getSimpleName(),
                    "getErrorTableName"));
        }
        return meta.name();
    }

    /**
     * {@inheritDoc}
     * <p>
     * この実装では、カラム名をモデルクラスの注釈から自動的に抽出する。
     * 注釈が指定されていない場合は例外がスローされる。
     * </p>
     */
    @Override
    public final List<String> getColumnNames() {
        TableModel meta = getModelType().getAnnotation(TableModel.class);
        if (meta == null) {
            throw new UnsupportedOperationException(MessageFormat.format(
                    "クラス{0}には@{1}の指定がないため、カラム名を自動的に判別できませんでした。{2}#{3}()をオーバーライドして明示的に指定して下さい",
                    getClass().getName(),
                    TableModel.class.getSimpleName(),
                    DupCheckDbExporterDescription.class.getSimpleName(),
                    "getColumnNames"));
        }
        return Arrays.asList(meta.columns());
    }

    /**
     * {@inheritDoc}
     * <p>
     * この実装では、カラム名をモデルクラスの注釈から自動的に抽出する。
     * 注釈が指定されていない場合は例外がスローされる。
     * </p>
     */
    @Override
    public List<String> getTargetColumnNames() {
        TableModel meta = getNormalModelType().getAnnotation(TableModel.class);
        if (meta == null) {
            throw new UnsupportedOperationException(MessageFormat.format(
                    "クラス{0}には@{1}の指定がないため、カラム名を自動的に判別できませんでした。{2}#{3}()をオーバーライドして明示的に指定して下さい",
                    getClass().getName(),
                    TableModel.class.getSimpleName(),
                    DupCheckDbExporterDescription.class.getSimpleName(),
                    "getNormalColumnNames"));
        }
        return Arrays.asList(meta.columns());
    }

    /**
     * 重複チェックに成功した際に、エクスポート対象となるカラム名の一覧を返す。
     * <p>
     * この実装では、カラム名をモデルクラスの注釈から自動的に抽出する。
     * 注釈が指定されていない場合は例外がスローされる。
     * </p>
     * @return 重複チェックに成功した際に、エクスポート対象となるカラム名の一覧
     */
    protected List<String> getErrorColumnNames() {
        TableModel meta = getErrorModelType().getAnnotation(TableModel.class);
        if (meta == null) {
            throw new UnsupportedOperationException(MessageFormat.format(
                    "クラス{0}には@{1}の指定がないため、カラム名を自動的に判別できませんでした。{2}#{3}()をオーバーライドして明示的に指定して下さい",
                    getClass().getName(),
                    TableModel.class.getSimpleName(),
                    DupCheckDbExporterDescription.class.getSimpleName(),
                    "getErrorColumnNames"));
        }
        return Arrays.asList(meta.columns());
    }

    @Override
    public List<String> getPrimaryKeyNames() {
        TableModel meta = getModelType().getAnnotation(TableModel.class);
        if (meta == null) {
            throw new UnsupportedOperationException(MessageFormat.format(
                    "クラス{0}には@{1}の指定がないため、主キー名を自動的に判別できませんでした。{2}#{3}()をオーバーライドして明示的に指定して下さい",
                    getClass().getName(),
                    TableModel.class.getSimpleName(),
                    DupCheckDbExporterDescription.class.getSimpleName(),
                    "getColumnNames"));
        }
        return Arrays.asList(meta.primary());
    }

    @Override
    public final DuplicateRecordCheck getDuplicateRecordCheck() {
        String errorTableName = getErrorTableName();

        // エラーコードカラムをエクスポート対象から外す
        List<String> errorColumnNames = new ArrayList<String>(getErrorColumnNames());
        String errorCodeColumnName = getErrorCodeColumnName();
        errorColumnNames.remove(errorCodeColumnName);

        return new DuplicateRecordCheck(
                errorTableName,
                errorColumnNames,
                getCheckColumnNames(),
                errorCodeColumnName,
                getErrorCodeValue());
    }
}
