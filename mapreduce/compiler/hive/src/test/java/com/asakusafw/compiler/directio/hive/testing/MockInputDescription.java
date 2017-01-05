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
package com.asakusafw.compiler.directio.hive.testing;

import com.asakusafw.directio.hive.info.InputInfo;
import com.asakusafw.directio.hive.info.LocationInfo;
import com.asakusafw.directio.hive.info.TableInfo;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

@SuppressWarnings("javadoc")
public abstract class MockInputDescription extends DirectFileInputDescription {

    private final Class<? extends MockDataFormat> format;

    MockInputDescription(Class<? extends MockDataFormat> format) {
        this.format = format;
    }

    public InputInfo toInfo() {
        try {
            return new InputInfo(
                    new LocationInfo(getBasePath(), getResourcePattern()),
                    getFormat()
                        .asSubclass(TableInfo.Provider.class)
                        .newInstance()
                        .getSchema());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Class<?> getModelType() {
        return MockDataModel.class;
    }

    @Override
    public String getBasePath() {
        return "in/" + getClass().getSimpleName();
    }

    @Override
    public String getResourcePattern() {
        return "*.mock";
    }

    @Override
    public Class<? extends DataFormat<?>> getFormat() {
        return format;
    }

    public static class A extends MockInputDescription {
        public A() {
            super(MockDataFormat.A.class);
        }
    }

    public static class B extends MockInputDescription {
        public B() {
            super(MockDataFormat.B.class);
        }
    }

    public static class C extends MockInputDescription {
        public C() {
            super(MockDataFormat.C.class);
        }
    }

    public static class D extends MockInputDescription {
        public D() {
            super(MockDataFormat.D.class);
        }
    }
}
