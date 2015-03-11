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
package com.asakusafw.testdriver.inprocess;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

/**
 * Mock implementation of a hadoop job.
 */
public class MockHadoopJob extends Configured implements Tool {

    private static Callback callback;

    @Override
    public int run(String[] args) throws Exception {
        Callback tool = take();
        return tool.run(args, getConf());
    }

    private synchronized Callback take() {
        Callback result = callback;
        callback = null;
        if (result == null) {
            throw new IllegalStateException();
        }
        return result;
    }

    static void callback(Callback tool) {
        callback = tool;
    }

    interface Callback {
        int run(String[] args, Configuration conf) throws Exception;
    }
}
