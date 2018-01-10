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
package com.asakusafw.dmdl.directio.text.mock;

import java.util.Locale;

import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.io.text.driver.FieldOutput;
import com.asakusafw.runtime.value.StringOption;

/**
 * Mock {@link FieldAdapter}.
 */
@SuppressWarnings("deprecation")
public class MockFieldAdapter implements FieldAdapter<StringOption> {

    @Override
    public void clear(StringOption property) {
        property.setNull();
    }

    @Override
    public void parse(CharSequence contents, StringOption property) {
        property.modify(contents.toString().toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void emit(StringOption property, FieldOutput output) {
        output.put(property.getAsString().toUpperCase());
    }
}
