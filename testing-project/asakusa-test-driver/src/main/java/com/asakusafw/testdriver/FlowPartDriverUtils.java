/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.testdriver;


import com.asakusafw.compiler.flow.Location;
import com.asakusafw.runtime.stage.StageConstants;

/**
 * フロー部品用のテストドライバを実行するためのユーティリティクラス。
 * @since 0.2.0
 */
final class FlowPartDriverUtils {


    /**
     * コンストラクタ。
     */
    private FlowPartDriverUtils() {
        // new不可。
    }

    /**
     * Hadoop Jobの入力データのリソース位置を生成する。
     *
     * @param driverContext ドライバコンテキスト。
     * @param name 入力データ名。
     * @return 入力データのリソース位置
     */
    public static Location createInputLocation(TestDriverContext driverContext, String name) {
        Location location = Location.fromPath(driverContext.getClusterWorkDir(), '/')
                .append(StageConstants.EXPR_EXECUTION_ID)
                .append("input") //$NON-NLS-1$
                .append(normalize(name));
        return location;
    }

    /**
     * Hadoop Jobの出力データのリソース位置を生成する。
     *
     * @param driverContext ドライバコンテキスト。
     * @param name 出力データ名。
     * @return 出力データのリソース位置
     */
    public static Location createOutputLocation(TestDriverContext driverContext, String name) {
        Location location = Location.fromPath(driverContext.getClusterWorkDir(), '/')
                .append(StageConstants.EXPR_EXECUTION_ID)
                .append("output") //$NON-NLS-1$
                .append(normalize(name))
                .asPrefix();
        return location;
    }

    /**
     * Hadoop Jobのワーキングディレクトリのリソース位置を生成する。
     *
     * @param driverContext ドライバコンテキスト。
     * @return ワーキングのリソース位置
     */
    public static Location createWorkingLocation(TestDriverContext driverContext) {
        Location location = Location.fromPath(driverContext.getClusterWorkDir(), '/')
                .append(StageConstants.EXPR_EXECUTION_ID)
                .append("temp"); //$NON-NLS-1$
        return location;
    }

    private static String normalize(String name) {
        // MultipleInputs/Outputsではアルファベットと数字だけしかつかえない
        StringBuilder buf = new StringBuilder();
        for (char c : name.toCharArray()) {
            // 0 はエスケープ記号に
            if ('1' <= c && c <= '9' || 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z') {
                buf.append(c);
            } else if (c <= 0xff) {
                buf.append('0');
                buf.append(String.format("%02x", (int) c)); //$NON-NLS-1$
            } else {
                buf.append("0u"); //$NON-NLS-1$
                buf.append(String.format("%04x", (int) c)); //$NON-NLS-1$
            }
        }
        return buf.toString();
    }

}
