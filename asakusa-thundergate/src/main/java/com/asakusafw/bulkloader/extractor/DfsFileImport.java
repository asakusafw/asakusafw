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
package com.asakusafw.bulkloader.extractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.common.MultiThreadedCopier;
import com.asakusafw.bulkloader.common.SequenceFileModelOutput;
import com.asakusafw.bulkloader.common.UrlStreamHandlerFactoryRegisterer;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.TsvIoFactory;
import com.asakusafw.runtime.io.ZipEntryInputStream;

/**
 * 標準入力を読み込んでSequenceFile形式でDFSにファイルを書き出すクラス。
 * @author yuta.shirai
 */
public class DfsFileImport {

    private static final int INPUT_BUFFER_BYTES = 128 * 1024;

    private static final int COPY_BUFFER_RECORDS = 1000;

    static {
        UrlStreamHandlerFactoryRegisterer.register();
    }

    /**
     * 標準入力を読み込んでDFSにファイルを書き出す。
     * ZIPで受け取ったTSV形式のファイルをModelオブジェクトに変換して、
     * SequenceFile形式でDFSに出力する
     * @param bean パラメータを保持するBean
     * @param user OSのユーザー名
     * @return 出力結果（true：正常終了、false：異常終了）
     */
    public boolean importFile(ImportBean bean, String user) {
        // 標準入力を取得
        ZipInputStream zipIs = new ZipInputStream(getInputStream());
        try {
            ZipEntry zipEntry;
            // ZIPファイルの終端まで繰り返す
            while ((zipEntry = zipIs.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    // エントリがディレクトリの場合はスキップする
                    continue;
                }
                String tableName = FileNameUtil.getImportTableName(
                        zipEntry.getName().replace(File.separatorChar, '/'));

                ImportTargetTableBean targetTableBean = bean.getTargetTable(tableName);
                if (targetTableBean == null) {
                    // ZIPエントリに対応するテーブルの定義がDSL存在しない場合異常終了する。
                    throw new BulkLoaderSystemException(
                            this.getClass(),
                            MessageIdConst.EXT_CREATE_HDFSFILE_EXCEPTION,
                            // TODO MessageFormat.formatを検討
                            "ZIPエントリに対応するテーブルの定義がDSL存在しない。テーブル名：" + tableName);
                }

                URI dfsFilePath;
                if (Boolean.valueOf(ConfigurationLoader.getProperty(Constants.PROP_KEY_WORKINGDIR_USE))) {
                    dfsFilePath = FileNameUtil.createDfsImportURIWithWorkingDir(
                            targetTableBean.getDfsFilePath(), bean.getExecutionId());
                } else {
                    dfsFilePath = FileNameUtil.createDfsImportURI(
                            targetTableBean.getDfsFilePath(), bean.getExecutionId(), user);
                }

                Class<?> targetTableModel = targetTableBean.getImportTargetType();

                Log.log(
                        this.getClass(),
                        MessageIdConst.EXT_CREATE_HDFSFILE,
                        tableName, dfsFilePath.toString(), targetTableModel.toString());

                // ファイルをSequenceFileに変換してDFSに書き出す
                write(targetTableModel, dfsFilePath, new ZipEntryInputStream(zipIs));

                Log.log(
                        this.getClass(),
                        MessageIdConst.EXT_CREATE_HDFSFILE_SUCCESS,
                        tableName, dfsFilePath.toString(), targetTableModel.toString());
            }
            // 正常終了
            return true;

        } catch (BulkLoaderSystemException e) {
            Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
            return false;
        } catch (IOException e) {
            // ZIPエントリの取得に失敗
            Log.log(
                    e,
                    this.getClass(),
                    MessageIdConst.EXT_CREATE_HDFSFILE_EXCEPTION,
                    "標準入力からZIPエントリの取得に失敗");
            return false;
        } finally {
            try {
                zipIs.close();
            } catch (IOException e) {
                // ここで例外が発生した場合は握りつぶす
                e.printStackTrace();
            }
        }
    }
    /**
     * ZipInputStreamからファイルを読み込んでSequenceFile型でDFSに書き出す。
     * 「TSV→Model→SequenceFile」の変換を行う
     * @param <T> Import対象テーブルに対応するModelのクラス型
     * @param targetTableModel Import対象テーブルに対応するModelのクラス
     * @param dfsFilePath HFSF上のファイル名
     * @param zipEntryInputStream ZipOutputStream
     * @throws BulkLoaderSystemException 読み出しと出力に失敗した場合
     */
    protected <T> void write(
            Class<T> targetTableModel,
            URI dfsFilePath,
            ZipEntryInputStream zipEntryInputStream) throws BulkLoaderSystemException {
        ModelInput<T> modelIn = null;
        FileSystem fs = null;
        SequenceFile.Writer writer = null;
        try {
            // TSVファイルをBeanに変換するオブジェクトを生成する
            TsvIoFactory<T> factory = new TsvIoFactory<T>(targetTableModel);
            modelIn = factory.createModelInput(zipEntryInputStream);

            // SequenceFileをDFSに出力するオブジェクトを生成する
            Configuration conf = new Configuration();
            fs = FileSystem.get(dfsFilePath, conf);

            // コピー用のバッファを作成する
            Collection<T> working = new ArrayList<T>(COPY_BUFFER_RECORDS);
            for (int i = 0; i < COPY_BUFFER_RECORDS; i++) {
                working.add(factory.createModelObject());
            }

            // ZIP圧縮に関する情報を取得
            String strCompType = ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_SEQ_FILE_COMP_TYPE);
            SequenceFile.CompressionType compType = getCompType(strCompType);

            // Writerを経由して別スレッドで書き出す
            writer = SequenceFile.createWriter(
                    fs,
                    conf,
                    new Path(dfsFilePath.getPath()),
                    NullWritable.class,
                    targetTableModel,
                    compType);
            MultiThreadedCopier.copy(modelIn, new SequenceFileModelOutput<T>(writer), working);
        } catch (IOException e) {
            throw new BulkLoaderSystemException(
                    e,
                    this.getClass(),
                    MessageIdConst.EXT_CREATE_HDFSFILE_EXCEPTION,
                    "DFSにファイルを書き出す処理に失敗。URI：" + dfsFilePath);
        } catch (InterruptedException e) {
            throw new BulkLoaderSystemException(
                    e,
                    this.getClass(),
                    MessageIdConst.EXT_CREATE_HDFSFILE_EXCEPTION,
                    "DFSにファイルを書き出す処理に失敗。URI：" + dfsFilePath);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * SequenceFileのCompressionTypeを取得する。
     * @param strCompType CompressionTypeの文字列
     * @return CompressionType
     */
    protected CompressionType getCompType(String strCompType) {
        CompressionType compType = null;
        try {
            compType = SequenceFile.CompressionType.valueOf(strCompType);
        } catch (Exception e) {
            compType = SequenceFile.CompressionType.NONE;
            Log.log(this.getClass(), MessageIdConst.EXT_SEQ_COMP_TYPE_FAIL, strCompType);
        }
        return compType;
    }
    /**
     * InputStreamを生成して返す。
     * @return InputStream
     */
    protected InputStream getInputStream() {
        return new BufferedInputStream(System.in, INPUT_BUFFER_BYTES);
    }
}
