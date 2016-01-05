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
package com.asakusafw.compiler.bulkloader;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.asakusafw.compiler.bulkloader.BulkLoaderScript.DuplicateRecordErrorTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.ExportTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.ImportTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.LockType;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.LockedOperation;
import com.asakusafw.compiler.bulkloader.testing.model.Ex1;
import com.asakusafw.compiler.bulkloader.testing.model.Ex2;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.utils.collections.Lists;

/**
 * Test for {@link BulkLoaderScript}.
 */
public class BulkLoaderScriptTest {

    /**
     * インポーター周辺のテスト。
     */
    @Test
    public void importers() {
        List<ImportTable> importers = Lists.create();
        List<ExportTable> exporters = Lists.create();
        importers.add(new ImportTable(
                Ex1.class,
                "EX1",
                Arrays.asList("SID", "VALUE", "STRING", "LAST_UPDATE_TIME", "JOBFLOW_SID", "CACHE_FILE_SID"),
                "VALUE > 0",
                "cache-id",
                LockType.ROW,
                LockedOperation.ERROR,
                Location.fromPath("ex1", '/')));
        importers.add(new ImportTable(
                Ex2.class,
                "EX2",
                Arrays.asList("SID", "VALUE", "STRING", "LAST_UPDATE_TIME", "JOBFLOW_SID", "CACHE_FILE_SID"),
                "VALUE < 0",
                null,
                LockType.UNLOCKED,
                LockedOperation.FORCE,
                Location.fromPath("ex2", '/')));
        BulkLoaderScript script = new BulkLoaderScript(importers, exporters);
        Properties properties = script.getImporterProperties();
        List<ImportTable> restored = ImportTable.fromProperties(properties, getClass().getClassLoader());

        assertThat(restored, is(importers));
    }

    /**
     * エクスポーター周辺のテスト。
     */
    @Test
    public void exporters() {
        List<ImportTable> importers = Lists.create();
        List<ExportTable> exporters = Lists.create();
        exporters.add(new ExportTable(
                Ex1.class,
                "EX1",
                Arrays.asList("SID", "VALUE", "STRING", "LAST_UPDATE_TIME", "JOBFLOW_SID", "CACHE_FILE_SID"),
                Arrays.asList("VALUE", "STRING"),
                null,
                Collections.singletonList(Location.fromPath("ex1", '/'))));
        exporters.add(new ExportTable(
                Ex2.class,
                "EX2",
                Arrays.asList("SID", "VALUE", "STRING", "LAST_UPDATE_TIME", "JOBFLOW_SID", "CACHE_FILE_SID", "ERROR"),
                Arrays.asList("VALUE", "STRING"),
                new DuplicateRecordErrorTable(
                        "EX2_ERROR",
                        Arrays.asList("VALUE", "STRING", "JOBFLOW_SID"),
                        Arrays.asList("VALUE"),
                        "ERROR",
                        "invalid"),
                Arrays.asList(
                        Location.fromPath("ex2_0", '/'),
                        Location.fromPath("ex2_1", '/'))));
        BulkLoaderScript script = new BulkLoaderScript(importers, exporters);
        Properties properties = script.getExporterProperties();
        List<ExportTable> restored =
            ExportTable.fromProperties(properties, getClass().getClassLoader());
        assertThat(restored, is(exporters));
    }
}
