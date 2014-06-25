/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.serde.mock;

import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Simple mock data model.
 */
@SuppressWarnings("all")
public class MockSimple {

    public final IntOption number = new IntOption();

    public final StringOption string = new StringOption();

    public MockSimple() {
        // do nothing
    }

    public MockSimple(int number, String string) {
        this.number.modify(number);
        this.string.modify(string);
    }
}
