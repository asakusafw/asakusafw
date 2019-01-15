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
package com.asakusafw.info.hive;

import org.junit.Test;

import com.asakusafw.info.InfoSerDe;

/**
 * Test for {@link StorageFormatInfo}.
 */
public class StorageFormatInfoTest {

    /**
     * built-in formats.
     */
    @Test
    public void builtin() {
        for (StorageFormatInfo.FormatKind kind : StorageFormatInfo.FormatKind.values()) {
            if (kind.getCategory() == StorageFormatInfo.Category.BUILTIN) {
                check(BuiltinStorageFormatInfo.of(kind));
            }
        }
    }

    /**
     * custom formats.
     */
    @Test
    public void custom() {
        check(new CustomStorageFormatInfo("com.example.Input", "com.example.Output"));
    }

    private void check(StorageFormatInfo info) {
        InfoSerDe.checkRestore(StorageFormatInfo.class, info);
    }
}
