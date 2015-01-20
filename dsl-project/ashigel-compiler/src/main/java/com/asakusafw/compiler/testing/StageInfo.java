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
package com.asakusafw.compiler.testing;

import com.asakusafw.compiler.common.Precondition;

/**
 * 各ステージの実行情報。
 */
public class StageInfo {

    private String className;

    /**
     * インスタンスを生成する。
     * @param className ステージを起動するためのクライアントクラスの完全限定名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public StageInfo(String className) {
        Precondition.checkMustNotBeNull(className, "className"); //$NON-NLS-1$
        this.className = className;
    }

    /**
     * ステージで起動されるクラスの名前を返す。
     * @return ステージで起動されるクラスの名前
     */
    public String getClassName() {
        return className;
    }
}
