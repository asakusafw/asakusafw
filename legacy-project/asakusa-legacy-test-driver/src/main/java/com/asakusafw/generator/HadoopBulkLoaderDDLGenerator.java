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
package com.asakusafw.generator;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * HadoopBulkLoader用のテーブルジェネレータ。
 */
public final class HadoopBulkLoaderDDLGenerator {

    /**
     * 利用する全ての環境変数名の接頭辞。
     */
    private static final String[] ENV_PREFIX = { "ASAKUSA_", "NS_" };

    private static final List<String> ENV_BULKLOADER_GENDDL = buildEnvProperties("BULKLOADER_GENDDL");

    private static final List<String> ENV_BULKLOADER_TABLES = buildEnvProperties("BULKLOADER_TABLES");

    private static final String TEMPLATE_DDL_FILENAME = "hadoopbulkloader_ddltemplate.sql";

    private static final String TABLENAME_REPLACE_STRING = "@TABLE_NAME@";

    private static final String OUTPUT_TABLES_PROPERTY = "${asakusa.bulkloader.tables}";

    /**
     * HadoopBulkLoader用のテーブルを生成します。
     *
     * @param args
     *            テーブル名
     * @throws Exception
     *             テーブルの生成に失敗した場合
     */
    public static void main(String[] args) throws Exception {
        String outputTablesString = findVariable(ENV_BULKLOADER_TABLES, true);
        List<String> outputTableList = null;
        if (outputTablesString != null
                && !OUTPUT_TABLES_PROPERTY.equals(outputTablesString)) {
            String[] outputTables = outputTablesString.trim().split("\\s+");
            outputTableList = Arrays.asList(outputTables);
        }

        String ddlTemplate;
        InputStream in = HadoopBulkLoaderDDLGenerator.class
                .getResourceAsStream(TEMPLATE_DDL_FILENAME);
        try {
            ddlTemplate = IOUtils.toString(in);
        } finally {
            in.close();
        }
        StringBuilder sb = new StringBuilder();

        for (String tableName : args) {
            if (outputTableList == null || outputTableList.contains(tableName)) {
                String tableddl = ddlTemplate.replaceAll(
                        TABLENAME_REPLACE_STRING, tableName);
                sb.append(tableddl);
            }
        }

        String outputFilePath = findVariable(ENV_BULKLOADER_GENDDL, true);
        if (outputFilePath == null) {
            throw new RuntimeException(
                    "システムプロパティ「ASAKUSA_BULKLOADER_TABLES」が設定されていません");
        }
        FileUtils.write(new File(outputFilePath), sb);
    }

    private HadoopBulkLoaderDDLGenerator() {
        return;
    }

    private static List<String> buildEnvProperties(String suffix) {
        assert suffix != null;
        List<String> properties = new ArrayList<String>(ENV_PREFIX.length);
        for (String prefix : ENV_PREFIX) {
            properties.add(prefix + suffix);
        }
        return Collections.unmodifiableList(properties);
    }

    private static String findVariable(List<String> variableNames,
            boolean mandatory) {
        assert variableNames != null;
        assert variableNames.isEmpty() == false;
        String value = null;
        for (String var : variableNames) {
            value = System.getProperty(var);
            if (value == null) {
                value = System.getenv(var);
            }
            if (value != null) {
                break;
            }
        }
        if (mandatory && value == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "環境変数\"{0}\"が設定されていません", variableNames.get(0)));
        }
        return value;
    }

}
