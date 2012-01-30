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
package com.asakusafw.runtime.flow;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

/**
 * MapやReduceの実行時に利用され、ライフサイクルを持つリソース。
 */
public interface FlowResource {

    /**
     * このリソースを初期化する。
     * @param configuration 初期化の設定
     * @throws IOException リソースの初期化に失敗した場合
     * @throws InterruptedException 初期化中に割り込みが発生した場合
     * @throws IllegalArgumentException 設定が不正である場合
     * @throws IllegalStateException 同一のリソースが複数回初期化された場合
     */
    void setup(Configuration configuration) throws IOException, InterruptedException;

    /**
     * このリソースを解放する。
     * @param configuration 初期化の設定
     * @throws IOException リソースの初期化に失敗した場合
     * @throws InterruptedException 初期化中に割り込みが発生した場合
     * @throws IllegalArgumentException 設定が不正である場合
     * @throws IllegalStateException 同一のリソースが複数回初期化された場合
     */
    void cleanup(Configuration configuration) throws IOException, InterruptedException;
}
