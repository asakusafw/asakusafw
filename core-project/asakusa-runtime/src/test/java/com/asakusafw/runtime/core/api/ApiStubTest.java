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
package com.asakusafw.runtime.core.api;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link ApiStub}.
 */
public class ApiStubTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        ApiStub<String> stub = new ApiStub<>("DEFAULT");
        assertThat(stub.get(), is("DEFAULT"));
        try (ApiStub.Reference<String> ref = stub.activate("ANOTHER")) {
            assertThat(stub.get(), is("ANOTHER"));
        }
        assertThat(stub.get(), is("DEFAULT"));
    }

    /**
     * nested.
     */
    @Test
    public void nested() {
        ApiStub<String> stub = new ApiStub<>("DEFAULT");
        assertThat(stub.get(), is("DEFAULT"));
        try (ApiStub.Reference<String> r0 = stub.activate("ANOTHER")) {
            assertThat(stub.get(), is("ANOTHER"));
            try (ApiStub.Reference<String> r1 = stub.activate("ANOTHER")) {
                assertThat(stub.get(), is("ANOTHER"));
            }
            assertThat(stub.get(), is("ANOTHER"));
        }
        assertThat(stub.get(), is("DEFAULT"));
    }

    /**
     * over close.
     */
    @Test
    public void over_close() {
        ApiStub<String> stub = new ApiStub<>("DEFAULT");
        assertThat(stub.get(), is("DEFAULT"));
        try (ApiStub.Reference<String> r0 = stub.activate("ANOTHER");
                ApiStub.Reference<String> r1 = stub.activate("ANOTHER")) {
            r1.close();
            assertThat(stub.get(), is("ANOTHER"));
            r1.close();
            assertThat(stub.get(), is("ANOTHER"));
        }
        assertThat(stub.get(), is("DEFAULT"));
    }

    /**
     * w/o defaults.
     */
    @Test(expected = IllegalStateException.class)
    public void no_defaults() {
        ApiStub<String> stub = new ApiStub<>();
        stub.get();
    }

    /**
     * conflict implementations.
     */
    @Test(expected = IllegalStateException.class)
    public void conflict() {
        ApiStub<String> stub = new ApiStub<>("DEFAULT");
        try (ApiStub.Reference<String> r0 = stub.activate("ANOTHER")) {
            try (ApiStub.Reference<String> r1 = stub.activate("CONFLICT")) {
                fail();
            }
        }
    }
}
