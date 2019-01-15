/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.value.IntOption;

/**
 * Mock {@link IntOption} projection.
 */
@DataModelKind("DMDL")
public interface MockIntProjection {

    /**
     * Returns the {@link IntOption}.
     * @return {@link IntOption}
     */
    IntOption getIntOption();

}