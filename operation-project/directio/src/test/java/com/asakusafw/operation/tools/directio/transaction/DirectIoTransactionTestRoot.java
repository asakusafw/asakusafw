/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.directio.transaction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.operation.tools.directio.DirectIoToolsTestRoot;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceProfile;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;

/**
 * Testing utilities about this package.
 */
abstract class DirectIoTransactionTestRoot extends DirectIoToolsTestRoot {

    private File systemDir;

    private final Map<String, File> tempDirs = new HashMap<>();

    File useSystemDir() {
        if (systemDir == null) {
            try {
                systemDir = folder.newFolder();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            conf.set(HadoopDataSourceUtil.KEY_SYSTEM_DIR, systemDir.toURI().toString());
        }
        return systemDir;
    }

    void addTransaction(String txId, boolean committed) {
        try {
            Path txDir = txDir();
            Files.createDirectories(txDir);

            Path beginMark = beginMark(txId);
            write(beginMark.toFile(), txId);

            if (committed) {
                Path commitMark = commitMark(txId);
                write(commitMark.toFile(), txId);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    Path txDir() {
        Path base = useSystemDir().toPath();
        return base.resolve(HadoopDataSourceUtil.TRANSACTION_INFO_DIR);
    }

    Path commitMark(String id) {
        return txDir().resolve(HadoopDataSourceUtil.PREFIX_COMMIT_MARK + id);
    }

    Path beginMark(String id) {
        return txDir().resolve(HadoopDataSourceUtil.PREFIX_BEGIN_MARK + id);
    }

    File useStageDir(String txId, String dsId) {
        File base = tempDirs.get(dsId);
        if (base == null) {
            try {
                base = folder.newFolder();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            String key = String.format("%s%s.%s", HadoopDataSourceUtil.PREFIX, dsId, HadoopDataSourceProfile.KEY_TEMP);
            conf.set(key, base.toURI().toString());
        }
        Path dir = base.toPath()
                .resolve(String.format("%s-%s", txId, dsId))
                .resolve("staging");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return dir.toFile();
    }
}
