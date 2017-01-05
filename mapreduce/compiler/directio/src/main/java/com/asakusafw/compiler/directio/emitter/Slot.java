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
package com.asakusafw.compiler.directio.emitter;

import java.util.List;

import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.SourceInfo;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.stage.directio.DirectOutputOrder;
import com.asakusafw.runtime.stage.directio.DirectOutputSpec;
import com.asakusafw.runtime.stage.directio.StringTemplate;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Represents {@link DirectOutputSpec} for compile time.
 * @since 0.2.5
 * @version 0.4.0
 */
public class Slot {

    final String name;

    final List<SourceInfo> sources;

    final Name valueType;

    final String basePath;

    final String resourcePath;

    final Name formatClass;

    final Name namingClass;

    final Name orderClass;

    final List<String> deletePatterns;

    /**
     * Creates a new instance.
     * @param name the name of this slot
     * @param sources source information
     * @param valueType name of value type
     * @param basePath target base path
     * @param resourcePath target resource path
     * @param formatClass {@link DataFormat} class name
     * @param namingClass {@link StringTemplate} class name
     * @param orderClass {@link DirectOutputOrder} class name
     * @param deletePatterns delete file patterns
     */
    public Slot(
            String name,
            List<SourceInfo> sources,
            Name valueType,
            String basePath,
            String resourcePath,
            Name formatClass,
            Name namingClass,
            Name orderClass,
            List<String> deletePatterns) {
        assert (namingClass == null) == (orderClass == null);
        this.name = name;
        this.sources = sources;
        this.valueType = valueType;
        this.basePath = basePath;
        this.resourcePath = resourcePath;
        this.formatClass = formatClass;
        this.namingClass = namingClass;
        this.orderClass = orderClass;
        this.deletePatterns = deletePatterns;
    }
}
