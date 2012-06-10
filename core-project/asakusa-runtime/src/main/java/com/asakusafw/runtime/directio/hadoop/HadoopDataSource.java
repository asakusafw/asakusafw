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
package com.asakusafw.runtime.directio.hadoop;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import com.asakusafw.runtime.directio.AbstractDirectDataSource;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceProfile;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.ResourceInfo;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.ResourcePattern;
import com.asakusafw.runtime.directio.keepalive.KeepAliveDataSource;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * An implementation of {@link AbstractDirectDataSource} using {@link FileSystem}.
 * @since 0.2.5
 * @version 0.2.6
 */
public class HadoopDataSource extends AbstractDirectDataSource implements Configurable {

    static final Log LOG = LogFactory.getLog(HadoopDataSource.class);

    private volatile DirectDataSource core;

    private volatile Configuration conf;

    @Override
    public Configuration getConf() {
        return conf;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public void configure(DirectDataSourceProfile profile) throws IOException, InterruptedException {
        if (conf == null) {
            throw new IllegalStateException();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start configuring Hadoop data source (id={0}, path={1})",
                    profile.getId(),
                    profile.getPath()));
        }
        HadoopDataSourceProfile hProfile = HadoopDataSourceProfile.convert(profile, conf);
        this.core = new HadoopDataSourceCore(hProfile);
        if (hProfile.getKeepAliveInterval() > 0) {
            this.core = new KeepAliveDataSource(core, hProfile.getKeepAliveInterval());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish configuring Hadoop data source: {0}",
                    hProfile));
        }
    }

    @Override
    public <T> List<DirectInputFragment> findInputFragments(
            Class<? extends T> dataType,
            DataFormat<T> format,
            String basePath,
            ResourcePattern resourcePattern) throws IOException, InterruptedException {
        return core.findInputFragments(dataType, format, basePath, resourcePattern);
    }

    @Override
    public <T> ModelInput<T> openInput(
            Class<? extends T> dataType,
            DataFormat<T> format,
            DirectInputFragment fragment,
            Counter counter) throws IOException, InterruptedException {
        return core.openInput(dataType, format, fragment, counter);
    }

    @Override
    public <T> ModelOutput<T> openOutput(
            OutputAttemptContext context,
            Class<? extends T> dataType,
            DataFormat<T> format,
            String basePath,
            String resourcePath,
            Counter counter) throws IOException, InterruptedException {
        return core.openOutput(context, dataType, format, basePath, resourcePath, counter);
    }

    @Override
    public List<ResourceInfo> list(
            String basePath,
            ResourcePattern resourcePattern,
            Counter counter) throws IOException, InterruptedException {
        return core.list(basePath, resourcePattern, counter);
    }

    @Override
    public boolean delete(
            String basePath,
            ResourcePattern resourcePattern,
            boolean recursive,
            Counter counter) throws IOException, InterruptedException {
        return core.delete(basePath, resourcePattern, recursive, counter);
    }

    @Override
    public void setupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        core.setupAttemptOutput(context);
    }

    @Override
    public void commitAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        core.commitAttemptOutput(context);
    }

    @Override
    public void cleanupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        core.cleanupAttemptOutput(context);
    }

    @Override
    public void setupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        core.setupTransactionOutput(context);
    }

    @Override
    public void commitTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        core.commitTransactionOutput(context);
    }

    @Override
    public void cleanupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        core.cleanupTransactionOutput(context);
    }
}
