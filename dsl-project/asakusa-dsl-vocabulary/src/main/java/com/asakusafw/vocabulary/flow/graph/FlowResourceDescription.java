/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.util.Set;

/**
 * フロー中に出現するリソースの記述。
 */
public interface FlowResourceDescription {

    /**
     * このリソースが利用するサイドデータの入力一覧を返す。
     * @return このリソースが利用するサイドデータの入力一覧
     */
    Set<InputDescription> getSideDataInputs();
}
