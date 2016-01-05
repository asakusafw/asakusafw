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
package com.asakusafw.bulkloader.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.util.VariableTable;


/**
 * Import処理/Export処理で扱うファイル名の作成・抽出等の操作を行うユーティリティクラス。
 * @author yuta.shirai
 * @since 0.1.0
 * @version 0.4.0
 */
public final class FileNameUtil {
    /**
     * このクラス。
     */
    private static final Class<?> CLASS = FileNameUtil.class;

    private static final Log LOG = new Log(CLASS);

    private FileNameUtil() {
        return;
    }

    /**
     * ディレクトリを含んだImportファイルのPathを作成する。
     * @param targetName ターゲット名
     * @param jobflowId ジョブフローID
     * @param executionId 実行ID
     * @param tableName Import対象テーブル名
     * @return Importファイルパス
     * @throws BulkLoaderSystemException Importファイル生成ディレクトリが存在しない場合
     */
    public static File createImportFilePath(
            String targetName,
            String jobflowId,
            String executionId,
            String tableName) throws BulkLoaderSystemException {
        File fileDirectry = new File(ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_FILE_DIR));
        if (!prepareTemporaryDirectory(fileDirectry)) {
            // ディレクトリが存在しない場合は異常終了する。
            throw new BulkLoaderSystemException(CLASS, "TG-COMMON-00017",
                    fileDirectry.getAbsolutePath());
        }

        StringBuilder strFileName = new StringBuilder(Constants.IMPORT_FILE_PREFIX);
        strFileName.append(Constants.IMPORT_FILE_DELIMITER);
        strFileName.append(targetName);
        strFileName.append(Constants.IMPORT_FILE_DELIMITER);
        strFileName.append(jobflowId);
        strFileName.append(Constants.IMPORT_FILE_DELIMITER);
        strFileName.append(executionId);
        strFileName.append(Constants.IMPORT_FILE_DELIMITER);
        strFileName.append(tableName);
        strFileName.append(Constants.IMPORT_FILE_EXTENSION);

