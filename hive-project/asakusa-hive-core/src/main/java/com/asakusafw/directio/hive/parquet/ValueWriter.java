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
package com.asakusafw.directio.hive.parquet;

import parquet.io.api.RecordConsumer;

/**
 * Write values into {@link RecordConsumer}.
 * @since 0.7.0
 */
@FunctionalInterface
public interface ValueWriter {

    /**
     * Write a value.
     * @param value the target value (never <em>represents</em> {@code null})
     * @param consumer the target consumer
     */
    void write(Object value, RecordConsumer consumer);
}
