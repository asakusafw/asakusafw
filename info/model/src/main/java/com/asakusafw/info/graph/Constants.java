/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.info.graph;

final class Constants {

    static final String ID_ID = "id";

    static final String ID_ATTRIBUTES = "attributes";

    static final String ID_NODE_INPUTS = "inputs";

    static final String ID_NODE_OUTPUTS = "outputs";

    static final String ID_NODE_WIRES = "wires";

    static final String ID_NODE_ELEMENTS = "elements";

    static final String ID_WIRE_UPSTREAM = "source";

    static final String ID_WIRE_DOWNSTREAM = "destination";

    private Constants() {
        return;
    }
}
