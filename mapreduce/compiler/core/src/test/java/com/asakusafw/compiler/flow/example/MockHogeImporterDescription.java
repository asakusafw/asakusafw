/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.util.Collections;
import java.util.Set;

import com.asakusafw.compiler.operator.model.MockHoge;
import com.asakusafw.compiler.testing.TemporaryInputDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;


/**
 * Mock {@link ImporterDescription} for {@link MockHoge}.
 */
public class MockHogeImporterDescription extends TemporaryInputDescription {

    @Override
    public Class<?> getModelType() {
        return MockHoge.class;
    }

    @Override
    public Set<String> getPaths() {
        return Collections.singleton("external/in/" + getModelType().getSimpleName());
    }
}
