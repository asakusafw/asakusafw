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
package com.asakusafw.compiler.flow;

/**
 * データクラスを格納するリポジトリ。
 * <p>
 * Adding data model kinds to Flow DSL compiler, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.compiler.operator.DataModelMirrorRepository}.
 * </p>
 */
public interface DataClassRepository extends FlowCompilingEnvironment.Initializable {

    /**
     * 指定の型に対するデータクラスを返す。
     * @param type 対象の型
     * @return 対応するデータクラス、解析できない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    DataClass load(java.lang.reflect.Type type);
}