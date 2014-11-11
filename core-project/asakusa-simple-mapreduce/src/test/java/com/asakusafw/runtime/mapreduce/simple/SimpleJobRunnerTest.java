/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.mapreduce.simple;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.mapreduce.simple.SimpleJobRunner;

/**
 * Test for {@link SimpleJobRunner}.
 */
public class SimpleJobRunnerTest {

    /**
     * A temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test for map only job.
     * @throws Exception if failed
     */
    @Test
    public void map_only() throws Exception {
        Job job = newJob();
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setMapperClass(SimpleMapper.class);
        job.setNumReduceTasks(0);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);

        File inputDir = folder.newFolder();
        File inputFile = new File(inputDir, "input.txt");
        write(inputFile, "Hello, world!");

        File outputDir = folder.newFolder();
        outputDir.delete();

        FileInputFormat.setInputPaths(job, new Path(inputFile.toURI()));
        FileOutputFormat.setOutputPath(job, new Path(outputDir.toURI()));
        assertThat(new SimpleJobRunner().run(job), is(true));
        assertThat(trimHead(read(outputDir)), is(set("Hello, world!")));
    }

    /**
     * Test for map-reduce job.
     * @throws Exception if failed
     */
    @Test
    public void map_reduce() throws Exception {
        Job job = newJob();
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapperClass(WordCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setSortComparatorClass(Text.Comparator.class);
        job.setGroupingComparatorClass(Text.Comparator.class);

        job.setReducerClass(WordCountReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        File inputDir = folder.newFolder();
        File inputFile = new File(inputDir, "input.txt");
        write(inputFile, new String[] {
                "a b c d",
                "a a b c",
                "c",
        });

        File outputDir = folder.newFolder();
        outputDir.delete();

        FileInputFormat.setInputPaths(job, new Path(inputFile.toURI()));
        FileOutputFormat.setOutputPath(job, new Path(outputDir.toURI()));
        assertThat(new SimpleJobRunner().run(job), is(true));
        assertThat(toMap(read(outputDir)), is(map(new String[] {
                "a", "3",
                "b", "2",
                "c", "3",
                "d", "1",
        })));
    }

    /**
     * Simple stress testing.
     * @throws Exception if failed
     */
    @Test
    public void map_only_stress() throws Exception {
        int count = 50;
        map_only();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            map_only();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "{0} map_only: {1}ms ({2}ms/attempt)",
                count,
                t1 - t0,
                (t1 - t0) / count));
    }

    /**
     * Simple stress testing.
     * @throws Exception if failed
     */
    @Test
    public void map_reduce_stress() throws Exception {
        int count = 50;
        map_reduce();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            map_reduce();
        }
        long t1 = System.currentTimeMillis();
        System.out.println(MessageFormat.format(
                "{0} map_reduce: {1}ms ({2}ms/attempt)",
                count,
                t1 - t0,
                (t1 - t0) / count));
    }

    /**
     * Test for wrong job.
     * @throws Exception if failed
     */
    @Test
    public void exception() throws Exception {
        Job job = newJob();
        job.setJobName("w/ exception");
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setMapperClass(InvalidMapper.class);
        job.setNumReduceTasks(0);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);

        File inputDir = folder.newFolder();
        File inputFile = new File(inputDir, "input.txt");
        write(inputFile, "testing");

        File outputDir = folder.newFolder();
        outputDir.delete();

        FileInputFormat.setInputPaths(job, new Path(inputFile.toURI()));
        FileOutputFormat.setOutputPath(job, new Path(outputDir.toURI()));
        assertThat(new SimpleJobRunner().run(job), is(false));
    }

    private Job newJob() throws IOException {
        Job job = JobCompatibility.newJob(new Configuration());
        job.getConfiguration().setInt("io.sort.mb", 16);
        return job;
    }

    private Set<String> set(String... values) {
        return new LinkedHashSet<String>(Arrays.asList(values));
    }

    private Map<String, String> map(String... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0;
        Map<String, String> results = new LinkedHashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            results.put(keyValuePairs[i + 0], keyValuePairs[i + 1]);
        }
        return results;
    }

    private Set<String> trimHead(Set<String> values) {
        Set<String> results = new LinkedHashSet<String>();
        for (String string : values) {
            int index = string.indexOf('\t');
            if (index >= 0) {
                results.add(string.substring(index + 1));
            } else {
                results.add(string);
            }
        }
        return results;
    }

    private Map<String, String> toMap(Set<String> values) {
        Map<String, String> results = new LinkedHashMap<String, String>();
        for (String string : values) {
            int index = string.indexOf('\t');
            assertThat(string, index, greaterThanOrEqualTo(0));
            String key = string.substring(0, index);
            assertThat(results, not(hasKey(key)));
            results.put(key, string.substring(index + 1));
        }
        return results;
    }

    private void write(File file, String... lines) throws IOException {
        PrintWriter writer = new PrintWriter(file, "UTF-8");
        try {
            for (String line : lines) {
                writer.println(line);
            }
        } finally {
            writer.close();
        }
    }

    private Set<String> read(File file) throws IOException {
        return read(file, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (name.startsWith(".") || name.equals(FileOutputCommitter.SUCCEEDED_FILE_NAME)) {
                    return false;
                }
                return true;
            }
        });
    }

    private Set<String> read(File file, FileFilter filter) throws IOException {
        if (filter.accept(file) == false) {
            return Collections.emptySet();
        }
        Set<String> results = new LinkedHashSet<String>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                results.addAll(read(f, filter));
            }
        } else {
            Scanner scanner = new Scanner(file, "UTF-8");
            try {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty() == false) {
                        results.add(line);
                    }
                }
            } finally {
                scanner.close();
            }
        }
        return results;
    }

    /**
     * through.
     */
    public static final class SimpleMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            context.write(key, value);
        }
    }

    /**
     * tokenize.
     */
    public static final class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
        final LongWritable one = new LongWritable(1);
        final Text out = new Text();
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            for (String token : value.toString().split("\\s+")) {
                if (token.isEmpty()) {
                    continue;
                }
                out.set(token);
                context.write(out, one);
            }
        }
    }

    /**
     * aggregate.
     */
    public static final class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        final LongWritable count = new LongWritable(1);
        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long total = 0;
            for (LongWritable value : values) {
                total += value.get();
            }
            count.set(total);
            context.write(key, count);
        }
    }

    /**
     * raise I/O error.
     */
    public static final class InvalidMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            throw new IOException();
        }
    }
}
