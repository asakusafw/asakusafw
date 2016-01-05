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
package com.asakusafw.vocabulary.bulkloader;

import java.util.Arrays;
import java.util.List;

/**
 * Mock {@link DupCheckDbExporterDescription}.
 */
public class InvalidDupCheckDbExporterDescription extends DupCheckDbExporterDescription {

    @Override
    public String getTargetName() {
        return "default";
    }

    @Override
    public Class<?> getModelType() {
        return NoTableModel.class;
    }

    @Override
    protected Class<?> getNormalModelType() {
        return NoTableModel.class;
    }

    @Override
    protected Class<?> getErrorModelType() {
        return NoErrorModel.class;
    }

    @Override
    protected List<String> getCheckColumnNames() {
        return Arrays.asList("A");
    }

    @Override
    protected String getErrorCodeColumnName() {
        return "E";
    }

    @Override
    protected String getErrorCodeValue() {
        return "DUP!";
    }
}
