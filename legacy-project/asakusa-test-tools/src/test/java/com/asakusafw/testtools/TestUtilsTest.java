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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import test.inspector.SuccessInspector;

import com.asakusafw.testtools.inspect.Cause;


public class TestUtilsTest {


    /**
     * 通常想定する使い方で、TestUtilが動作することの確認
     * @throws Exception
     */
    @Test
    public void testNormal() throws Exception {
        String TEST_FILE = "src/test/data/Excel/ExcelUtils/TEST_UTIL_NORMAL.xls";
        File testFile = new File(TEST_FILE);
        List<File> testFileList = new ArrayList<File>();
        testFileList.add(testFile);
        TestUtils testUtils = new TestUtils(testFileList);
        testUtils.storeToDatabase(true);
        testUtils.loadFromDatabase();
        testUtils.inspect();
        for (Cause cause : testUtils.getCauses()) {
            System.out.println(cause.getMessage());
        }
        Assert.assertEquals("検査NGの数", 0, testUtils.getCauses().size());
    }

    /**
     * Custom Inspectorを使用するテスト
     * @throws Exception
     */
    @Test
    public void testCustomInspector() throws Exception {
        String TEST_FILE = "src/test/data/Excel/ExcelUtils/TEST_UTIL_NORMAL.xls";
        File testFile = new File(TEST_FILE);
        List<File> testFileList = new ArrayList<File>();
        testFileList.add(testFile);
        TestUtils testUtils = new TestUtils(testFileList);
        testUtils.storeToDatabase(true);
        testUtils.loadFromDatabase();
        SuccessInspector successInspector = new SuccessInspector();
        testUtils.setInspector("ALL_TYPES_W_NOERR", successInspector);
        testUtils.inspect();
        for (Cause cause : testUtils.getCauses()) {
            System.out.println(cause.getMessage());
        }
        Assert.assertEquals("検査NGの数", 0, testUtils.getCauses().size());
    }

}
