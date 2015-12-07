/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.directio.tools;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.ResourceInfo;
import com.asakusafw.runtime.directio.ResourcePattern;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;

/**
 * CLI for {@link DirectDataSource#list(String, ResourcePattern, Counter)}.
 * @since 0.4.0
 */
public final class DirectIoList extends Configured implements Tool {

    static final Log LOG = LogFactory.getLog(DirectIoList.class);

    private DirectDataSourceRepository repository;

    /**
     * Creates a new instance.
     */
    public DirectIoList() {
        return;
    }

    /**
     * Creates a new instance for testing.
     * @param repository repository
     */
    DirectIoList(DirectDataSourceRepository repository) {
        this.repository = repository;
    }

    @Override
    public int run(String[] args) throws Exception {
        LinkedList<String> argList = new LinkedList<>();
        Collections.addAll(argList, args);
        while (argList.isEmpty() == false) {
            String arg = argList.removeFirst();
            if (arg.equals("--")) { //$NON-NLS-1$
                break;
            } else {
                argList.addFirst(arg);
                break;
            }
        }
        if (argList.size() < 2) {
            LOG.error(MessageFormat.format(
                    "Invalid arguments: {0}",
                    Arrays.toString(args)));
            System.err.println(MessageFormat.format(
                    "Usage: hadoop {0} -conf <datasource-conf.xml> base-path resource-pattern [resource-pattern [...]]",
                    getClass().getName()));
            return 1;
        }
        String path = argList.removeFirst();
        List<FilePattern> patterns = new ArrayList<>();
        for (String arg : argList) {
            patterns.add(FilePattern.compile(arg));
        }
        if (repository == null) {
            repository = HadoopDataSourceUtil.loadRepository(getConf());
        }
        String basePath = repository.getComponentPath(path);
        DirectDataSource source = repository.getRelatedDataSource(path);
        for (FilePattern pattern : patterns) {
            List<ResourceInfo> list = source.list(basePath, pattern, new Counter());
            for (ResourceInfo info : list) {
                System.out.println(info.getPath());
            }
        }
        return 0;
    }

    /**
     * Tool program entry.
     * @param args {@code Hadoop-generic-arguments...} {@code application specific-arguments}
     * @throws Exception if failed to execute
     * @see #run(String[])
     */
    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new Configuration(), new DirectIoList(), args);
        System.exit(exitCode);
    }
}
