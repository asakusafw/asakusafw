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
package com.asakusafw.cleaner.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

import com.asakusafw.cleaner.bean.DFSCleanerBean;
import com.asakusafw.cleaner.common.CleanerInitializer;
import com.asakusafw.cleaner.common.ConfigurationLoader;
import com.asakusafw.cleaner.common.Constants;
import com.asakusafw.cleaner.common.MessageIdConst;
import com.asakusafw.cleaner.exception.CleanerSystemException;
import com.asakusafw.cleaner.log.Log;

/**
 * HDFSCleanerの実行クラス。
 * @author yuta.shirai
 *
 */
public class HDFSCleaner extends Configured implements Tool {
    /** クラス。 */
    private static final Class<?> CLASS = HDFSCleaner.class;



    /**
     * Creates a new instance.
     */
    public HDFSCleaner() {
        super();
    }

    /**
     * Creates a new instance with configuration object.
     * @param conf configuration
     */
    public HDFSCleaner(Configuration conf) {
        super(conf);
    }

    /**
     * メインメソッド
     *
     * コマンドライン引数として以下の値をとる。
     * ・args[0]=動作モード
     * ・args[1]=ユーザー名
     * ・args[2]=コンフィグレーションファイル
     *
     * @param args コマンドライン引数
     * @throws Exception if failed to execute
     */
    public static void main(String[] args) throws Exception {
        HDFSCleaner tool = new HDFSCleaner();
        int result = tool.run(args);
        System.exit(result);
    }

    @Override
    public int run(String[] args) throws Exception {
        return execute(args);
    }

