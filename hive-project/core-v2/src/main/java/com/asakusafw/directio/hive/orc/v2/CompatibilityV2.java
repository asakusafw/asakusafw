/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.orc.v2;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.io.orc.OrcFile;
import org.apache.hadoop.hive.ql.io.orc.Reader;
import org.apache.orc.CompressionKind;
import org.apache.orc.OrcConf;
import org.apache.orc.StripeInformation;

import com.asakusafw.directio.hive.orc.AbstractOrcFileFormat;
import com.asakusafw.directio.hive.orc.Compatibility;
import com.asakusafw.directio.hive.orc.OrcFileInput;
import com.asakusafw.directio.hive.orc.OrcFileOutput;
import com.asakusafw.directio.hive.orc.OrcFormatConfiguration;
import com.asakusafw.directio.hive.serde.DataModelInspector;
import com.asakusafw.directio.hive.serde.DataModelMapping;
import com.asakusafw.directio.hive.util.CompatibilityUtil;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.BlockMap;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat.InputContext;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * compatibility layer for Direct I/O ORC File support.
 * @since 0.10.3
 */
public class CompatibilityV2 extends Compatibility {

    static final Log LOG = LogFactory.getLog(CompatibilityV2.class);

    @Override
    protected int getPriority() {
        OptionalInt version = CompatibilityUtil.getHiveMajorVersion();
        if (version.isPresent()) {
            int v = version.getAsInt();
            return v == 2 ? 2 : -1;
        }
        try {
            Class.forName("org.apache.orc.OrcConf"); //$NON-NLS-1$
            return 2;
        } catch (ClassNotFoundException | LinkageError e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error occurred while initializing compatibility class", e);  //$NON-NLS-1$
            }
            return -1;
        }
    }

    /**
     * returns extra table property map from the format configuration.
     * @param format the format configuration
     * @return the extra table property map
     */
    @Override
    protected Map<String, String> collectPropertyMap(OrcFormatConfiguration format) {
        Map<String, String> properties = new HashMap<>();
        putTableProperty(properties, OrcConf.COMPRESS, format.getCompressionKind());
        putTableProperty(properties, OrcConf.STRIPE_SIZE, format.getStripeSize());
        return properties;
    }

    private static void putTableProperty(Map<String, String> results, OrcConf property, Object value) {
        if (value == null) {
            return;
        }
        results.put(property.getAttribute(), value.toString());
    }

    @Override
    public List<DirectInputFragment> computeInputFragments(
            AbstractOrcFileFormat<?> format,
            InputContext context) throws IOException, InterruptedException {
        // TODO parallel?
        List<DirectInputFragment> results = new ArrayList<>();
        for (FileStatus status : context.getInputFiles()) {
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("Compatibility.infoLoadMetadata"), //$NON-NLS-1$
                        context.getDataType().getSimpleName(),
                        status.getPath()));
            }
            Reader orc = OrcFile.createReader(context.getFileSystem(), status.getPath());
            if (LOG.isInfoEnabled()) {
                LOG.info(MessageFormat.format(
                        Messages.getString("Compatibility.infoAnalyzeMetadata"), //$NON-NLS-1$
                        context.getDataType().getSimpleName(),
                        status.getPath(),
                        orc.getNumberOfRows(),
                        orc.getRawDataSize()));
            }
            BlockMap blockMap = BlockMap.create(
                    status.getPath().toString(),
                    status.getLen(),
                    BlockMap.computeBlocks(context.getFileSystem(), status),
                    false);
            for (StripeInformation stripe : orc.getStripes()) {
                long begin = stripe.getOffset();
                long end = begin + stripe.getLength();
                DirectInputFragment fragment = blockMap.get(begin, end);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Detect ORCFile stripe: path={0}, rows={1}, range={2}+{3}, allocation={4}", //$NON-NLS-1$
                            fragment.getPath(),
                            stripe.getNumberOfRows(),
                            fragment.getOffset(),
                            fragment.getSize(),
                            fragment.getOwnerNodeNames()));
                }
                results.add(fragment);
            }
        }
        return results;
    }

    @Override
    public <T> ModelInput<T> createInput(
            AbstractOrcFileFormat<T> format,
            Class<? extends T> dataType,
            FileSystem fileSystem,
            Path path,
            long offset,
            long fragmentSize,
            Counter counter) throws IOException, InterruptedException {
        DataModelMapping driverConf = new DataModelMapping();
        OrcFormatConfiguration conf = format.getFormatConfiguration();
        if (conf.getFieldMappingStrategy() != null) {
            driverConf.setFieldMappingStrategy(conf.getFieldMappingStrategy());
        }
        if (conf.getOnMissingSource() != null) {
            driverConf.setOnMissingSource(conf.getOnMissingSource());
        }
        if (conf.getOnMissingTarget() != null) {
            driverConf.setOnMissingTarget(conf.getOnMissingTarget());
        }
        if (conf.getOnIncompatibleType() != null) {
            driverConf.setOnIncompatibleType(conf.getOnIncompatibleType());
        }
        long size = fragmentSize;
        if (size < 0L) {
            FileStatus stat = fileSystem.getFileStatus(path);
            size = stat.getLen();
        }
        return new OrcFileInput<>(
                format.getDataModelDescriptor(), driverConf,
                fileSystem, path,
                offset, size, counter);
    }

    @Override
    public <T> ModelOutput<T> createOutput(
            AbstractOrcFileFormat<T> format,
            Class<? extends T> dataType,
            FileSystem fileSystem,
            Path path,
            Counter counter) throws IOException, InterruptedException {
        OrcFormatConfiguration conf = format.getFormatConfiguration();
        OrcFile.WriterOptions options = OrcFile.writerOptions(format.getConf());
        options.fileSystem(fileSystem);
        options.inspector(new DataModelInspector(format.getDataModelDescriptor()));
        org.apache.orc.OrcFile.Version formatVersion = conf.getFormatVersion(org.apache.orc.OrcFile.Version.class);
        if (formatVersion != null) {
            options.version(formatVersion);
        }
        CompressionKind compressionKind = conf.getCompressionKind(getCompressionKindClass());
        if (compressionKind != null) {
            options.compress(compressionKind);
        }
        Long stripeSize = conf.getStripeSize();
        if (stripeSize != null) {
            options.stripeSize(stripeSize);
        }
        return new OrcFileOutput<>(format.getDataModelDescriptor(), path, fileSystem, options, counter);
    }

    @Override
    public Optional<OrcFile.Version> findVersionId(String name) {
        try {
            return Optional.ofNullable(OrcFile.Version.byName(name));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    @Override
    public Class<CompressionKind> getCompressionKindClass() {
        return CompressionKind.class;
    }
}
