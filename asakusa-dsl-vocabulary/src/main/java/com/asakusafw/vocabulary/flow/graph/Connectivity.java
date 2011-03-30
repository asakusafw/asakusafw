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
package com.asakusafw.vocabulary.flow.graph;

/**
 * 要素の出力の結線に関する制約。
 */
public enum Connectivity implements FlowElementAttribute {

    /**
     * 結線されていなくてもよい。
     */
    OPTIONAL,

    /**
     * 結線されていなければならない。
     */
    MANDATORY,

    ;
    /**
     * 規定の制約を返す。
     * @return 規定の制約
     */
    public static Connectivity getDefault() {
        return MANDATORY;
    }
}
