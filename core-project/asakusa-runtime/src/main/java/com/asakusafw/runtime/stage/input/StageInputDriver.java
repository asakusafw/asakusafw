/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.input;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import com.asakusafw.runtime.stage.StageInput;

/**
 * ステージ入力を設定するためのドライバ。
 * @since 0.1.0
 * @version 0.2.5
 */
public final class StageInputDriver {

    static final Log LOG = LogFactory.getLog(StageInputDriver.class);

    private static final Charset ASCII = Charset.forName("ASCII");

    private static final long SERIAL_VERSION = 1;

    private static final String KEY = "com.asakusafw.stage.input";

    /**
     * Sets the input specification for this job.
     * @param job current job
     * @param inputList each input specification
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    public static void set(Job job, List<StageInput> inputList) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (inputList == null) {
            throw new IllegalArgumentException("inputList must not be null"); //$NON-NLS-1$
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Encoding inputs ({0} entries)",
                        inputList.size()));
            }
            String encoded = encode(inputList);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Encoded inputs ({0} bytes)",
                        encoded.length()));
            }
            job.getConfiguration().set(KEY, encoded);
        } catch (IOException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to store input information: {0}",
                    KEY), e);
        }
    }

    /**
     * ジョブに設定されたステージ入力の一覧を返す。
     * @param conf 設定情報
     * @return ジョブに設定されたステージ入力の一覧、未設定の場合は空のリスト
     * @throws IOException ステージ一覧の情報を復元できなかった場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    static List<StageInput> getInputs(Configuration conf) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        String encoded = conf.getRaw(KEY);
        if (encoded == null) {
            return Collections.emptyList();
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Decoding inputs ({0} bytes)",
                        encoded.length()));
            }
            List<StageInput> inputList = decode(conf, encoded);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Decoded inputs ({0} entries)",
                        inputList.size()));
            }
            return inputList;
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to extract input information: {0}",
                    KEY), e);
        } catch (ClassNotFoundException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to extract input information: {0}",
                    KEY), e);
        }
    }

    private static String encode(List<StageInput> inputList) throws IOException {
        assert inputList != null;
        String[] dictionary = buildDictionary(inputList);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(new GZIPOutputStream(new Base64OutputStream(sink)));
        WritableUtils.writeVLong(output, SERIAL_VERSION);
        WritableUtils.writeStringArray(output, dictionary);
        WritableUtils.writeVInt(output, inputList.size());
        for (StageInput input : inputList) {
            writeEncoded(output, dictionary, input.getPathString());
            writeEncoded(output, dictionary, input.getFormatClass().getName());
            writeEncoded(output, dictionary, input.getMapperClass().getName());
            WritableUtils.writeVInt(output, input.getAttributes().size());
            for (Map.Entry<String, String> attribute : input.getAttributes().entrySet()) {
                writeEncoded(output, dictionary, attribute.getKey());
                writeEncoded(output, dictionary, attribute.getValue());
            }
        }
        output.close();
        return new String(sink.toByteArray(), ASCII);
    }

    @SuppressWarnings("rawtypes")
    private static List<StageInput> decode(
            Configuration conf,
            String encoded) throws IOException, ClassNotFoundException {
        assert conf != null;
        assert encoded != null;
        ByteArrayInputStream source = new ByteArrayInputStream(encoded.getBytes(ASCII));
        DataInputStream input = new DataInputStream(new GZIPInputStream(new Base64InputStream(source)));
        long version = WritableUtils.readVLong(input);
        if (version != SERIAL_VERSION) {
            throw new IOException(MessageFormat.format(
                    "Invalid StageInput version: framework={0}, saw={1}",
                    SERIAL_VERSION,
                    version));
        }
        String[] dictionary = WritableUtils.readStringArray(input);
        int inputListSize = WritableUtils.readVInt(input);
        List<StageInput> results = new ArrayList<StageInput>();
        for (int inputListIndex = 0; inputListIndex < inputListSize; inputListIndex++) {
            String pathString = readEncoded(input, dictionary);
            String formatName = readEncoded(input, dictionary);
            String mapperName = readEncoded(input, dictionary);
            int attributeCount = WritableUtils.readVInt(input);
            Map<String, String> attributes = new HashMap<String, String>();
            for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
                String keyString = readEncoded(input, dictionary);
                String valueString = readEncoded(input, dictionary);
                attributes.put(keyString, valueString);
            }
            Class<? extends InputFormat> formatClass = conf.getClassByName(formatName).asSubclass(InputFormat.class);
            Class<? extends Mapper> mapperClass = conf.getClassByName(mapperName).asSubclass(Mapper.class);
            results.add(new StageInput(pathString, formatClass, mapperClass, attributes));
        }
        return results;
    }

    private static String[] buildDictionary(List<StageInput> inputList) {
        assert inputList != null;
        SortedSet<String> values = new TreeSet<String>();
        for (StageInput input : inputList) {
            values.add(input.getPathString());
            values.add(input.getFormatClass().getName());
            values.add(input.getMapperClass().getName());
            values.addAll(input.getAttributes().keySet());
            values.addAll(input.getAttributes().values());
        }
        return values.toArray(new String[values.size()]);
    }

    private static String readEncoded(DataInput input, String[] dictionary) throws IOException {
        assert input != null;
        assert dictionary != null;
        int index = WritableUtils.readVInt(input);
        if (index < 0 || index >= dictionary.length) {
            throw new IOException(MessageFormat.format(
                    "Invalid encoded value: index={0}, dict={1}",
                    index,
                    Arrays.toString(dictionary)));
        }
        return dictionary[index];
    }

    private static void writeEncoded(DataOutput output, String[] dictionary, String value) throws IOException {
        assert output != null;
        assert dictionary != null;
        assert value != null;
        int index = Arrays.binarySearch(dictionary, value);
        if (index < 0) {
            throw new IllegalStateException(MessageFormat.format(
                    "Value is not in dictionary: value={0}, dict={1}",
                    value,
                    Arrays.toString(dictionary)));
        }
        WritableUtils.writeVInt(output, index);
    }

    private StageInputDriver() {
        return;
    }
}
