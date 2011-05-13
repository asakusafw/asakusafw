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
package com.asakusafw.testdriver;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.asakusafw.compiler.flow.Location;

/**
 * フロー部品用のテストドライバを実行するためのユーティリティクラス。
 * @since 0.2.0
 */
class FlowPartDriverUtils {

    
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
                .append(driverContext.getExecutionId()).append("input").append(normalize(name));
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
                .append(driverContext.getExecutionId()).append("output").append(normalize(name)).asPrefix();
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
                .append(driverContext.getExecutionId()).append("temp");
        return location;
    }

    /**
     * パス文字列からURIを生成する。
     * 
     * @param path パス文字列
     * @param fragment URIに付加するフラグメント識別子
     * @return ワーキングのリソース位置
     * @throws URISyntaxException 引数の値がURIとして不正な値であった場合
     */
    public static URI toUri(String path, String fragment) throws URISyntaxException {
        URI resource = new File(path).toURI();
        URI uri = new URI(resource.getScheme(), resource.getUserInfo(), resource.getHost(), resource.getPort(),
                resource.getPath(), resource.getQuery(), fragment);
        return uri;
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
                buf.append(String.format("%02x", (int) c));
            } else {
                buf.append("0u");
                buf.append(String.format("%04x", (int) c));
            }
        }
        return buf.toString();
    }

}
