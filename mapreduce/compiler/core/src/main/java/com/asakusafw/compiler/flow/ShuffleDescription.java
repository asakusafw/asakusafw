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
package com.asakusafw.compiler.flow;

import java.lang.reflect.Type;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;

/**
 * Describes shuffle operations.
 */
public class ShuffleDescription {

    private final Type outputType;

    private final ShuffleKey keyInfo;

    private final LinePartProcessor converter;

    /**
     * Creates a new instance.
     * @param outputType the shuffle output type
     * @param keyInfo information of the shuffle key
     * @param converter the data converter for shuffle input
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ShuffleDescription(
            Type outputType,
            ShuffleKey keyInfo,
            LinePartProcessor converter) {
        Precondition.checkMustNotBeNull(outputType, "outputType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(keyInfo, "keyInfo"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(converter, "converter"); //$NON-NLS-1$
        this.outputType = outputType;
        this.keyInfo = keyInfo;
        this.converter = converter;
    }

    /**
     * Returns the shuffle output type.
     * @return the shuffle output type
     */
    public Type getOutputType() {
        return outputType;
    }

    /**
     * Returns information of the shuffle key.
     * @return information of the shuffle key
     */
    public ShuffleKey getKeyInfo() {
        return keyInfo;
    }

    /**
     * Returns the data converter for shuffle input.
     * @return the data converter for shuffle input
     */
    public LinePartProcessor getConverter() {
        return converter;
    }
}
