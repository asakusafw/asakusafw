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
package com.asakusafw.workflow.hadoop;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes files on Hadoop file system.
 * @since 0.10.0
 */
public final class HadoopDelete {

    static final Logger LOG = LoggerFactory.getLogger(HadoopDelete.class);

    private HadoopDelete() {
        return;
    }

    /**
     * Program entry.
     * @param args paths to delete
     */
    public static void main(String... args) {
        int status = exec(args);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int exec(String... args) {
        if (args.length == 0) {
            LOG.warn("there are no files to delete");
            return -1;
        }
        boolean sawError = false;
        Configuration conf = new Configuration();
        for (String arg : args) {
            try {
                delete(conf, new Path(arg));
            } catch (IOException e) {
                LOG.error(MessageFormat.format(
                        "failed to delete file: {0}",
                        arg), e);
                sawError = true;
            }
        }
        return sawError ? 1 : 0;
    }

    private static void delete(Configuration conf, Path path) throws IOException {
        FileSystem fs = path.getFileSystem(conf);
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleting file: {}", fs.makeQualified(path));
        }
        boolean deleted = fs.delete(path, true);
        if (LOG.isDebugEnabled()) {
            if (deleted) {
                LOG.debug("delete success: {}", fs.makeQualified(path));
            } else if (fs.exists(path)) {
                LOG.debug("delete failed: {}", fs.makeQualified(path));
            } else {
                LOG.debug("target file is not found: {}", fs.makeQualified(path));
            }
        }
    }
}
