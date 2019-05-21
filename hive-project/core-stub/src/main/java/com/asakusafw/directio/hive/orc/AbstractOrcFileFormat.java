/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.orc;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.directio.hive.serde.DataModelDescriptor;
import com.asakusafw.directio.hive.serde.PropertyDescriptor;
import com.asakusafw.info.hive.BuiltinStorageFormatInfo;
import com.asakusafw.info.hive.StorageFormatInfo;
import com.asakusafw.info.hive.TableInfo;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.directio.hadoop.StripedDataFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * An abstract implementation of {@link HadoopFileFormat} for ORCFile.
 * @param <T> the data model type
 * @since 0.7.0
 */
public abstract class AbstractOrcFileFormat<T> extends HadoopFileFormat<T>
        implements StripedDataFormat<T>, TableInfo.Provider {

    static final Log LOG = LogFactory.getLog(AbstractOrcFileFormat.class);

    static final Compatibility COMPAT = Compatibility.getInstance();

    /**
     * Returns the format configuration.
     * @return the format configuration
     */
    public abstract OrcFormatConfiguration getFormatConfiguration();

    /**
     * Returns the target data model descriptor.
     * @return the target data model descriptor
     */
    public abstract DataModelDescriptor getDataModelDescriptor();

    /**
     * Returns the table name.
     * @return the table name
     */
    public abstract String getTableName();

    @Override
    public TableInfo getSchema() {
        DataModelDescriptor desc = getDataModelDescriptor();
        TableInfo.Builder builder = new TableInfo.Builder(getTableName());
        for (PropertyDescriptor property : desc.getPropertyDescriptors()) {
            builder.withColumn(property.getSchema());
        }
        builder.withComment(desc.getDataModelComment());
        builder.withStorageFormat(BuiltinStorageFormatInfo.of(StorageFormatInfo.FormatKind.ORC));
        OrcFormatConfiguration conf = getFormatConfiguration();
        builder.withProperties(COMPAT.collectPropertyMap(conf));

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getSupportedType() {
        return (Class<T>) getDataModelDescriptor().getDataModelClass();
    }

    @Override
    public List<DirectInputFragment> computeInputFragments(
            InputContext context) throws IOException, InterruptedException {
        return COMPAT.computeInputFragments(this, context);
    }

    @Override
    public ModelInput<T> createInput(
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            long offset, long fragmentSize,
            Counter counter) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "ORCFile input ({0}): {1}", //$NON-NLS-1$
                    path,
                    getFormatConfiguration()));
        }
        return COMPAT.createInput(this, dataType, fileSystem, path, offset, fragmentSize, counter);
    }

    @Override
    public ModelOutput<T> createOutput(
            Class<? extends T> dataType,
            FileSystem fileSystem, Path path,
            Counter counter) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "ORCFile output ({0}): {1}", //$NON-NLS-1$
                    path,
                    getFormatConfiguration()));
        }
        return COMPAT.createOutput(this, dataType, fileSystem, path, counter);
    }
}
