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
package com.asakusafw.directio.hive.parquet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

final class Util {

    static final Log LOG = LogFactory.getLog(Util.class);

    private Util() {
        return;
    }

    static long getFileSize(Path path, Configuration conf) {
        try {
            FileSystem fs = path.getFileSystem(conf);
            FileStatus status = fs.getFileStatus(path);
            return status.getLen();
        } catch (FileNotFoundException e) {
            LOG.debug(MessageFormat.format(
                    "cannot obtain the Parquet file size: {0}",
                    path), e);
        } catch (IOException e) {
            LOG.warn(MessageFormat.format(
                    "cannot obtain the Parquet file size: {0}",
                    path), e);
        }
        return 0;
    }
}
