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

import parquet.schema.Type;

/**
 * Exchanges values between Asakusa and Parquet API.
 * @since 0.7.0
 */
public interface ParquetValueDriver {

    /**
     * Returns a parquet data type for this driver.
     * @param name the field name
     * @return the related type
     */
    Type getType(String name);

    /**
     * Returns a new {@link ValueConverter}.
     * @return a new converter
     */
    ValueConverter getConverter();

    /**
     * Returns a {@link ValueWriter}.
     * @return a writer
     */
    ValueWriter getWriter();
}
