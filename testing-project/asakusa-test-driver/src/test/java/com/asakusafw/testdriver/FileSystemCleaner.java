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
package com.asakusafw.testdriver;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Closes all Hadoop file systems and resets their cache.
 */
public class FileSystemCleaner extends ExternalResource {

    static final Logger LOG = LoggerFactory.getLogger(FileSystemCleaner.class);

    @Override
    protected void before() {
        clean();
    }

    @Override
    protected void after() {
        clean();
    }

    private static void clean() {
        try {
            FileSystem.closeAll();
        } catch (IOException e) {
            LOG.warn("error occurred while cleaning up Hadoop file systems", e);
        }
    }
}
