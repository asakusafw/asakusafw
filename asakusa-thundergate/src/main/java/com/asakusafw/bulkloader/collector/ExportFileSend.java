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
package com.asakusafw.bulkloader.collector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileCompType;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.common.UrlStreamHandlerFactoryRegisterer;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.TsvIoFactory;

/**
 * Export対象ファイルをDBサーバへ送信するクラス。
 * @author yuta.shirai
 */
public class ExportFileSend {

    static {
        UrlStreamHandlerFactoryRegisterer.register();
    }

    /**
     * ファイル名作成の為のマップ。
     */
    Map<String, Integer> fileNameMap = new HashMap<String, Integer>();
    /**
     * Export対象ファイルをDBサーバへ送信する。
     * <p>
     * SequenceFile形式のExport対象ファイルを読み込んでTSV形式に変換した上で、
     * ZIP形式に圧縮してDBサーバの標準入力へ送信する。
     * 送信は、Exporterの標準入力にファイルを書き込む事で行う。
     * </p>
     * @param bean パラメータを保持するBean
     * @param user OSのユーザー名
     * @return Exportファイル送信結果（true:成功、false:失敗）
     */
    public boolean sendExportFile(ExporterBean bean, String user) {

        // ZIP圧縮に関する情報を取得
        String strCompType = ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_FILE_COMP_TYPE);
        FileCompType compType = FileCompType.find(strCompType);

