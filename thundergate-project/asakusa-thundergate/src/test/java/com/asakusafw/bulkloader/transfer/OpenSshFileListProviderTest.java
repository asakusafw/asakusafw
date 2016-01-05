/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.bulkloader.transfer;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assume;
import org.junit.Test;

/**
 * Test for {@link OpenSshFileListProvider}.
 */
public class OpenSshFileListProviderTest {

    /**
     * Simple testing.
     */
    @Test
    public void simple() {
        try {
            OpenSshFileListProvider provider = new OpenSshFileListProvider(
                    "/usr/bin/ssh",
                    System.getProperty("user.name"),
                    "localhost",
                    Arrays.asList("echo", "hoge", "VAR"),
                    Collections.singletonMap("MESSAGE", "VAR"));
            try {
                provider.discardWriter();
                provider.discardReader();
                provider.waitForComplete();
            } finally {
                provider.close();
            }
        } catch (Exception e) {
            Assume.assumeNoException(e);
        }
    }
}
