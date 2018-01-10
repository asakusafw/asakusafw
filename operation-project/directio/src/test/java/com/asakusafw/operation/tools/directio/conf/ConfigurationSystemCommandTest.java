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
package com.asakusafw.operation.tools.directio.conf;

import org.apache.hadoop.fs.FileSystem;
import org.junit.Test;

import com.asakusafw.operation.tools.directio.DirectIoToolsTestRoot;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;

/**
 * Test for {@link ConfigurationSystemCommand}.
 */
public class ConfigurationSystemCommandTest extends DirectIoToolsTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        invoke("configuration", "system");
    }

    /**
     * show help.
     */
    @Test
    public void help() {
        invoke("configuration", "system", "--help");
    }

    /**
     * w/ invalid file system.
     */
    @Test
    public void invalid_filesystem() {
        getConf().set(FileSystem.FS_DEFAULT_NAME_KEY, "invalid:///");
        addEntry("root", "/", "here");
        invoke("configuration", "system");
        // w/o error
    }

    /**
     * w/ invalid source conf.
     */
    @Test
    public void invalid_datasource() {
        getConf().set(HadoopDataSourceUtil.KEY_SYSTEM_DIR, "invalid:///testing");
        addEntry("invalid", "/", "testing");
        invoke("configuration", "system");
        // w/o error
    }

    /**
     * w/ invalid source conf but system dir is valid.
     */
    @Test
    public void invalid_pair() {
        getConf().set(FileSystem.FS_DEFAULT_NAME_KEY, "invalid:///");
        getConf().set(HadoopDataSourceUtil.KEY_SYSTEM_DIR, "file:///testing");
        addEntry("invalid", "/", "testing");
        invoke("configuration", "system");
        // w/o error
    }
}
