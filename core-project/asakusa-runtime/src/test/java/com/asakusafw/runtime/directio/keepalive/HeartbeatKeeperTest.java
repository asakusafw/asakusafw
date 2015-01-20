/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.keepalive;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.runtime.directio.Counter;

/**
 * Test for {@link HeartbeatKeeper}.
 */
public class HeartbeatKeeperTest {

    private HeartbeatKeeper keeper;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        keeper = new HeartbeatKeeper(10);
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        keeper.close();
    }

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Mock mock = new Mock();
        long s0 = mock.count;
        assertThat(s0, is(0L));

        keeper.register(mock);
        long s11 = mock.count;
        Thread.sleep(500);
        long s12 = mock.count;
        assertThat(s12, greaterThan(s11));

        keeper.unregister(mock);
        long s21 = mock.count;
        Thread.sleep(500);
        long s22 = mock.count;
        assertThat(s22, is(s21));

        // ok.
        keeper.unregister(mock);
    }

    private static class Mock extends Counter {

        volatile long count;

        Mock() {
            return;
        }

        @Override
        protected void onChanged() {
            count++;
        }
    }
}
