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
package com.asakusafw.workflow.executor.basic;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.workflow.executor.TaskExecutionContext;
import com.asakusafw.workflow.executor.TaskExecutor;
import com.asakusafw.workflow.executor.TaskExecutors;
import com.asakusafw.workflow.model.DeleteTaskInfo;
import com.asakusafw.workflow.model.TaskInfo;

/**
 * A basic implementation of {@link TaskExecutor} for {@link DeleteTaskInfo}.
 * This implementation does not support Hadoop file system. To support Hadoop file systems, please inherit
 * this class and override {@link #deleteOnHadoopFileSystem(TaskExecutionContext, String)}.
 * @since 0.10.0
 */
public class BasicDeleteTaskExecutor implements TaskExecutor {

    static final Logger LOG = LoggerFactory.getLogger(BasicDeleteTaskExecutor.class);

    @Override
    public boolean isSupported(TaskExecutionContext context, TaskInfo task) {
        return task instanceof DeleteTaskInfo;
    }

    @Override
    public void execute(TaskExecutionContext context, TaskInfo task) throws IOException, InterruptedException {
        DeleteTaskInfo delete = (DeleteTaskInfo) task;
        String resolved = TaskExecutors.resolvePath(context, delete.getPath());
        LOG.info("delete file: {} ({})", resolved, delete.getPathKind());
        switch (delete.getPathKind()) {
        case LOCAL_FILE_SYSTEM:
            deleteOnLocalFileSystem(context, resolved);
            break;
        case HADOOP_FILE_SYSTEM:
            deleteOnHadoopFileSystem(context, resolved);
            break;
        default:
            throw new AssertionError(delete.getPathKind());
        }
    }

    /**
     * Delete files on local file system.
     * @param context the current context
     * @param path the target path
     * @throws IOException if I/O error was occurred while deleting files
     * @throws InterruptedException if interrupted while deleting files
     */
    protected void deleteOnLocalFileSystem(
            TaskExecutionContext context, String path) throws IOException, InterruptedException {
        Path base = Paths.get(path);
        if (Files.exists(base) == false) {
            LOG.debug("no files to delete: {}", path);
            return;
        }
        Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                super.postVisitDirectory(dir, exc);
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Delete files on Hadoop file system.
     * @param context the current context
     * @param path the target path
     * @throws IOException if I/O error was occurred while deleting files
     * @throws InterruptedException if interrupted while deleting files
     */
    protected void deleteOnHadoopFileSystem(
            TaskExecutionContext context, String path) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }
}
