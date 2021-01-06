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
package com.asakusafw.directio.hive.serde.mock;

import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;

/**
 * Mock data model with all value types.
 */
@SuppressWarnings("all")
public class MockTypes {

    public final BooleanOption booleanOption = new BooleanOption();

    public final ByteOption byteOption = new ByteOption();

    public final ShortOption shortOption = new ShortOption();

    public final IntOption intOption = new IntOption();

    public final LongOption longOption = new LongOption();

    public final FloatOption floatOption = new FloatOption();

    public final DoubleOption doubleOption = new DoubleOption();

    public final DateOption dateOption = new DateOption();

    public final DateTimeOption dateTimeOption = new DateTimeOption();

    public final StringOption stringOption = new StringOption();

    public final DecimalOption decimalOption = new DecimalOption();
}
