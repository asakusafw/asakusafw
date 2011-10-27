/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.runtime.util.VariableTable;


/**
 * Import処理/Export処理で扱うファイル名の作成・抽出等の操作を行うユーティリティクラス。
 * @author yuta.shirai
 */
public final class FileNameUtil {
    /**
     * このクラス。
     */
    private static final Class<?> CLASS = FileNameUtil.class;

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
        if (!fileDirectry.exists()) {
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
     * DFS上のImportファイルのファイルパス（フルパス）を生成して返す。
     * @param dfsFilePath DFS上のファイルパス
     * @param user OSのユーザー名
     * @param executionId 実行ID
     * @return フルパスに変換したDFS上のImportファイルパス
     * @throws BulkLoaderSystemException 不正なURIの場合
     */
    public static URI createDfsImportURI(
            String dfsFilePath,
            String executionId,
            String user) throws BulkLoaderSystemException {
        VariableTable variables = Constants.createVariableTable();
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_USER, user);
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_EXECUTION_ID, executionId);
        StringBuilder path = new StringBuilder();
        path.append(ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST));
        path.append(Constants.HDFSFIXED_PATH);
        path.append(variables.parse(dfsFilePath, false));
        try {
            return new URI(path.toString());
        } catch (URISyntaxException e) {
            throw new BulkLoaderSystemException(e, CLASS, "TG-COMMON-00018", path.toString());
        }
    }

    /**
     * DFS上のImportファイルのファイルパス（フルパス）を生成して返す。
     *
     * createDfsImportURIはDfs上でしか正常に動作しないパスを返すため、
     * スタンドアロンモードでも動作するためにワーキングディレクトリを使用する。
     * bulkloader-conf-hc.properties のプロパティ workingdir.use を true に設定すると
     * createDfsImportURIメソッドの代わりに本メソッドが呼ばれるようになる。
     *
     * @param dfsFilePath DFS/またはローカル上のファイルパス
     * @param executionId 実行ID
     * @return フルパスに変換したDFS/またはローカル上のファイルパス
     * @throws BulkLoaderSystemException 不正なURIの場合
     */
    public static URI createDfsImportURIWithWorkingDir(
            String dfsFilePath,
            String executionId) throws BulkLoaderSystemException {

        FileSystem fs = null;
        Path workingDirPath = null;
        Configuration conf = new Configuration();
        String path = ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST);
        try {
            // NOTE HADOOP_HOME/conf配下にクラスパスを追加するとStackOverFlowErrorが発生するため、設定ファイルから取得するようにしている。
//            fs = FileSystem.get(conf);
            fs = FileSystem.get(new URI(path), conf);

            fs.setWorkingDirectory(fs.getHomeDirectory());
            workingDirPath = fs.getWorkingDirectory();
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, CLASS, "TG-COMMON-00019", path);
        } catch (URISyntaxException e) {
            throw new BulkLoaderSystemException(e, CLASS, "TG-COMMON-00018", path);
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        VariableTable variables = Constants.createVariableTable();
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_USER, workingDirPath.toString());
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_EXECUTION_ID, executionId);
        String resolved = variables.parse(dfsFilePath, false);
        try {
            return new URI(resolved.substring(1)); // 先頭の[/]は除去する
        } catch (URISyntaxException e) {
            throw new BulkLoaderSystemException(e, CLASS, "TG-EXTRACTOR-02001",
                    // TODO MessageFormat.formatを検討
                    "HDFSに書き出すURIが不正。URI：" + dfsFilePath.substring(1));
        }
    }
    /**
     * DFS上のディレクトリパスをフルパスにして返す。
     * @param dfsFileDir DFS上のディレクトリパス
     * @param user OSのユーザー名
     * @param executionId ジョブフロー実行ID
     * @return DFS上のディレクトリパス（フルパス）
     */
    public static List<String> createDfsExportURI(List<String> dfsFileDir, String user, String executionId) {
        VariableTable variables = Constants.createVariableTable();
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_USER, user);
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_EXECUTION_ID, executionId);
        StringBuilder prefix = new StringBuilder();
        List<String> results = new ArrayList<String>();
        prefix.append(ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST));
        prefix.append(Constants.HDFSFIXED_PATH);
        for (String dirPath : dfsFileDir) {
            String expanded = variables.parse(dirPath, false);
            results.add(prefix + expanded);
        }
        return results;
    }

    /**
     * DFS上のEmportファイルのファイルパス（フルパス）を生成して返す。
     *
     * createDfsExportURIはDfs上でしか正常に動作しないパスを返すため、
     * スタンドアロンモードでも動作するためにワーキングディレクトリを使用する。
     * bulkloader-conf-hc.properties のプロパティ workingdir.use を true に設定すると
     * createDfsExportURIメソッドの代わりに本メソッドが呼ばれるようになる。
     *
     * @param dfsFileDir DFS/またはローカル上のディレクトリパス
     * @param executionId ジョブフロー実行ID
     * @return DFS/またはローカル上のディレクトリパス（フルパス）
     * @throws BulkLoaderSystemException 処理に失敗した場合
     */
    public static List<String> createDfsExportURIWithWorkingDir(
            List<String> dfsFileDir,
            String executionId)
        throws BulkLoaderSystemException {

        FileSystem fs = null;
        Path workingDirPath = null;
        Configuration conf = new Configuration();
        try {
            // NOTE HADOOP_HOME/conf配下にクラスパスを追加するとStackOverFlowErrorが発生するため、設定ファイルから取得するようにしている。
//            fs = FileSystem.get(conf);
            fs = FileSystem.get(
                    new URI(ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST)),
                    conf);
            fs.setWorkingDirectory(fs.getHomeDirectory());
            workingDirPath = fs.getWorkingDirectory();
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, CLASS, "TG-EXTRACTOR-02001",
                    "HDFSのファイルシステムの取得に失敗。URI："
                    + ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST));
        } catch (URISyntaxException e) {
            throw new BulkLoaderSystemException(e, CLASS, "TG-EXTRACTOR-02001",
                    "HDFSのパスが不正。URI："
                    + ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST));
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    throw new BulkLoaderSystemException(e, CLASS, "TG-EXTRACTOR-02001",
                            "HDFSのファイルシステムのクローズに失敗。URI："
                            + ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST));
                }
            }
        }

        VariableTable variables = Constants.createVariableTable();
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_USER, workingDirPath.toString());
        variables.defineVariable(Constants.HDFS_PATH_VARIABLE_EXECUTION_ID, executionId);
        List<String> results = new ArrayList<String>();
        for (String dirPath : dfsFileDir) {
            String expanded = variables.parse(dirPath, false);
            results.add(expanded.substring(1)); // 先頭の[/]は除去する
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
}
