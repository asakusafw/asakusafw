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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.asakusafw.cleaner.bean.LocalFileCleanerBean;
import com.asakusafw.cleaner.common.CleanerInitializer;
import com.asakusafw.cleaner.common.ConfigurationLoader;
import com.asakusafw.cleaner.common.Constants;
import com.asakusafw.cleaner.common.MessageIdConst;
import com.asakusafw.cleaner.exception.CleanerSystemException;
import com.asakusafw.cleaner.log.Log;


/**
 * LocalFileCleanerの実行クラス。
 * @author yuta.shirai
 *
 */
public class LocalFileCleaner {
    /** クラス。 */
    private static final Class<?> CLASS = LocalFileCleaner.class;

    /**
     * メインメソッド
     *
     * コマンドライン引数として以下の値をとる。
     * ・args[0]=動作モード
     * ・args[1]=コンフィグレーションファイル
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        LocalFileCleaner cleaner = new LocalFileCleaner();
        int result = cleaner.execute(args);
        System.exit(result);
    }
    /**
     * LocalFileCleanerの処理を実行する。
     * @param args コマンドライン引数
     * @return 終了ステータス
     */
    protected int execute(String[] args) {
        String[] prop = new String[1];
        String mode = null;
        if (args.length > 0) {
            mode = args[0];
        }
        if (args.length > 1) {
            prop[0] = args[1];
        }

        // 引数の数をチェック
        if (args.length != 2) {
            System.err.println(
                    "ERROR：引数の数が不正です。 引数の数：" + args.length
                    + " 動作モード：" + mode + " コンフィグレーションファイル：" + prop[0]);
            return Constants.EXIT_CODE_ERROR;
        }

        try {
            // 初期処理
            if (!CleanerInitializer.initLocalFileCleaner(prop)) {
                Log.log(CLASS, MessageIdConst.LCLN_INIT_ERROR, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_ERROR;
            }

            // 開始ログ出力
            Log.log(CLASS, MessageIdConst.LCLN_START, new Date(), mode, prop[0]);

            // 動作モードを取得
            boolean recursive = false;
            if (Constants.CLEAN_MODE_NOMAL.equals(mode)) {
                recursive = false;
            } else if (Constants.CLEAN_MODE_RECURSIVE.equals(mode)) {
                recursive = true;
            } else {
                Log.log(CLASS, MessageIdConst.LCLN_PARAMCHECK_ERROR, "動作モード", mode, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_ERROR;
            }

            // クリーニング対象ディレクトリを取得
            LocalFileCleanerBean[] bean = null;
            try {
                bean = getCleanLocalDirs();
            } catch (CleanerSystemException e) {
                Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
                return Constants.EXIT_CODE_ERROR;
            }

            // 保持期間を取得
            int keepDate = getLocalFileKeepDate();

            // クリーニングを実行
            boolean cleanResult = true;
            Date now = new Date();
            for (int i = 0; i < bean.length; i++) {
                try {
                    Log.log(
                            CLASS,
                            MessageIdConst.LCLN_CLEN_FILE,
                            bean[i].getCleanDir().getAbsolutePath(),
                            bean[i].getPattern(),
                            keepDate,
                            mode,
                            now);
                    if (cleanDir(bean[i].getCleanDir(), bean[i].getPattern(), keepDate, now, recursive)) {
                        Log.log(
                                CLASS,
                                MessageIdConst.LCLN_CLEN_DIR_SUCCESS,
                                bean[i].getCleanDir().getAbsolutePath(),
                                keepDate,
                                mode);
                    } else {
                        Log.log(
                                CLASS,
                                MessageIdConst.LCLN_CLEN_DIR_FAIL,
                                bean[i].getCleanDir().getAbsolutePath(),
                                keepDate,
                                mode);
                        cleanResult = false;
                    }
                } catch (CleanerSystemException e) {
                    Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
                    cleanResult = false;
                }
            }

            // 正常終了
            if (cleanResult) {
                Log.log(CLASS, MessageIdConst.LCLN_EXIT_SUCCESS, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_SUCCESS;
            } else {
                Log.log(CLASS, MessageIdConst.LCLN_EXIT_WARNING, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_WARNING;
            }

        } catch (Exception e) {
            try {
                Log.log(e, CLASS, MessageIdConst.LCLN_EXCEPRION, new Date(), mode, prop[0]);
                return Constants.EXIT_CODE_ERROR;
            } catch (Exception e1) {
                System.err.print("LocalFileCleanerで不明なエラーが発生しました。");
                e1.printStackTrace();
                return Constants.EXIT_CODE_ERROR;
            }
        }
    }
    /**
     * クリーニング対象ディレクトリをクリーニングする。
     * @param creanDir クリーニング対象ディレクトリ
     * @param pattern クリーニングパターン
     * @param keepDate 保持日数
     * @param now 現在日時
     * @param recursive 再帰的にクリーニングを行うか
     * @throws CleanerSystemException
     * @return クリーニング結果
     */
    private boolean cleanDir(
            File creanDir,
            String pattern,
            int keepDate,
            Date now,
            boolean recursive) throws CleanerSystemException {
        if (creanDir == null || !creanDir.exists()) {
            // 指定されたディレクトリが存在しない
            Log.log(CLASS, MessageIdConst.LCLN_CLEN_DIR_ERROR, "指定されたディレクトリが存在しない", creanDir);
            return false;
        }
        if (!creanDir.isDirectory()) {
            // 指定されたパスがディレクトリでない
            Log.log(CLASS, MessageIdConst.LCLN_CLEN_DIR_ERROR, "指定されたパスがディレクトリでない", creanDir);
            return false;
        }

        // クリーニングを行う
        Log.log(CLASS, MessageIdConst.LCLN_FILE_DELETE, creanDir.getAbsolutePath());
        int cleanFileCount = 0;
        int cleanDirCount = 0;
        boolean result = true;
        File[] files = getListFiles(creanDir);
        for (int i = 0; i < files.length; i++) {
            // 最終更新日時を取得
            long lastModifiedTime = files[i].lastModified();

            if (files[i].isDirectory() && recursive) {
                // ディレクトリかつ、再帰的に処理を行う場合
                File[] childFiles = getListFiles(files[i]);
                if (childFiles.length == 0) {
                    // 子ファイルが存在しない場合、ディレクトリを削除
                    if (isExpired(lastModifiedTime, keepDate, now)) {
                        if (!files[i].delete()) {
                            Log.log(CLASS, MessageIdConst.LCLN_CLEN_FAIL, "ディレクトリ", files[i].getAbsolutePath());
                            result = false;
                        } else {
                            cleanDirCount++;
                            Log.log(CLASS, MessageIdConst.LCLN_DIR_DELETE, files[i].getAbsolutePath());
                        }
                    }
                } else {
                    // 子ファイルが存在する場合、再帰的にクリーニング処理を行う。
                    if (cleanDir(files[i], pattern, keepDate, now, recursive)) {
                        // 子ファイルが全て削除された場合はディレクトリを削除する
                        childFiles = getListFiles(files[i]);
                        if (childFiles.length == 0) {
                            if (isExpired(lastModifiedTime, keepDate, now)) {
                                if (!files[i].delete()) {
                                    Log.log(CLASS, MessageIdConst.LCLN_CLEN_FAIL, "ディレクトリ", files[i].getAbsolutePath());
                                    result = false;
                                } else {
                                    cleanDirCount++;
                                    Log.log(CLASS, MessageIdConst.LCLN_DIR_DELETE, files[i].getAbsolutePath());
                                }
                            }
                        }
                    } else {
                        Log.log(CLASS, MessageIdConst.LCLN_CLEN_FAIL, "ディレクトリ", files[i].getAbsolutePath());
                        result = false;
                    }
                }
            } else if (files[i].isFile()) {
                // ファイルの場合、保持期間を過ぎていて、パターンにマッチすれば削除する
                if (isExpired(lastModifiedTime, keepDate, now) && isMatchPattern(files[i], pattern)) {
                    if (!files[i].delete()) {
                        Log.log(CLASS, MessageIdConst.LCLN_CLEN_FAIL, "ファイル", files[i].getAbsolutePath());
                        result = false;
                    } else {
                        Log.log(CLASS, MessageIdConst.LCLN_DELETE_FILE, files[i].getAbsolutePath());
                        cleanFileCount++;
                    }
                }
            }
        }
        Log.log(
                CLASS,
                MessageIdConst.LCLN_FILE_DELETE_SUCCESS,
                creanDir.getAbsolutePath(),
                cleanDirCount,
                cleanFileCount);
        return result;
    }
    /**
     * 子ファイルを取得する。
     * @param dir 親ディレクトリ
     * @return 子ファイル
     */
    private File[] getListFiles(File dir) {
        File[] child = dir.listFiles();
        if (child == null) {
            child = new File[0];
        }
        return child;
    }
    /**
     * 削除対象のファイルがパターンにマッチするか判断する。
     * @param file 削除対象ファイル
     * @param pattern 削除対象ファイルパターン
     * @throws CleanerSystemException
     * @return 削除可否
     */
    private boolean isMatchPattern(File file, String pattern) throws CleanerSystemException {
        if (pattern == null || pattern.equals("")) {
            return true;
        } else {
            String strFile = file.getAbsolutePath();
            try {
                Matcher m = Pattern.compile(pattern).matcher(strFile);
                return m.matches();
            } catch (PatternSyntaxException e) {
                throw new CleanerSystemException(e, this.getClass(), MessageIdConst.LCLN_PATTERN_FAIL, pattern);
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
     * プロパティからクリーニング対象ディレクトリの保持日数を取得する。
     * @return クリーニング対象ディレクトリの保持日数
     */
    protected int getLocalFileKeepDate() {
        return Integer.parseInt(ConfigurationLoader.getProperty(Constants.PROP_KEY_LOCAL_FILE_KEEP_DATE));
    }
    /**
     * プロパティからクリーニング対象ディレクトリを取得する。
     * @return クリーニングの設定
     * @throws CleanerSystemException Cleanerのシステム例外
     */
    protected LocalFileCleanerBean[] getCleanLocalDirs() throws CleanerSystemException {
        List<String> cleanDirList = ConfigurationLoader.getPropStartWithString(
                Constants.PROP_KEY_LOCAL_FILE_CLEAN_DIR + ".");
        List<String> noEmptyDirList = ConfigurationLoader.getNoEmptyList(cleanDirList);

        int listSize = noEmptyDirList.size();
        List<LocalFileCleanerBean> list = new ArrayList<LocalFileCleanerBean>();
        for (int i = 0; i < listSize; i++) {
            // クリーニングディレクトリを設定
            LocalFileCleanerBean bean = new LocalFileCleanerBean();
            String dirKey = noEmptyDirList.get(i);
            String strDir = ConfigurationLoader.getProperty(dirKey);
            bean.setCleanDir(new File(strDir));

            // クリーニングパターンを設定
            String number = dirKey.substring(dirKey.lastIndexOf(".") + 1, dirKey.length());
            String pattarnKey = Constants.PROP_KEY_LOCAL_FILE_CLEAN_PATTERN + "." + number;
            String pattern = ConfigurationLoader.getProperty(pattarnKey);
            if (pattern == null || pattern.equals("")) {
                throw new CleanerSystemException(
                        this.getClass(), MessageIdConst.LCLN_PATTERN_NOT_FOUND, dirKey, strDir, pattarnKey);
            } else {
                bean.setPattern(pattern);
            }

            list.add(bean);
        }

        return list.toArray(new LocalFileCleanerBean[list.size()]);
    }
}
