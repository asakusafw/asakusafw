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
package com.asakusafw.testtools;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.asakusafw.testtools.db.DbUtilTest;
import com.asakusafw.testtools.excel.ExcelUtilsTest;
import com.asakusafw.testtools.inspect.CauseTest;
import com.asakusafw.testtools.inspect.DefaultInspectorTest;


@RunWith(Suite.class)
@SuiteClasses({
   DbUtilTest.class,
   TestDataHolderTest.class,
   ExcelUtilsTest.class,
   DefaultInspectorTest.class,
   TestUtilsTest.class,
   CauseTest.class
})

public class AllTests {
}
