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
package com.asakusafw.compiler.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.utils.collections.Tuple2;
import com.asakusafw.utils.collections.Tuples;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;

/**
 * Utilities for enum types.
 */
public final class EnumUtil {

    /**
     * Maps enum constants into the corresponded flow element ports.
     * @param enumType the source enum type
     * @param ports the target flow element ports
     * @return the mapping results
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public static List<Tuple2<Enum<?>, FlowElementPortDescription>> extractConstants(
            Class<?> enumType,
            Collection<FlowElementPortDescription> ports) {
        Precondition.checkMustNotBeNull(enumType, "enumType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(ports, "ports"); //$NON-NLS-1$

        Enum<?>[] constants = (Enum<?>[]) enumType.getEnumConstants();
        if (constants == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Cannot extract constants from {0}", //$NON-NLS-1$
                    enumType));
        }

        Map<String, FlowElementPortDescription> portNames = new HashMap<>();
        for (FlowElementPortDescription port : ports) {
            portNames.put(port.getName(), port);
        }

        List<Tuple2<Enum<?>, FlowElementPortDescription>> results = new ArrayList<>();
        for (Enum<?> constant : constants) {
            String name = JavaName.of(constant.name()).toMemberName();
            FlowElementPortDescription port = portNames.get(name);
            if (port == null) {
                throw new IllegalStateException(MessageFormat.format(
                        "Cannot extract {0} (in {1})", //$NON-NLS-1$
                        constant.name(),
                        portNames));
            }
            results.add(Tuples.of(constant, port));
        }
        if (ports.size() > results.size()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Cannot map constants to ports ({0} -> {1})", //$NON-NLS-1$
                    Arrays.asList(constants),
                    ports));
        }
        return results;
    }

    private EnumUtil() {
        throw new AssertionError();
    }
}
