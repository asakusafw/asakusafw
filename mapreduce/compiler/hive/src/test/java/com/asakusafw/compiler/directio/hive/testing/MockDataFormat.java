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

import com.asakusafw.directio.hive.info.FieldType.TypeName;
import com.asakusafw.directio.hive.info.TableInfo;
import com.asakusafw.runtime.directio.DataFormat;

@SuppressWarnings("javadoc")
public abstract class MockDataFormat implements DataFormat<MockDataModel>, TableInfo.Provider {

    MockDataFormat() {
        return;
    }

    @Override
    public Class<MockDataModel> getSupportedType() {
        return MockDataModel.class;
    }

    @Override
    public TableInfo getSchema() {
        return new TableInfo.Builder(getClass().getSimpleName())
                .withColumn("COL", TypeName.INT)
                .build();
    }

    public static final class A extends MockDataFormat {
        public A() {
            return;
        }
    }

    public static final class B extends MockDataFormat {
        public B() {
            return;
        }
    }

    public static final class C extends MockDataFormat {
        public C() {
            return;
        }
    }

    public static final class D extends MockDataFormat {
        public D() {
            return;
        }
    }
}
