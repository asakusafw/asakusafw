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
package com.asakusafw.bulkloader.collector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;

import com.asakusafw.bulkloader.bean.ExportTargetTableBean;
import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileCompType;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.TsvIoFactory;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;

/**
 * Export対象ファイルをDBサーバへ送信するクラス。
 * @author yuta.shirai
 */
public class ExportFileSend {

    static final Log LOG = new Log(ExportFileSend.class);

    /**
     * ファイル名作成の為のマップ。
     */
    Map<String, Integer> fileNameMap = new HashMap<String, Integer>();
    /**
     * Export対象ファイルをDBサーバへ送信する。
     * <p>
     * Export対象ファイルを読み込んでTSV形式に変換した上で、DBサーバの標準入力へ送信する。
     * 送信は、Exporterの標準入力にファイルを書き込む事で行う。
     * </p>
     * @param bean パラメータを保持するBean
     * @param user OSのユーザー名
     * @return Exportファイル送信結果（true:成功、false:失敗）
     */
    public boolean sendExportFile(ExporterBean bean, String user) {

        // 圧縮に関する情報を取得
        String strCompType = ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_FILE_COMP_TYPE);
        FileCompType compType = FileCompType.find(strCompType);

        OutputStream output = getOutputStream();
        try {
            FileList.Writer writer;
            try {
                writer = FileList.createWriter(output, compType == FileCompType.DEFLATED);
            } catch (IOException e) {
                throw new BulkLoaderSystemException(e, getClass(), "TG-COLLECTOR-02001",
                        "Exporterと接続するチャネルを開けませんでした");
            }
            Configuration conf = new Configuration();
            List<String> l = bean.getExportTargetTableList();
            for (String tableName : l) {
                ExportTargetTableBean targetTable = bean.getExportTargetTable(tableName);
                Class<? extends Writable> targetTableModel =
                    targetTable.getExportTargetType().asSubclass(Writable.class);

                List<Path> filePath = FileNameUtil.createPaths(
                        conf,
                        targetTable.getDfsFilePaths(),
                        bean.getExecutionId(),
                        user);

                // Export対象テーブルに対するディレクトリ数分繰り返す
                int fileCount = filePath.size();
                long recordCount = 0;
                for (int i = 0; i < fileCount; i++) {
                    // Exportファイルを送信
                    LOG.info("TG-COLLECTOR-02002",
                            tableName, filePath.get(i), compType.getSymbol(), targetTableModel.toString());
                    long countInFile = send(targetTableModel, filePath.get(i).toString(), writer, tableName);
                    if (countInFile >= 0) {
                        recordCount += countInFile;
                    }
                    LOG.info("TG-COLLECTOR-02003",
                            tableName, filePath.get(i), compType.getSymbol(), targetTableModel.toString());
                }

                LOG.info("TG-PROFILE-01004",
                        bean.getTargetName(),
                        bean.getBatchId(),
                        bean.getJobflowId(),
                        bean.getExecutionId(),
                        tableName,
                        recordCount);
            }

            try {
                writer.close();
            } catch (IOException e) {
                // ここで例外が発生した場合は握りつぶす
                e.printStackTrace();
            }

            // 正常終了
            return true;
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
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
     * 指定された一時ファイルを読み込んでTSV形式で
     * {@link com.asakusafw.bulkloader.transfer.FileList.Writer}に書き出す。
     * @param <T> データモデルの型
     * @param targetTableModel Exportデータに対応するModelのクラス型
     * @param filePath Exportファイル
     * @param writer 出力先のWriter
     * @param tableName テーブル名
     * @return 書きだしたレコード数、エントリをひとつも書き出さなかった場合は -1
     * @throws BulkLoaderSystemException 入出力に関するシステム例外が発生した場合
     */
    protected <T extends Writable> long send(
            Class<T> targetTableModel,
            String filePath,
            FileList.Writer writer,
            String tableName) throws BulkLoaderSystemException {
        FileSystem fs = null;
        String fileName = null;

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
                LOG.info("TG-COLLECTOR-02006",
                        tableName, filePath);
                return -1;
            } else {
                LOG.info("TG-COLLECTOR-02007",
                        listedPaths.length, tableName, filePath);
            }
            long count = 0;
            boolean addEntry = false;
            for (Path path : listedPaths) {
                // ファイルがシステムファイルの場合はスキップする
                if (isSystemFile(path)) {
                    continue;
                }

                // TODO 見通しを良くする
                // テンポラリ領域から結果を読み込むオブジェクトを生成する
                ModelInput<T> input = TemporaryStorage.openInput(conf, targetTableModel, path);
                try {
                    while (true) {
                        // エントリを追加
                        addEntry = true;
                        fileName = FileNameUtil.createSendExportFileName(tableName, fileNameMap);
                        OutputStream output = writer.openNext(FileList.content(fileName));
                        try {
                            CountingOutputStream counter = new CountingOutputStream(output);
                            ModelOutput<T> modelOut = factory.createModelOutput(counter);
                            T model = factory.createModelObject();
                            LOG.info("TG-COLLECTOR-02004",
                                    tableName, path.toString(), fileName);

                            // 入力を読み込み、Model→TSV変換を行う
                            boolean nextFile = false;
                            while (input.readTo(model)) {
                                // Modolを書き出す
                                modelOut.write(model);
                                count++;
                                // 最大ファイルサイズに達したかチェックする
                                // charからbyteに変換する部分でバッファされるため、
                                // 必ずしも分割サイズで分割されない。(バッファ分の誤差がある)
                                if (counter.getByteCount() > maxSize) {
                                    nextFile = true;
                                    break;
                                }
                            }
                            modelOut.close();
                            LOG.info("TG-COLLECTOR-02005",
                                    tableName, path.toString(), fileName);

                            if (nextFile) {
                                // 最大ファイルサイズに達した場合は繰り返す
                                continue;
                            } else {
                                // 入力ファイルの終点に達した場合はループを抜ける
                                break;
                            }
                        } finally {
                            output.close();
                        }
                    }
                } finally {
                    input.close();
                }
            }
            if (addEntry) {
                return count;
            } else {
                assert count == 0;
                return -1;
            }
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-COLLECTOR-02001",
                    MessageFormat.format(
                            "HDFSのディレクトリ：{0} 送信ファイル名：{1}",
                            filePath,
                            fileName));
        } catch (URISyntaxException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-COLLECTOR-02001",
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
                            "TG-COLLECTOR-02001",
                            MessageFormat.format(
                                    "HDFSのファイルシステムのクローズに失敗。URI：{0}",
                                    filePath));
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
