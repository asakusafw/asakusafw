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
package com.asakusafw.compiler.flow.testing.external;

import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.testing.TemporaryOutputDescription;


/**
 * {@link Ex2}のモックエクスポーター記述。
 */
public class Ex2MockExporterDescription extends TemporaryOutputDescription {

    @Override
    public Class<?> getModelType() {
        return Ex2.class;
    }

    @Override
    public String getPathPrefix() {
        return "target/testing/out/" + getModelType().getSimpleName() + "-*";
    }
}
