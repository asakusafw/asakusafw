/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.integration.core.yaess;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

public class Main extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 0) {
            throw new IllegalStateException();
        }
        Files.write(
                Paths.get(System.getenv("PROJECT_HOME"), "hadoop.txt"),
                Arrays.asList("hadoop"));
        return 0;
    }
}