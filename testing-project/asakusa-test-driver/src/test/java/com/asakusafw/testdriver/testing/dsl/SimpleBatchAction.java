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
package com.asakusafw.testdriver.testing.dsl;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.value.StringOptionUtil;
import com.asakusafw.runtime.workaround.snappyjava.MacSnappyJavaWorkaround;
import com.asakusafw.testdriver.testing.model.Simple;

/**
 * Processes input files, and puts results onto output.
 */
public class SimpleBatchAction extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        MacSnappyJavaWorkaround.install();
        FileSystem fs = FileSystem.get(getConf());
        fs.mkdirs(new Path(SimpleExporter.DIRECTORY));
        Path inputDir = new Path(SimpleImporter.DIRECTORY);
        int index = 0;
        for (FileStatus input : fs.listStatus(inputDir)) {
            Path output = new Path(SimpleExporter.OUTPUT_PREFIX + index++);
            process(input.getPath(), output);
        }
        extra();
        return 0;
    }

    private void extra() {
        VariableTable args = new VariableTable();
        args.defineVariables(getConf().get(StageConstants.PROP_ASAKUSA_BATCH_ARGS, ""));
        switch (args.getVariables().getOrDefault("action", "")) {
        case "":
            break;
        case "dependency":
            try {
                Class.forName("com.asakusafw.example.Dependency");
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
            break;
        case "invalid":
            throw new UnsupportedOperationException("invalid");
        default:
            throw new AssertionError(args.getVariables());
        }
    }

    private void process(Path input, Path output) throws IOException {
        Simple buf = new Simple();
        try (ModelInput<Simple> in = TemporaryStorage.openInput(getConf(), Simple.class, input);
                ModelOutput<Simple> out = TemporaryStorage.openOutput(getConf(), Simple.class, output)) {
            while (in.readTo(buf)) {
                StringOptionUtil.append(buf.getValueOption(), "?");
                out.write(buf);
            }
        }
    }
}