    /**
     * HDFSCleanerの処理を実行する。
     * @param args コマンドライン引数
     * @return 終了ステータス
     */
    protected int execute(String[] args) {
        String[] prop = new String[1];
        String mode = null;
        String user = null;
        FileSystem fs = null;

        if (args.length > 0) {
            mode = args[0];
        }
        if (args.length > 1) {
            user = args[1];
        }
        if (args.length > 2) {
            prop[0] = args[2];
        }

        // 引数の数をチェック
        if (args.length != 3) {
            System.err.println(
                    "ERROR：引数の数が不正です。 引数の数：" + args.length
                    + " 動作モード：" + mode
                    + " ユーザー名：" + user
                    + " コンフィグレーションファイル：" + prop[0]);
            Log.log(CLASS, MessageIdConst.HCLN_PARAMCHECK_ERROR, "引数の数", args.length, new Date(), mode, prop[0]);
            return Constants.EXIT_CODE_ERROR;
        }

        try {
            // 初期処理
            if (!CleanerInitializer.initDFSCleaner(prop)) {
                Log.log(CLASS, MessageIdConst.HCLN_INIT_ERROR, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_ERROR;
            }

            // 開始ログ出力
            Log.log(CLASS, MessageIdConst.HCLN_START, new Date(), mode, prop[0]);

            // 動作モードを取得
            boolean recursive = false;
            if (Constants.CLEAN_MODE_NOMAL.equals(mode)) {
                recursive = false;
            } else if (Constants.CLEAN_MODE_RECURSIVE.equals(mode)) {
                recursive = true;
            } else {
                Log.log(CLASS, MessageIdConst.HCLN_PARAMCHECK_ERROR, "動作モード", mode, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_ERROR;
            }

            // HDFS上のクリーニング対象ディレクトリを取得
            DFSCleanerBean[] bean = null;
            try {
                bean = getCleanLocalPath(user);
            } catch (CleanerSystemException e) {
                Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
                return Constants.EXIT_CODE_ERROR;
            }

            // 保持期間を取得
            int keepDate = getHDFSFileKeepDate();

            boolean cleanResult = true;
            Date now = new Date();
            for (int i = 0; i < bean.length; i++) {
                try {
                    // クリーニングを実行
                    Path cleanDir = bean[i].getCleanDir();
                    // ファイルシステムを取得
                    try {
                        Configuration conf = getConf();
                        fs = cleanDir.getFileSystem(conf);
                        if (fs == null) {
                            Log.log(CLASS, MessageIdConst.HCLN_CLEN_DIR_ERROR,
                                    "Path.getFileSystemの戻り値がnull", cleanDir.toString());
                            cleanResult = false;
                            continue;
                        }
                    } catch (IOException e) {
                        Log.log(e, CLASS, MessageIdConst.HCLN_CLEN_DIR_ERROR,
                                "HDFSのファイルシステムの取得に失敗", cleanDir.toString());
                        cleanResult = false;
                        continue;
                    }

                    boolean target = bean[i].hasExecutionId();
                    String pattern = bean[i].getPattern();
                    Log.log(
                            CLASS,
                            MessageIdConst.HCLN_CLEN_FILE,
                            cleanDir.toString(),
                            pattern,
                            keepDate,
                            mode,
                            target,
                            now);
                    if (cleanDir(fs, cleanDir, target, pattern, keepDate, now, recursive)) {
                        Log.log(CLASS, MessageIdConst.HCLN_CLEN_DIR_SUCCESS, cleanDir.toString(), keepDate, mode);
                    } else {
                        Log.log(CLASS, MessageIdConst.HCLN_CLEN_DIR_FAIL, cleanDir.toString(), keepDate, mode);
                        cleanResult = false;
                    }
                } catch (CleanerSystemException e) {
                    Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
                    cleanResult = false;
                } finally {
                    if (fs != null) {
                        // CHECKSTYLE:OFF EmptyBlockCheck
                        try {
                            fs.close();
                        } catch (IOException ignored) {
                            // ignored
                        }
                        // CHECKSTYLE:ON EmptyBlockCheck
                    }
                }
            }

            // 正常終了
            if (cleanResult) {
                Log.log(CLASS, MessageIdConst.HCLN_EXIT_SUCCESS, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_SUCCESS;
            } else {
                Log.log(CLASS, MessageIdConst.HCLN_EXIT_WARNING, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_WARNING;
            }
        } catch (RuntimeException e) {
            try {
                Log.log(e, CLASS, MessageIdConst.HCLN_EXCEPRION, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_ERROR;
            } catch (Exception e1) {
                System.err.print("HDFSCleanerで不明なエラーが発生しました。");
                e1.printStackTrace();
                return Constants.EXIT_CODE_ERROR;
            }
        }
    }
    /**
     * クリーニング対象ディレクトリをクリーニングする。
     * @param fs HDFSを表すファイルシステム
     * @param cleanPath HDFS上のクリーニング対象ディレクトリのパス
     * @param isSetExecutionId ジョブフロー実行IDが指定されているかどうか
     * @param pattern クリーニングパターン
     * @param keepDate 保持日数
     * @param now 現在日時
     * @param recursive 再帰的にクリーニングを行うか
     * @return クリーニング結果
     * @throws CleanerSystemException
     */
    private boolean cleanDir(
            FileSystem fs,
            Path cleanPath,
            boolean isSetExecutionId,
            String pattern,
            int keepDate,
            Date now,
            boolean recursive) throws CleanerSystemException {
        try {
            if (!fs.exists(cleanPath)) {
                // 指定されたディレクトリが存在しない
                Log.log(CLASS, MessageIdConst.HCLN_CLEN_DIR_ERROR, "指定されたディレクトリが存在しない", cleanPath.toString());
                return false;
            }
            if (!fs.getFileStatus(cleanPath).isDir()) {
                // 指定されたパスがディレクトリでない
                Log.log(CLASS, MessageIdConst.HCLN_CLEN_DIR_ERROR, "指定されたパスがディレクトリでない", cleanPath.toString());
                return false;
            }

            // クリーニングを行う
            Log.log(CLASS, MessageIdConst.HCLN_FILE_DELETE, cleanPath.toString());
            int cleanFileCount = 0;
            int cleanDirCount = 0;
            boolean result = true;
            FileStatus[] dirStatus = getListStatus(fs, cleanPath);
            Path[] listedPaths = FileUtil.stat2Paths(dirStatus);
            for (Path path : listedPaths) {
                FileStatus status = fs.getFileStatus(path);
                long lastModifiedTime = status.getModificationTime();
                if (status.isDir() && recursive) {
                    // ディレクトリかつ、再帰的に処理を行う場合
                    if (isSetExecutionId) {
                        // ジョブフロー実行IDが指定されている場合はMMに問い合わせを行う
                        String executionId = path.getName();
                        if (isRunningJobFlow(executionId)) {
                            // 実行中のジョブフローの為、クリーニング対象としない。
                            Log.log(CLASS, MessageIdConst.HCLN_CLEN_DIR_EXEC, path.toString());
                            continue;
                        }
                    }
                    FileStatus[] childdirStatus = getListStatus(fs, path);
                    if (childdirStatus.length == 0) {
                        // 子ファイルが存在しない場合、ディレクトリを削除
                        if (isExpired(lastModifiedTime, keepDate, now)) {
                            if (!fs.delete(path, false)) {
                                Log.log(CLASS, MessageIdConst.HCLN_CLEN_FAIL, "ディレクトリ", path.toString());
                                result = false;
                            } else {
                                cleanDirCount++;
                                Log.log(CLASS, MessageIdConst.HCLN_DIR_DELETE, path.toString());
                            }
                        }
                    } else {
                        // 子ファイルが存在する場合、再帰的にクリーニング処理を行う。
                        if (cleanDir(fs, path, false, pattern, keepDate, now, recursive)) {
                            // 子ファイルが全て削除された場合はディレクトリを削除する
                            childdirStatus = getListStatus(fs, path);
                            if (childdirStatus.length == 0) {
                                if (isExpired(lastModifiedTime, keepDate, now)) {
                                    if (!fs.delete(path, false)) {
                                        Log.log(CLASS, MessageIdConst.HCLN_CLEN_FAIL, "ディレクトリ", path.toString());
                                        result = false;
                                    } else {
                                        cleanDirCount++;
                                        Log.log(CLASS, MessageIdConst.HCLN_DIR_DELETE, path.toString());
                                    }
                                }
                            }
                        } else {
                            Log.log(CLASS, MessageIdConst.HCLN_CLEN_FAIL, "ディレクトリ", path.toString());
                            result = false;
                        }
                    }
                } else if (!status.isDir()) {
                    // ファイルの場合、保持期間を過ぎていれば削除する
                    if (isExpired(lastModifiedTime, keepDate, now) && isMatchPattern(path, pattern)) {
                        if (!fs.delete(path, false)) {
                            Log.log(CLASS, MessageIdConst.HCLN_CLEN_FAIL, "ファイル", path.toString());
                            result = false;
                        } else {
                            Log.log(CLASS, MessageIdConst.HCLN_DELETE_FILE, path.toString());
                            cleanFileCount++;
                        }
                    }
                }
            }

            Log.log(
                    CLASS,
                    MessageIdConst.HCLN_FILE_DELETE_SUCCESS,
                    cleanPath.toString(),
                    cleanDirCount,
                    cleanFileCount);

            return result;
        } catch (IOException e) {
            Log.log(e, CLASS, MessageIdConst.HCLN_CLEN_DIR_EXCEPTION, cleanPath.getName());
            return false;
        }
    }
    /**
     * 子ファイルを取得する。
     * @param fs ファイルシステム
     * @param path 親ディレクトリ
     * @return 子ファイル
     * @throws IOException
     */
    private FileStatus[] getListStatus(FileSystem fs, Path path) throws IOException {
        FileStatus[] status;
        try {
            status = fs.listStatus(path);
        } catch (FileNotFoundException e) {
            status = null;
        }
        if (status == null) {
            status = new FileStatus[0];
        }
        return status;
    }
    /**
     * 削除対象のファイルがパターンにマッチするか判断する。
     * @param path 削除対象ファイル
     * @param pattern 削除対象ファイルパターン
     * @return 削除可否
     * @throws CleanerSystemException
     */
    private boolean isMatchPattern(Path path, String pattern) throws CleanerSystemException {
        if (pattern == null || pattern.equals("")) {
            return true;
        } else {
            String strFile = path.toString();
            try {
                Matcher m = Pattern.compile(pattern).matcher(strFile);
                return m.matches();
            } catch (PatternSyntaxException e) {
                throw new CleanerSystemException(e, this.getClass(), MessageIdConst.HCLN_PATTERN_FAIL, pattern);
            }
        }
    }
    /**
     * 保持日付を判断し、ファイルが削除可能か判断する。
     * @param lastModifiedTime ファイルの最終更新日時
     * @param keepDate 保持日数
     * @param now 現在日時
     * @return 削除可否
     */
    private boolean isExpired(long lastModifiedTime, int keepDate, Date now) {
        long keepTime = (keepDate) * 24L * 60L * 60L * 1000L;
        long period = lastModifiedTime + keepTime;
        return now.getTime() > period;
    }
    /**
     * 当該ジョブフローインスタンスを実行中か問い合わせる。
     * @param executionId ジョブフロー実行ID
     * @return 実行中の場合:true、実行中でない場合:false
     */
    protected boolean isRunningJobFlow(String executionId) {
        // TODO 未実装状態。要実装。
        return false;
    }
    /**
     * プロパティからクリーニング対象ディレクトリの保持日数を取得する。
     * @return クリーニング対象ディレクトリの保持日数
     */
    private int getHDFSFileKeepDate() {
        return Integer.parseInt(ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_FILE_KEEP_DATE));
    }
    /**
     * プロパティからクリーニング対象パスを取得し、
     * HDFS上のディレクトリパスをフルパスにして返す。
     * @param user ユーザー名
     * @return クリーニング対象パス
     * @throws CleanerSystemException クリーニングパターンが存在しない場合
     */
    private DFSCleanerBean[] getCleanLocalPath(String user) throws CleanerSystemException {
        // プロパティからクリーニング対象パスを取得する
        List<String> cleanDirList = ConfigurationLoader.getPropStartWithString(
                Constants.PROP_KEY_HDFS_FILE_CLEAN_DIR + ".");
        List<String> noEmptyDirList = ConfigurationLoader.getNoEmptyList(cleanDirList);

        List<DFSCleanerBean> list = new ArrayList<DFSCleanerBean>();
        int listSize = noEmptyDirList.size();
        for (int i = 0; i < listSize; i++) {
            // クリーニングディレクトリを設定
            DFSCleanerBean bean = new DFSCleanerBean();
            String dirKey = noEmptyDirList.get(i);
            // ユーザー名を表す文字列を置換
            String strPath = ConfigurationLoader.getProperty(dirKey);
            String strCleanPath = strPath.replace(Constants.HDFS_PATH_REPLACE_STR_USER, user);
            // ジョブフロー実行IDを表す文字列を排除
            boolean isSetexecutionId = false;
            if (strCleanPath.endsWith(Constants.HDFS_PATH_REPLACE_STR_ID)) {
                isSetexecutionId = true;
                strCleanPath = strCleanPath.substring(0, strCleanPath.indexOf(Constants.HDFS_PATH_REPLACE_STR_ID) - 1);
            }
            bean.setCleanDir(createPath(strCleanPath));
            bean.setExecutionId(isSetexecutionId);

            // クリーニングパターンを設定
            String number = dirKey.substring(dirKey.lastIndexOf(".") + 1, dirKey.length());
            String pattarnKey = Constants.PROP_KEY_HDFS_FILE_CLEAN_PATTERN + "." + number;
            String pattern = ConfigurationLoader.getProperty(pattarnKey);
            if (pattern == null || pattern.equals("")) {
                throw new CleanerSystemException(
                        this.getClass(),
                        MessageIdConst.HCLN_PATTERN_NOT_FOUND,
                        dirKey,
                        strPath,
                        pattarnKey);
            } else {
                bean.setPattern(pattern);
            }

            list.add(bean);
        }
        return list.toArray(new DFSCleanerBean[list.size()]);
    }
    /**
     * パスをフルパスに組み立てて返す。
     * @param strCleanPath クリーニングパス
     * @return フルパス
     */
    protected Path createPath(String strCleanPath) {
        StringBuffer path = new StringBuffer(ConfigurationLoader.getProperty(Constants.PROP_KEY_HDFS_PROTCOL_HOST));
        path.append(Constants.HDFSFIXED_PATH);
        path.append(strCleanPath);
        return new Path(path.toString());
    }
}
