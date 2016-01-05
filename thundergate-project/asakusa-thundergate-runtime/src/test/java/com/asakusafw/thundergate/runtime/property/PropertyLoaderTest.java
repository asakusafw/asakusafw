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
package com.asakusafw.thundergate.runtime.property;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link PropertyLoader}.
 */
public class PropertyLoaderTest {

    private File zip;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        zip = File.createTempFile(getClass().getSimpleName(), ".zip");
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        if (zip != null && zip.exists()) {
            assertThat(zip.delete(), is(true));
        }
    }

    /**
     * インポーターの設定
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void loadImporterProperties() throws Exception {
        Properties source = new Properties();
        source.setProperty("hello", "world");

        OutputStream out = new FileOutputStream(zip);
        try {
            ZipOutputStream archive = new ZipOutputStream(out);
            PropertyLoader.saveImporterProperties(archive, "default", source);
            archive.close();
        } finally {
            out.close();
        }

        PropertyLoader loader = new PropertyLoader(zip, "default");
        try {
            Properties importer = loader.loadImporterProperties();
            assertThat(importer, is(source));

            try {
                loader.loadExporterProperties();
                fail();
            } catch (IOException e) {
                // ok.
            }
        } finally {
            loader.close();
        }
    }

    /**
     * エクスポーターの設定
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void loadExporterProperties() throws Exception {
        Properties source = new Properties();
        source.setProperty("hello", "world");

        OutputStream out = new FileOutputStream(zip);
        try {
            ZipOutputStream archive = new ZipOutputStream(out);
            PropertyLoader.saveExporterProperties(archive, "default", source);
            archive.close();
        } finally {
            out.close();
        }

        PropertyLoader loader = new PropertyLoader(zip, "default");
        try {
            Properties exporter = loader.loadExporterProperties();
            assertThat(exporter, is(source));

            try {
                loader.loadImporterProperties();
                fail();
            } catch (IOException e) {
                // ok.
            }
        } finally {
            loader.close();
        }
    }
}
