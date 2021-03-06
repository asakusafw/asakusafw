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
package com.asakusafw.info.hive;

import org.junit.Test;

import com.asakusafw.info.InfoSerDe;
import com.asakusafw.info.hive.FieldType.TypeName;

/**
 * Test for {@link ColumnInfo}.
 */
public class ColumnInfoTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        InfoSerDe.checkRestore(ColumnInfo.class,
                new ColumnInfo("testing", PlainType.of(TypeName.INT), null));
    }

    /**
     * w/ comment.
     */
    @Test
    public void comment() {
        InfoSerDe.checkRestore(ColumnInfo.class,
                new ColumnInfo("w_comment", PlainType.of(TypeName.STRING), "Hello, world!"));
    }
}
