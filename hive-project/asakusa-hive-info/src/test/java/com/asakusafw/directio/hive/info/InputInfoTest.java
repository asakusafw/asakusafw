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
package com.asakusafw.directio.hive.info;

import org.junit.Test;

import com.asakusafw.directio.hive.info.FieldType.TypeName;

/**
 * Test for {@link InputInfo}.
 */
public class InputInfoTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        check(new InputInfo(
                new LocationInfo("base-path", "resource-pattern"),
                new TableInfo.Builder("TESTING")
                    .withColumn("COL", TypeName.INT)
                    .build()));
    }

    private void check(InputInfo info) {
        Util.check(InputInfo.class, info);
    }
}
