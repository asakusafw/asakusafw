/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Tuple2;
import com.asakusafw.utils.collections.Tuples;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;

/**
 * 列挙を取り扱うためのユーティリティ。
 */
public final class EnumUtil {

    /**
     * 列挙をポート記述にマッピングして返す。
     * @param enumType マッピングする列挙
     * @param ports マッピング先のポート一覧
     * @return マッピング結果
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static List<Tuple2<Enum<?>, FlowElementPortDescription>> extractConstants(
            Class<?> enumType,
            Collection<FlowElementPortDescription> ports) {
        Precondition.checkMustNotBeNull(enumType, "enumType"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(ports, "ports"); //$NON-NLS-1$

        Enum<?>[] constants = (Enum<?>[]) enumType.getEnumConstants();
        if (constants == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Cannot extract constants from {0}",
                    enumType));
        }

        Map<String, FlowElementPortDescription> portNames = Maps.create();
        for (FlowElementPortDescription port : ports) {
            portNames.put(port.getName(), port);
        }

        List<Tuple2<Enum<?>, FlowElementPortDescription>> results = Lists.create();
        for (Enum<?> constant : constants) {
            String name = JavaName.of(constant.name()).toMemberName();
            FlowElementPortDescription port = portNames.get(name);
            if (port == null) {
                throw new IllegalStateException(MessageFormat.format(
                        "Cannot extract {0} (in {1})",
                        constant.name(),
                        portNames));
            }
            results.add(Tuples.<Enum<?>, FlowElementPortDescription>of(constant, port));
        }
        if (ports.size() > results.size()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Cannot map constants to ports ({0} -> {1})",
                    Arrays.asList(constants),
                    ports));
        }
        return results;
    }

    /**
     * インスタンス化の禁止。
     */
    private EnumUtil() {
        throw new AssertionError();
    }
}
