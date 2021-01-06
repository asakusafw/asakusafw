/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.parquet.mock;

import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Mock data type with date-time type.
 */
@SuppressWarnings("all")
public class WithDateTime {

    public final StringOption id = new StringOption();

    public final DateTimeOption value = new DateTimeOption();

    public WithDateTime() {
        // do nothing
    }

    public WithDateTime(String id, DateTime value) {
        this.id.modify(id);
        this.value.modify(value);
    }
}