        OutputStream output = getOutputStream();
        try {
            // 標準出力ストリームをZipOutputStreamでラップする
            ZipOutputStream zos = new ZipOutputStream(output);
            if (FileCompType.STORED.equals(compType)) {
                zos.setLevel(0);
            }

            List<String> l = bean.getExportTargetTableList();
            boolean isPutEntry = false;
            for (String tableName : l) {
                ExportTargetTableBean targetTable = bean.getExportTargetTable(tableName);
                Class<? extends Writable> targetTableModel =
                    targetTable.getExportTargetType().asSubclass(Writable.class);

                List<String> filePath;
                if (Boolean.valueOf(ConfigurationLoader.getProperty(Constants.PROP_KEY_WORKINGDIR_USE))) {
                    filePath = FileNameUtil.createDfsExportURIWithWorkingDir(
                            targetTable.getDfsFilePaths(), bean.getExecutionId());
                } else {
                    filePath = FileNameUtil.createDfsExportURI(
                            targetTable.getDfsFilePaths(), user, bean.getExecutionId());
                }

                // Export対象テーブルに対するディレクトリ数分繰り返す
                int fileCount = filePath.size();
                long recordCount = 0;
                for (int i = 0; i < fileCount; i++) {
                    // Exportファイルを送信
                    Log.log(
                            this.getClass(),
                            MessageIdConst.COL_SEND_HDFSFILE,
                            tableName, filePath.get(i), compType.getCompType(), targetTableModel.toString());
                    long countInFile = send(targetTableModel, filePath.get(i), zos, tableName);
                    isPutEntry |=  countInFile > 0;
                    recordCount += countInFile;
                    Log.log(
                            this.getClass(),
                            MessageIdConst.COL_SEND_HDFSFILE_SUCCESS,
                            tableName, filePath.get(i), compType.getCompType(), targetTableModel.toString());
                }

                Log.log(
                        this.getClass(),
                        MessageIdConst.PRF_COLLECT_COUNT,
                        bean.getTargetName(),
                        bean.getBatchId(),
                        bean.getJobflowId(),
                        bean.getExecutionId(),
                        tableName,
                        recordCount);
            }

            if (!isPutEntry) {
                // ZIPにエントリが存在しない場合はダミーのエントリを追加する
                ZipEntry ze = new ZipEntry(Constants.EXPORT_FILE_NOT_FOUND);
                try {
                    zos.putNextEntry(ze);
                } catch (IOException e) {
                    throw new BulkLoaderSystemException(
                            e,
                            this.getClass(),
                            MessageIdConst.COL_SENDFILE_EXCEPTION,
                            "ZIPファイルへのエントリの追加に失敗");
                }
            }

            try {
                zos.close();
            } catch (IOException e) {
                // ここで例外が発生した場合は握りつぶす
                e.printStackTrace();
            }

            // 正常終了
            return true;
        } catch (BulkLoaderSystemException e) {
            Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
            return false;
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                // ここで例外が発生した場合は握りつぶす
                e.printStackTrace();
            }
        }
    }
    /**
     * 指定されたSequenceFileを読み込んでTSV形式でZipOutputStreamに書き出す。
     * @param <T> データモデルの型
     * @param targetTableModel Exportデータに対応するModelのクラス型
     * @param filePath Exportファイル
     * @param zos 出力先の{@link ZipOutputStream}
     * @param tableName テーブル名
     * @return ZipOutputStreamに追加したエントリの数
     * @throws BulkLoaderSystemException 入出力に関するシステム例外が発生した場合
     */
    protected <T extends Writable> long send(
            Class<T> targetTableModel,
            String filePath,
            ZipOutputStream zos,
            String tableName) throws BulkLoaderSystemException {
        FileSystem fs = null;
        String fileName = null;
        long count = 0;

        // 最大ファイルサイズを取得する
        long maxSize = Long.parseLong(ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_LOAD_MAX_SIZE));

        try {
            TsvIoFactory<T> factory = new TsvIoFactory<T>(targetTableModel);
            Configuration conf = new Configuration();
            fs = FileSystem.get(new URI(filePath), conf);

            // ディレクトリ内のファイルを取得し、ファイルを出力する
            FileStatus[] status = fs.globStatus(new Path(filePath));
            Path[] listedPaths = FileUtil.stat2Paths(status);
            if (listedPaths == null) {
                Log.log(
                        this.getClass(),
                        MessageIdConst.COL_EXPORT_FILE_NOT_FOUND,
                        tableName, filePath);
                return count;
            } else {
                Log.log(
                        this.getClass(),
                        MessageIdConst.COL_EXPORT_FILE_FOUND,
                        listedPaths.length, tableName, filePath);
            }
            for (Path path : listedPaths) {
                // ファイルがシステムファイルの場合はスキップする
                if (isSystemFile(path)) {
                    continue;
                }

                // TODO 見通しを良くする
                SequenceFile.Reader reader = null;
                try {
                    // SequenceFileをHDFSから読み込むオブジェクトを生成する
                    reader = new SequenceFile.Reader(fs, path, conf);

                    while (true) {
                        // ZIPエントリを追加
                        fileName = FileNameUtil.createSendExportFileName(tableName, fileNameMap);
                        ZipEntry ze = new ZipEntry(fileName);
                        zos.putNextEntry(ze);
                        count++;

                        ModelOutput<T> modelOut = null;
                        try {
                            // BeanをTSVファイルに変換するオブジェクトを生成する
                            ByteCountZipEntryOutputStream zeos = new ByteCountZipEntryOutputStream(zos);
                            T model = factory.createModelObject();
                            modelOut = factory.createModelOutput(zeos);

                            Log.log(
                                    this.getClass(),
                                    MessageIdConst.COL_SENDFILE,
                                    tableName, path.toString(), fileName);

                            // SequenceFileを読み込み、Model→TSV変換を行う
                            boolean nextFile = false;
                            while (reader.next(NullWritable.get(), model)) {
                                // Modolを書き出す
                                modelOut.write(model);
                                // 最大ファイルサイズに達したかチェックする
                                // charからbyteに変換する部分でバッファされるため、
                                // 必ずしも分割サイズで分割されない。(バッファ分の誤差がある)
                                if (zeos.getSize() > maxSize) {
                                    nextFile = true;
                                    break;
                                }
                            }

                            Log.log(this.getClass(),
                                    MessageIdConst.COL_SENDFILE_SUCCESS,
                                    tableName, path.toString(), fileName);

                            if (nextFile) {
                                // 最大ファイルサイズに達した場合は繰り返す
                                continue;
                            } else {
                                // 入力ファイルの終点に達した場合はループを抜ける
                                break;
                            }
                        } finally {
                            if (modelOut != null) {
                                modelOut.close();
                            }
                        }
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
            return count;
        } catch (IOException e) {
            throw new BulkLoaderSystemException(
                    e,
                    this.getClass(),
                    MessageIdConst.COL_SENDFILE_EXCEPTION,
                    MessageFormat.format(
                            "HDFSのディレクトリ：{0} 送信ファイル名：{1}",
                            filePath,
                            fileName));
        } catch (URISyntaxException e) {
            throw new BulkLoaderSystemException(
                    e,
                    this.getClass(),
                    MessageIdConst.COL_SENDFILE_EXCEPTION,
                    MessageFormat.format(
                            "HDFSのパスが不正。HDFSのディレクトリ：{0}",
                            filePath));
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    throw new BulkLoaderSystemException(
                            e,
                            this.getClass(),
                            MessageIdConst.COL_SENDFILE_EXCEPTION,
                            // TODO MessageFormat.formatの検討
                            "HDFSのファイルシステムのクローズに失敗。URI：" + filePath);
                }
            }
        }
    }
    /**
     * ファイルがHadoopのシステムファイルである場合のみ{@code true}を返す。
     * @param path ファイルのパス
     * @return 指定のパスがシステムファイルの位置を表す場合に{@code true}、そうでなければ{@code false}
     */
    private boolean isSystemFile(Path path) {
        assert path != null;
        String name = path.getName();
        return name.equals(FileOutputCommitter.SUCCEEDED_FILE_NAME)
            || name.equals("_logs");
    }

    /**
     * OutputStreamを生成して返す。
     * @return OutputStream
     */
    protected OutputStream getOutputStream() {
        return SystemOutManager.getOut();
    }
}
