/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio;

import java.io.IOException;

/**
 * An abstract implementation of {@link DirectDataSourceRepository}.
 * @since 0.2.5
 */
public abstract class AbstractDirectDataSource implements DirectDataSource {

    /**
     * Configures this data source.
     * @param profile profile object
     * @throws IOException if failed to configure this object
     * @throws InterruptedException if interrupted
     */
    public abstract void configure(DirectDataSourceProfile profile) throws IOException, InterruptedException;
}
