/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.hadoop.fs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;

/**
 * CLI for Hadoop FS cleaning tool.
 * @since 0.4.0
 */
public class Clean extends Configured implements Tool {

    static final Log LOG = LogFactory.getLog(Clean.class);

    static final Option OPT_RECURSIVE;
    static final Option OPT_DRY_RUN;
    static final Option OPT_KEEP_DAYS;

    private static final Options OPTIONS;
    static {
        OPT_RECURSIVE = new Option("r", "recursive", false, "remove recursively"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_DRY_RUN = new Option("s", "dry-run", false, "do not delete actually"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_KEEP_DAYS = new Option("k", "keep-days", true, "keep files lecent days"); //$NON-NLS-1$ //$NON-NLS-2$

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_RECURSIVE);
        OPTIONS.addOption(OPT_DRY_RUN);
        OPTIONS.addOption(OPT_KEEP_DAYS);
    }

    private final long currentTime;

    /**
     * Creates a new instance.
     */
    public Clean() {
        this(System.currentTimeMillis());
    }

    Clean(long currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * Program entry.
     * @param args arguments
     * @throws Exception if failed to execute command
     */
    public static void main(String... args) throws Exception {
        int exit = exec(args);
        if (exit != 0) {
            System.exit(exit);
        }
    }

    /**
     * Program entry.
     * @param args arguments
     * @return the exit code
     * @throws Exception if failed to execute command
     */
    public static int exec(String... args) throws Exception {
        LOG.info("[OT-CLEAN-I00000] Start Hadoop FS cleaning tool");
        long start = System.currentTimeMillis();
        Tool tool = new Clean();
        tool.setConf(new Configuration());
        int exit = tool.run(args); // no generic options
        long end = System.currentTimeMillis();
        LOG.info(MessageFormat.format(
                "[OT-CLEAN-I00999] Finish Hadoop FS cleaning tool (exit-code={0}, elapsed={1}ms)",
                exit,
                end - start));
        return exit;
    }

    @Override
    public int run(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("args must not be null"); //$NON-NLS-1$
        }
        Opts opts;
        try {
            opts = parseOptions(args);
            if (opts == null) {
                return 2;
            }
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "[OT-CLEAN-E00001] Invalid options: {0}",
                    Arrays.toString(args)), e);
            return 2;
        }
        long period = currentTime - (long) (opts.keepDays * TimeUnit.DAYS.toMillis(1));
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Keep switching-time: {0}", new Date(period))); //$NON-NLS-1$
        }
        Context context = new Context(opts.recursive, period, opts.dryRun);
        for (Path path : opts.paths) {
            remove(path, context);
        }
        if (context.hasError()) {
            return 1;
        }
        return 0;
    }

    private Opts parseOptions(String[] args) throws ParseException {
        assert args != null;
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Parsing options: {0}", Arrays.toString(args))); //$NON-NLS-1$
        }

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        boolean recursive = cmd.hasOption(OPT_RECURSIVE.getOpt());
        String keepString = cmd.getOptionValue(OPT_KEEP_DAYS.getOpt());
        boolean dryRun = cmd.hasOption(OPT_DRY_RUN.getOpt());
        String[] rest = cmd.getArgs();

        if (keepString == null) {
            LOG.error(MessageFormat.format(
                    "[OT-CLEAN-E00001] Missing option: -{0}",
                    OPT_KEEP_DAYS.getLongOpt()));
            return null;
        }
        if (rest == null) {
            rest = new String[0];
        }

        if (LOG.isDebugEnabled())
         {
            LOG.debug(MessageFormat.format("Option {0}: {1}", OPT_RECURSIVE.getLongOpt(), recursive)); //$NON-NLS-1$
        }
        double keepDays;
        try {
            keepDays = Double.parseDouble(keepString);
        } catch (NumberFormatException e) {
            LOG.error(MessageFormat.format(
                    "[OT-CLEAN-E00001] -{0} must be a number: {1}",
                    OPT_KEEP_DAYS.getLongOpt(),
                    keepString));
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Option {0}: {1}", OPT_KEEP_DAYS.getLongOpt(), keepDays)); //$NON-NLS-1$
            LOG.debug(MessageFormat.format("Option {0}: {1}", OPT_DRY_RUN.getLongOpt(), dryRun)); //$NON-NLS-1$
        }

        List<Path> paths = new ArrayList<>();
        for (String pathString : rest) {
            if (pathString.trim().isEmpty()) {
                continue;
            }
            try {
                Path path = new Path(pathString);
                paths.add(path);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format("Option --: {0}", path)); //$NON-NLS-1$
                }
            } catch (RuntimeException e) {
                LOG.error(MessageFormat.format(
                        "[OT-CLEAN-E00001] Invalid target path: {0}",
                        pathString), e);
                return null;
            }
        }
        if (paths.isEmpty()) {
            LOG.error(MessageFormat.format(
                    "[OT-CLEAN-E00001] Missing target paths",
                    new Object[0]));
            return null;
        }
        return new Opts(recursive, keepDays, dryRun, paths);
    }

    boolean remove(Path path, Context context) {
        LOG.info(MessageFormat.format(
                "[OT-CLEAN-I01000] Start cleaning: {0}",
                path));
        FileSystem fs;
        try {
            fs = FileSystem.get(path.toUri(), getConf());
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "[OT-CLEAN-E01001] Failed to connect to filesystem: {0}",
                    path), e);
            context.setError();
            return false;
        }
        List<FileStatus> files;
        try {
            files = asList(fs.globStatus(path));
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "[OT-CLEAN-E01002] Failed to glob path pattern: {0}",
                    path), e);
            context.setError();
            return false;
        }
        if (files.isEmpty()) {
            LOG.warn(MessageFormat.format(
                    "[OT-CLEAN-W01001] Target file is not found: {0}",
                    path));
            context.setError();
            return false;
        }
        boolean removed = true;
        long start = System.currentTimeMillis();
        for (FileStatus file : files) {
            removed &= remove(fs, file, context);
        }
        long end = System.currentTimeMillis();
        LOG.info(MessageFormat.format(
                "[OT-CLEAN-I01999] Finish cleaning: {0} (all-removed={1}, elapsed={2}ms)",
                path,
                removed,
                end - start));
        return removed;
    }

    private boolean remove(FileSystem fs, FileStatus file, Context context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format("Attempt to remove {0}", file.getPath())); //$NON-NLS-1$
        }
        boolean isSymlink = context.isSymlink(fs, file);
        if (isSymlink) {
            LOG.error(MessageFormat.format(
                    "[OT-CLEAN-W01001] Symlink is currenty not supported: {0}",
                    file.getPath()));
            context.setError();
            return false;
        }
        if (file.isDirectory()) {
            if (context.isRecursive()) {
                List<FileStatus> children;
                try {
                    children = asList(fs.listStatus(file.getPath()));
                } catch (IOException e) {
                    LOG.error(MessageFormat.format(
                            "[OT-CLEAN-E01003] Failed to list directory: {0}",
                            file.getPath()), e);
                    context.setError();
                    return false;
                }
                boolean deleteChildren = true;
                for (FileStatus child : children) {
                    deleteChildren &= remove(fs, child, context);
                }
                if (deleteChildren == false) {
                    LOG.info(MessageFormat.format(
                            "[OT-CLEAN-I01004] Skipped: {0} (is no-empty directory)",
                            file.getPath(),
                            new Date(file.getModificationTime())));
                    return false;
                }
            } else {
                LOG.info(MessageFormat.format(
                        "[OT-CLEAN-I01003] Skipped: {0} (is directory)",
                        file.getPath(),
                        new Date(file.getModificationTime())));
                return false;
            }
        }
        if (context.canDelete(file)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Removing {0}", file.getPath())); //$NON-NLS-1$
            }
            if (context.isDryRun() == false) {
                try {
                    boolean removed = fs.delete(file.getPath(), false);
                    if (removed == false) {
                        LOG.error(MessageFormat.format(
                                "[OT-CLEAN-E01004] Failed to remove: {0}",
                                file.getPath()));
                        context.setError();
                        return false;
                    }
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "[OT-CLEAN-E01004] Failed to remove: {0}",
                            file.getPath()), e);
                    context.setError();
                    return false;
                }
            }
            LOG.info(MessageFormat.format(
                    "[OT-CLEAN-I01001] Removed: {0} (timestamp={1})",
                    file.getPath(),
                    new Date(file.getModificationTime())));
        } else {
            LOG.info(MessageFormat.format(
                    "[OT-CLEAN-I01002] Kept: {0} (timestamp={1})",
                    file.getPath(),
                    new Date(file.getModificationTime())));
            return false;
        }
        return true;
    }

    private List<FileStatus> asList(FileStatus[] files) {
        if (files == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(files);
        }
    }

    private static final class Opts {

        final boolean recursive;

        final double keepDays;

        final boolean dryRun;

        final List<Path> paths;

        Opts(boolean recursive, double keepDays, boolean dryRun, List<Path> paths) {
            this.recursive = recursive;
            this.keepDays = keepDays;
            this.dryRun = dryRun;
            this.paths = paths;
        }
    }

    private static final class Context {

        private final boolean recursive;

        private final long keepPeriod;

        private final boolean dryRun;

        private boolean sawError;

        private static final Method FILE_STATUS_IS_SYMLINK;
        static {
            Method m;
            try {
                m = FileStatus.class.getMethod("isSymlink"); //$NON-NLS-1$
            } catch (Exception e) {
                m = null;
                LOG.debug("FileStatus.isSymlink does not supported"); //$NON-NLS-1$
            }
            FILE_STATUS_IS_SYMLINK = m;
        }

        Context(boolean recursive, long keepPeriod, boolean dryRun) {
            this.recursive = recursive;
            this.keepPeriod = keepPeriod;
            this.dryRun = dryRun;
            this.sawError = false;
        }

        public boolean isSymlink(FileSystem fs, FileStatus file) {
            try {
                return isSymlink0(fs, file);
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Failed to resolve symlink", //$NON-NLS-1$
                            file.getPath()), e);
                }
                return false;
            }
        }

        private boolean isSymlink0(FileSystem fs, FileStatus file) throws IOException {
            assert fs != null;
            assert file != null;
            URI uri = file.getPath().toUri();
            if (uri.getScheme() == null) {
                uri = fs.makeQualified(file.getPath()).toUri();
                if (uri == null) {
                    return false;
                }
            }
            // NOTE: It seems that Hadoop 2.0 LocalFileSystem still does not support symlink.
            if (uri.getScheme().equals("file")) { //$NON-NLS-1$
                File f = new File(uri);
                File c = f.getCanonicalFile();
                if (f.equals(c)) {
                    return false;
                }
                if (f.getName().equals(c.getName()) == false) {
                    return true;
                }
                File p = f.getParentFile().getCanonicalFile();
                if (p.equals(c.getParentFile()) == false) {
                    return true;
                }
            } else if (FILE_STATUS_IS_SYMLINK != null) {
                try {
                    return Boolean.TRUE.equals(FILE_STATUS_IS_SYMLINK.invoke(file));
                } catch (Exception e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "Failed to invoke {0}({1})", //$NON-NLS-1$
                                FILE_STATUS_IS_SYMLINK.getName(),
                                file.getPath()), e);
                    }
                    return false;
                }
            }
            return false;
        }

        public boolean isRecursive() {
            return recursive;
        }

        public boolean isDryRun() {
            return dryRun;
        }

        public boolean canDelete(FileStatus file) {
            long lastModified = file.getModificationTime();
            return lastModified < keepPeriod;
        }

        public void setError() {
            this.sawError = true;
        }

        public boolean hasError() {
            return this.sawError;
        }
    }
}
