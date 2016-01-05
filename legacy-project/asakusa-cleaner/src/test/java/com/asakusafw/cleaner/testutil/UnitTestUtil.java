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
package com.asakusafw.cleaner.testutil;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.asakusafw.cleaner.common.ConfigurationLoader;
import com.asakusafw.cleaner.common.Constants;

/**
 * <p>
 * </p>
 *
 * @author akira.kawaguchi
 *
 */
public class UnitTestUtil {
    private static final File targetDir = new File("target/asakusa-cleaner");
    public static void setUpEnv() throws Exception {
        Properties p = System.getProperties();
        p.setProperty(Constants.CLEAN_HOME, "src/test");
        ConfigurationLoader.setSysProp(p);
        System.setProperties(p);
    }
    public static void tearDownEnv() throws Exception {
        Properties p = System.getProperties();
        p.clear();
        ConfigurationLoader.setSysProp(p);
        System.setProperties(p);
    }
    public static void setUpBeforeClass() throws Exception {
        targetDir.mkdir();
    }
    public static void tearDownAfterClass() throws Exception {
        FileUtils.deleteDirectory(targetDir);
    }
    public static void startUp() throws Exception {
    }
    public static void tearDown() throws Exception {
    }
}
