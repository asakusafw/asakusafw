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
package com.asakusafw.operator.dmdl;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;

/**
 * Mock data model.
 */
@DataModelKind("DMDL")
public class MockDataModel implements DataModel<MockDataModel>, MockIntProjection, MockFloatProjection {

    private final IntOption intOption = new IntOption();

    private final FloatOption floatOption = new FloatOption();

    private final DecimalOption mutipleSegmentsNamedOption = new DecimalOption();

    @Override
    public IntOption getIntOption() {
        return intOption;
    }

    @Override
    public FloatOption getFloatOption() {
        return floatOption;
    }

    /**
     * Returns the {@link DecimalOption}.
     * @return {@link DecimalOption}
     */
    public DecimalOption getMutipleSegmentsNamedOption() {
        return mutipleSegmentsNamedOption;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void reset() {
        intOption.setNull();
        floatOption.setNull();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void copyFrom(MockDataModel other) {
        intOption.copyFrom(other.intOption);
        floatOption.copyFrom(other.floatOption);
    }
}
