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
package com.asakusafw.compiler.flow.example;

import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.compiler.testing.TemporaryOutputDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;


/**
 * Mock {@link ExporterDescription} for {@link MockHoge}.
 */
public class MockHogeExporterDescription extends TemporaryOutputDescription {

    @Override
    public Class<?> getModelType() {
        return MockHoge.class;
    }

    @Override
    public String getPathPrefix() {
        return "external/out/" + getModelType().getSimpleName() + "-*";
    }
}
