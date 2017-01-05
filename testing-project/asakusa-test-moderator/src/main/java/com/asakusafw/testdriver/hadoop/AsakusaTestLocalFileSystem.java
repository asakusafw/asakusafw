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
package com.asakusafw.testdriver.hadoop;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.LocalFileSystem;

/**
 * Creates FileSystem For Local Test With TestDriver.
 * @since 0.2.5
 * @version 0.6.0
 */
public class AsakusaTestLocalFileSystem extends LocalFileSystem {

    /**
     * Sets Working Directory as Home.
     * @param name URI.
     * @param conf Configuration,
     * @throws IOException if failed to initialize this object
     * @see org.apache.hadoop.fs.FilterFileSystem#initialize(java.net.URI,
     *      org.apache.hadoop.conf.Configuration)
     */
    @Override
    public void initialize(URI name, Configuration conf) throws IOException {
        super.initialize(name, conf);
        setWorkingDirectory(getHomeDirectory());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass().equals(obj.getClass());
    }
}