        return new File(fileDirectry, strFileName.toString());
    }

    /**
     * Extractorに送信するImportファイル名を作成する。
     * @param tableName Import対象テーブル名
     * @return Importファイル名
     */
    public static String createSendImportFileName(String tableName) {
        StringBuilder strFileNmae = new StringBuilder(Constants.IMPORT_FILE_PREFIX);
        strFileNmae.append(Constants.IMPORT_FILE_DELIMITER);
        strFileNmae.append(tableName);
        strFileNmae.append(Constants.EXPORT_FILE_EXTENSION);
        return strFileNmae.toString();
    }
    /**
     * Importerから受信したImportファイル名からテーブル名を取出す。
     * @param fileName ファイル名
     * @return テーブル名
     */
    public static String getImportTableName(String fileName) {
        String normalized = new File(fileName).getName().replace(File.separatorChar, '/');
        int start = Constants.IMPORT_FILE_PREFIX.length() + Constants.IMPORT_FILE_DELIMITER.length();
        int end = normalized.length() - Constants.IMPORT_FILE_EXTENSION.length();
        return normalized.substring(start, end);
    }

    /**
     * Resolves the raw path.
     * @param conf current configuration
     * @param rawPath raw path
     * @param executionId current execution ID
     * @param user current user name
     * @return the resolved full path
     * @throws BulkLoaderSystemException if failed to resolve the path
     * @since 0.4.0
     */
    public static Path createPath(
            Configuration conf,
            String rawPath,
            String executionId,
            String user) throws BulkLoaderSystemException {
        return createPaths(conf, Collections.singletonList(rawPath), executionId, user).get(0);
    }

    /**
     * Resolves the raw path.
     * @param conf current configuration
     * @param rawPaths raw paths
     * @param executionId current execution ID
     * @param user current user name
     * @return the resolved full path
     * @throws BulkLoaderSystemException if failed to resolve the path
     * @since 0.4.0
     */
    public static List<Path> createPaths(
            Configuration conf,
            List<String> rawPaths,
            String executionId,
            String user) throws BulkLoaderSystemException {
        String basePathString = ConfigurationLoader.getProperty(Constants.PROP_KEY_BASE_PATH);
        Path basePath;
        if (basePathString == null || basePathString.isEmpty()) {
            basePath = null;
        } else {
            basePath = new Path(basePathString);
        }
        VariableTable variables = Constants.createVariableTable();
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_USER, user);
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_EXECUTION_ID, executionId);
        FileSystem fs;
        try {
            if (basePath == null) {
                fs = FileSystem.get(conf);
            } else {
                fs = FileSystem.get(basePath.toUri(), conf);
                basePath = fs.makeQualified(basePath);
            }
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, CLASS, "TG-COMMON-00019", rawPaths);
        }
        List<Path> results = new ArrayList<Path>();
        for (String rawPath : rawPaths) {
            String resolved = variables.parse(rawPath, false);
            Path fullPath;
            if (basePath == null) {
                fullPath = fs.makeQualified(new Path(resolved));
            } else {
                fullPath = new Path(basePath, resolved);
            }
            results.add(fullPath);
        }
        return results;
    }

    /**
     * Exporterに送信するExportファイル名を作成する。
     * exportファイル名を組み立てて返す
     * @param tableName テーブル名
     * @param fileNameMap テーブル名とシーケンスを保持するマップ
     * @return Exportファイル名
     */
    public static String createSendExportFileName(String tableName, Map<String, Integer> fileNameMap) {
        Integer seq = fileNameMap.get(tableName);
        if (seq == null) {
            seq = Integer.valueOf(1);
        } else {
            seq++;
        }

        fileNameMap.put(tableName, seq);
        StringBuilder strFileNmae = new StringBuilder(Constants.EXPORT_FILE_PREFIX);
        strFileNmae.append(Constants.EXPORT_FILE_DELIMITER);
        strFileNmae.append(tableName);
        strFileNmae.append(Constants.EXPORT_FILE_DELIMITER);
        strFileNmae.append(seq.toString());
        strFileNmae.append(Constants.EXPORT_FILE_EXTENSION);
        return strFileNmae.toString();
    }
    /**
     * Collectorから受信したExportファイル名からテーブル名を取出す。
     * ファイル名からテーブル名を分解して返す
     * @param fileName ファイル名
     * @return テーブル名
     */
    public static String getExportTableName(String fileName) {
        String normalized = new File(fileName).getName().replace(File.separatorChar, '/');
        int start = Constants.EXPORT_FILE_PREFIX.length() + Constants.EXPORT_FILE_DELIMITER.length();
        int end = normalized.lastIndexOf(Constants.EXPORT_FILE_DELIMITER);
        try {
            return normalized.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * exportTSV中間ファイル名を組み立てて返す。
     * @param fileDirectry ファイル生成ディレクトリ
     * @param targetName ターゲット名
     * @param jobflowId ジョブフローID
     * @param executionId ジョブフロー実行ID
     * @param tableName テーブル名
     * @param seq シーケンス番号
     * @return exportファイル名(フルパス)
     */
    public static File createExportFilePath(
            File fileDirectry,
            String targetName,
            String jobflowId,
            String executionId,
            String tableName,
            int seq) {
        StringBuilder strFileName = new StringBuilder(Constants.EXPORT_FILE_PREFIX);
        strFileName.append(Constants.EXPORT_FILE_DELIMITER);
        strFileName.append(targetName);
        strFileName.append(Constants.EXPORT_FILE_DELIMITER);
        strFileName.append(jobflowId);
        strFileName.append(Constants.EXPORT_FILE_DELIMITER);
        strFileName.append(executionId);
        strFileName.append(Constants.EXPORT_FILE_DELIMITER);
        strFileName.append(tableName);
        strFileName.append(Constants.EXPORT_FILE_DELIMITER);
        strFileName.append(String.valueOf(seq));
        strFileName.append(Constants.EXPORT_FILE_EXTENSION);
        return new File(fileDirectry, strFileName.toString());
    }

    /**
     * Prepares the temporary directory.
     * @param directory the target directory path
     * @return {@code true} if successfully prepared, otherwise {@code false}
     */
    public static boolean prepareTemporaryDirectory(File directory) {
        if (directory.mkdirs() || directory.isDirectory()) {
            if (directory.setReadable(true, false) == false && directory.canRead() == false) {
                LOG.debugMessage("Failed to set readable: {0}", directory);
            }
            if (directory.setWritable(true, false) == false && directory.canWrite() == false) {
                LOG.debugMessage("Failed to set writable: {0}", directory);
            }
            return true;
        }
        return false;
    }
}
