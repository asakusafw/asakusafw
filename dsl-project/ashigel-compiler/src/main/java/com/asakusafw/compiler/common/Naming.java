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
package com.asakusafw.compiler.common;

import java.text.MessageFormat;

/**
 * エミッタの名前付け。
 * @since 0.1.0
 * @version 0.2.6
 */
public final class Naming {

    /**
     * ステージごとのクライアントのクラス名を返す。
     * @return ステージごとのクライアントのクラス名
     */
    public static String getClientClass() {
        return "StageClient"; //$NON-NLS-1$
    }

    /**
     * ステージごとのマッパークラスの単純名を返す。
     * @param inputId 入力ポート番号
     * @return マッパークラスの単純名
     */
    public static String getMapClass(int inputId) {
        return String.format("%s%d", "StageMapper", inputId); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * ステージごとのレデューサークラスの単純名を返す。
     * @return レデューサークラスの単純名
     */
    public static String getReduceClass() {
        return "StageReducer"; //$NON-NLS-1$
    }

    /**
     * ステージごとのコンバイナークラスの単純名を返す。
     * @return コンバイナークラスの単純名
     */
    public static String getCombineClass() {
        return "StageCombiner"; //$NON-NLS-1$
    }

    /**
     * マップの断片プログラムクラスの単純名を返す。
     * @param serialNumber フラグメントのシリアル番号
     * @return 断片プログラムクラスの単純名
     */
    public static String getMapFragmentClass(int serialNumber) {
        return String.format("%s%d", "MapFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * レデュースの断片プログラムクラスの単純名を返す。
     * @param serialNumber フラグメントのシリアル番号
     * @return 断片プログラムクラスの単純名
     */
    public static String getReduceFragmentClass(int serialNumber) {
        return String.format("%s%d", "ReduceFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Mapからの出力の断片プログラムクラスの単純名を返す。
     * @param serialNumber フラグメントのシリアル番号
     * @return 断片プログラムクラスの単純名
     */
    public static String getMapOutputFragmentClass(int serialNumber) {
        return String.format("%s%d", "MapOutputFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Combineからの出力の断片プログラムクラスの単純名を返す。
     * @param serialNumber フラグメントのシリアル番号
     * @return 断片プログラムクラスの単純名
     */
    public static String getCombineOutputFragmentClass(int serialNumber) {
        return String.format("%s%d", "CombineOutputFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * コンバインの断片プログラムクラスの単純名を返す。
     * @param serialNumber フラグメントのシリアル番号
     * @return 断片プログラムクラスの単純名
     */
    public static String getCombineFragmentClass(int serialNumber) {
        return String.format("%s%d", "CombineFragment", serialNumber); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * シャッフルキーのクラス単純名。
     * <p>
     * ステージごとのパッケージ直下に配置される。
     * </p>
     * @return クラス単純名
     */
    public static String getShuffleKeyClass() {
        return "ShuffleKey"; //$NON-NLS-1$
    }

    /**
     * シャッフル値のクラス単純名。
     * <p>
     * ステージごとのパッケージ直下に配置される。
     * </p>
     * @return クラス単純名
     */
    public static String getShuffleValueClass() {
        return "ShuffleValue"; //$NON-NLS-1$
    }

    /**
     * シャッフルパーティショナーのクラス単純名。
     * <p>
     * ステージごとのパッケージ直下に配置される。
     * </p>
     * @return クラス単純名
     */
    public static String getShufflePartitionerClass() {
        return "ShufflePartitioner"; //$NON-NLS-1$
    }

    /**
     * シャッフルグループ比較器のクラス単純名。
     * <p>
     * ステージごとのパッケージ直下に配置される。
     * </p>
     * @return クラス単純名
     */
    public static String getShuffleGroupingComparatorClass() {
        return "ShuffleGroupingComparator"; //$NON-NLS-1$
    }

    /**
     * シャッフル順序比較器のクラス単純名。
     * <p>
     * ステージごとのパッケージ直下に配置される。
     * </p>
     * @return クラス単純名
     */
    public static String getShuffleSortComparatorClass() {
        return "ShuffleSortComparator"; //$NON-NLS-1$
    }

    /**
     * シャッフルキーのコピーメソッド名。
     * @return メソッド名
     */
    public static String getShuffleKeyGroupCopier() {
        return "copyGroupFrom"; //$NON-NLS-1$
    }

    /**
     * シャッフルキーのグループ化プロパティ名。
     * @param elementId 要素ID
     * @param termId 項目ID
     * @return プロパティ名
     */
    public static String getShuffleKeyGroupProperty(int elementId, int termId) {
        return String.format("groupElem%dTerm%d", elementId, termId); //$NON-NLS-1$
    }

    /**
     * シャッフルキーのソートプロパティ名。
     * @param portId ポートID
     * @param termId 項目ID
     * @return プロパティ名
     */
    public static String getShuffleKeySortProperty(int portId, int termId) {
        return String.format("sortPort%dTerm%d", portId, termId); //$NON-NLS-1$
    }

    /**
     * シャッフルキーのセッター名。
     * @param portId ポートID
     * @return プロパティ名
     */
    public static String getShuffleKeySetter(int portId) {
        return String.format("setPort%d", portId); //$NON-NLS-1$
    }

    /**
     * シャッフル値のゲッター名。
     * @param portId ポートID
     * @return プロパティ名
     */
    public static String getShuffleValueGetter(int portId) {
        return String.format("getPort%d", portId); //$NON-NLS-1$
    }

    /**
     * シャッフル値のセッター名。
     * @param portId ポートID
     * @return プロパティ名
     */
    public static String getShuffleValueSetter(int portId) {
        return String.format("setPort%d", portId); //$NON-NLS-1$
    }

    /**
     * ステージの名前を返す。
     * @param stageNumber 対象のステージ番号
     * @return 名前
     */
    public static String getStageName(int stageNumber) {
        return String.format("stage%04d", stageNumber); //$NON-NLS-1$
    }

    /**
     * Returns the cleanup stage.
     * @return the stage name
     * @since 0.2.6
     */
    public static String getCleanupStageName() {
        return "cleanup"; //$NON-NLS-1$
    }

    /**
     * プロローグステージの名前を返す。
     * @param moduleId プロローグ処理を行うモジュールの名前
     * @return 名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getPrologueName(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return MessageFormat.format("prologue.{0}", moduleId); //$NON-NLS-1$
    }

    /**
     * プロローグステージの名前を返す。
     * @param moduleId プロローグ処理を行うモジュールの名前
     * @param stageId ステージ識別子
     * @return 名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getPrologueName(String moduleId, String stageId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return MessageFormat.format("prologue.{0}.{1}", moduleId, stageId); //$NON-NLS-1$
    }

    /**
     * エピローグステージの名前を返す。
     * @param moduleId エピローグ処理を行うモジュールの名前
     * @return 名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getEpilogueName(String moduleId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return MessageFormat.format("epilogue.{0}", moduleId); //$NON-NLS-1$
    }

    /**
     * エピローグステージの名前を返す。
     * @param moduleId エピローグ処理を行うモジュールの名前
     * @param stageId ステージ識別子
     * @return 名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getEpilogueName(String moduleId, String stageId) {
        Precondition.checkMustNotBeNull(moduleId, "moduleId"); //$NON-NLS-1$
        return MessageFormat.format("epilogue.{0}.{1}", moduleId, stageId); //$NON-NLS-1$
    }

    /**
     * コンパイル結果の標準的なクラスパッケージファイル名を返す。
     * @param flowId 対象の識別子
     * @return 対応するクラスパッケージファイル名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getJobflowClassPackageName(String flowId) {
        Precondition.checkMustNotBeNull(flowId, "flowId"); //$NON-NLS-1$
        return String.format("jobflow-%s%s", flowId, ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * コンパイル結果の標準的なソースパッケージファイル名を返す。
     * @param flowId 対象の識別子
     * @return 対応するパッケージファイル名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getJobflowSourceBundleName(String flowId) {
        Precondition.checkMustNotBeNull(flowId, "flowId"); //$NON-NLS-1$
        return String.format("jobflow-%s-sources%s", flowId, ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * インスタンス化の禁止。
     */
    private Naming() {
        throw new AssertionError();
    }
}
