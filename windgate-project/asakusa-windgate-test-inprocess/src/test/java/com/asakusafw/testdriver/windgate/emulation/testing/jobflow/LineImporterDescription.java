/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver.windgate.emulation.testing.jobflow;

import com.asakusafw.testdriver.windgate.emulation.testing.io.LineStreamSupport;
import com.asakusafw.testdriver.windgate.emulation.testing.model.Line;
import com.asakusafw.vocabulary.windgate.FsImporterDescription;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;

/**
 * Importer description for {@link Line}.
 */
public class LineImporterDescription extends FsImporterDescription {

    @Override
    public Class<?> getModelType() {
        return Line.class;
    }

    @Override
    public String getProfileName() {
        return "testing";
    }

    @Override
    public String getPath() {
        return "input.txt";
    }

    @Override
    public Class<? extends DataModelStreamSupport<?>> getStreamSupport() {
        return LineStreamSupport.class;
    }
}
