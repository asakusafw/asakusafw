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
package com.asakusafw.vocabulary.windgate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport;
import com.asakusafw.windgate.core.vocabulary.FileProcess;
import com.asakusafw.windgate.core.vocabulary.StreamProcess;

/**
 * Test for {@link FsImporterDescription}.
 */
public class FsImporterDescriptionTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Mock desc = new Mock("example", StringSupport.class);
        DriverScript script = desc.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.LOCAL_FILE_RESOURCE_NAME));
        assertThat(script.getConfiguration(), hasEntry(FileProcess.FILE.key(), "example"));
        assertThat(script.getConfiguration(), hasEntry(StreamProcess.STREAM_SUPPORT.key(), StringSupport.class.getName()));
        assertThat(script.getParameterNames(), hasSize(0));
    }

    /**
     * w/ variables.
     */
    @Test
    public void parameters() {
        Mock desc = new Mock("testing/${var}", StringSupport.class);
        DriverScript script = desc.getDriverScript();
        assertThat(script.getResourceName(), is(Constants.LOCAL_FILE_RESOURCE_NAME));
        assertThat(script.getConfiguration(), hasEntry(FileProcess.FILE.key(), "testing/${var}"));
        assertThat(script.getConfiguration(), hasEntry(StreamProcess.STREAM_SUPPORT.key(), StringSupport.class.getName()));
        assertThat(script.getParameterNames(), containsInAnyOrder("var"));
    }

    /**
     * path is null.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_path_null() {
        Mock desc = new Mock(null, StringSupport.class);
        desc.getDriverScript();
    }

    /**
     * path is empty.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_path_empty() {
        Mock desc = new Mock("", StringSupport.class);
        desc.getDriverScript();
    }

    /**
     * support class is null.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_support_null() {
        Mock desc = new Mock("example", null);
        desc.getDriverScript();
    }

    /**
     * support class does not support model type.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_support_inconsistent() {
        Mock desc = new Mock("example", VoidSupport.class);
        desc.getDriverScript();
    }

    /**
     * cannot instantiate the support class.
     */
    @Test(expected = IllegalStateException.class)
    public void invalid_support_fail() {
        Mock desc = new Mock("example", InvalidSupport.class);
        desc.getDriverScript();
    }

    /**
     * String support.
     */
    public static class StringSupport extends MockStreamSupport<String> {
        @Override
        public Class<String> getSupportedType() {
            return String.class;
        }
    }

    /**
     * Void support.
     */
    public static class VoidSupport extends MockStreamSupport<Void> {
        @Override
        public Class<Void> getSupportedType() {
            return Void.class;
        }
    }

    /**
     * Invalid support.
     */
    private static class InvalidSupport extends MockStreamSupport<Object> {
        @Override
        public Class<Object> getSupportedType() {
            return Object.class;
        }
    }

    private static final class Mock extends FsImporterDescription {

        private final String path;

        private final Class<? extends DataModelStreamSupport<?>> supportClass;

        Mock(String path, Class<? extends DataModelStreamSupport<?>> supportClass) {
            this.path = path;
            this.supportClass = supportClass;
        }

        @Override
        public String getProfileName() {
            return "testing";
        }

        @Override
        public Class<?> getModelType() {
            return String.class;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public Class<? extends DataModelStreamSupport<?>> getStreamSupport() {
            return supportClass;
        }
    }
}
