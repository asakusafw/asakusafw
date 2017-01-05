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
package com.asakusafw.testdriver.directio.api;

import com.asakusafw.testdriver.TesterBase;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

/**
 * Flow testing API for Direct I/O.
 * @since 0.7.3
 */
final class DirectIoFlowTester extends DirectIoTester {

    private final TesterBase tester;

    DirectIoFlowTester(TesterBase tester) {
        this.tester = tester;
    }

    @Override
    protected DirectIoResource resource(DirectFileInputDescription description) {
        return new Resource(tester, description);
    }

    private static final class Resource extends DirectIoResource {

        private final TesterBase tester;

        Resource(TesterBase tester, DirectFileInputDescription description) {
            super(tester, description);
            this.tester = tester;
        }

        @Override
        public void prepare(DataModelSourceFactory source) {
            tester.putExternalResource(description, source);
        }
    }
}
