/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver.directio;

import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;

/**
 * Mock {@link DirectFileOutputDescription}.
 */
public class MockOutputDescription extends DirectFileOutputDescription {

    private final String basePath;

    private final String resourcePattern;

    private final Class<? extends DataFormat<?>> format;

    MockOutputDescription(String basePath, String resourcePattern, Class<? extends DataFormat<?>> format) {
        this.basePath = basePath;
        this.resourcePattern = resourcePattern;
        this.format = format;
    }

    @Override
    public Class<?> getModelType() {
        return Text.class;
    }

    @Override
    public Class<? extends DataFormat<?>> getFormat() {
        return format;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public String getResourcePattern() {
        return resourcePattern;
    }

    @Override
    public List<String> getOrder() {
        return Collections.emptyList();
    }
}
