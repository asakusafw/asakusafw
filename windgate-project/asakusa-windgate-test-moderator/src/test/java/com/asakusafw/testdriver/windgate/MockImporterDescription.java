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
package com.asakusafw.testdriver.windgate;

import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.windgate.core.DriverScript;

/**
 * Mock {@link WindGateImporterDescription}.
 */
public class MockImporterDescription extends WindGateImporterDescription {

    private final Class<?> modelType;

    private final String profileName;

    private final DriverScript driverScript;

    MockImporterDescription(Class<?> modelType, String profileName, DriverScript driverScript) {
        this.modelType = modelType;
        this.profileName = profileName;
        this.driverScript = driverScript;
    }

    @Override
    public Class<?> getModelType() {
        return modelType;
    }

    @Override
    public String getProfileName() {
        return profileName;
    }

    @Override
    public DriverScript getDriverScript() {
        return driverScript;
    }
}
