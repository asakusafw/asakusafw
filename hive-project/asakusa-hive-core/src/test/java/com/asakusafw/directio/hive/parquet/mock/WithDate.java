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
package com.asakusafw.directio.hive.parquet.mock;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Mock data type with date type.
 */
@SuppressWarnings("all")
public class WithDate {

    public final StringOption id = new StringOption();

    public final DateOption value = new DateOption();

    public WithDate() {
        // do nothing
    }

    public WithDate(String id, Date value) {
        this.id.modify(id);
        this.value.modify(value);
    }
}
