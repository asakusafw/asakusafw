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
package com.asakusafw.testdriver.windows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.hadoop.util.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.OSInfo;

/**
 * Installs {@code winutils.exe} into the current environment.
 * @since 0.7.4
 */
public final class WinUtilsInstaller {

    static final Logger LOG = LoggerFactory.getLogger(WinUtilsInstaller.class);

    private static final Field WINUTILS_PATH;
    static {
        Field f = null;
        if (Shell.WINDOWS) {
            try {
                // only Hadoop 2.x has 'Shell.WINUTILS:String'
                f = Shell.class.getField("WINUTILS"); //$NON-NLS-1$
                f.setAccessible(true);
                if (f.getType() != String.class) {
                    throw new IllegalStateException(MessageFormat.format(
                            Messages.getString("WinUtilsInstaller.errorIncompatibleTargetFieldType"), //$NON-NLS-1$
                            f.getType().getName()));
                }

                // Hack: we try to remove final flag from 'Shell.WINUTILS'
                Field modifiers = Field.class.getDeclaredField("modifiers"); //$NON-NLS-1$
                modifiers.setAccessible(true);
                modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            } catch (Exception e) {
                LOG.debug("current Hadoop version does not support winutils.exe", e); //$NON-NLS-1$
            }
        }
        WINUTILS_PATH = f;
    }

    private static final String ARCH_32 = "x86"; //$NON-NLS-1$

    private static final String ARCH_64 = "x86_64"; //$NON-NLS-1$

    private static final String SOURCE = getArchitecture() + "/winutils.exe"; //$NON-NLS-1$

    private static final String TARGET_PREFIX = "winutils-" + getUserHash(); //$NON-NLS-1$

    private static final String TARGET_SUFFIX = ".exe"; //$NON-NLS-1$

    private static final AtomicReference<TemporaryFileInstaller> CACHE =
            new AtomicReference<TemporaryFileInstaller>();

    private WinUtilsInstaller() {
        return;
    }

    /**
     * Returns whether the current environment requires {@code winutils.exe} or not.
     * @return {@code true} if the current environment requires {@code winutils.exe}, otherwise {@code false}
     */
    public static boolean isTarget() {
        return WINUTILS_PATH != null;
    }

    /**
     * Returns whether the current environment already has {@code winutils.exe} or not.
     * @return {@code true} if the current environment already has {@code winutils.exe}, otherwise {@code false}
     */
    public static boolean isAlreadyInstalled() {
        if (isTarget() == false) {
            return false;
        }
        try {
            String path = (String) WINUTILS_PATH.get(null);
            return path != null && new File(path).canExecute();
        } catch (Exception e) {
            LOG.debug("exception occurred while checking winutils.exe", e); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Installs {@code winutils.exe} into the target directory.
     * @param directory the target directory
     * @return installed location (never null)
     * @throws IOException if error occurred while installing
     */
    public static File put(File directory) throws IOException {
        TemporaryFileInstaller installer = prepare();
        assert installer != null;

        // install into the default location
        File file = new File(directory, TARGET_PREFIX + TARGET_SUFFIX);
        try {
            LOG.info(MessageFormat.format(
                    Messages.getString("WinUtilsInstaller.infoInstallToDefaultLocation"), //$NON-NLS-1$
                    file));
            installer.install(file, true);
            return file;
        } catch (IOException e) {
            LOG.debug("failed to install winutils into the default location: {}", file, e); //$NON-NLS-1$
        }

        // install into a temporary location
        File temp = null;
        boolean success = false;
        try {
            temp = File.createTempFile(TARGET_PREFIX + '-', TARGET_SUFFIX, directory);
            LOG.info(MessageFormat.format(
                    Messages.getString("WinUtilsInstaller.infoInstallToTemporaryLocation"), //$NON-NLS-1$
                    temp));
            installer.install(temp, false);
            temp.deleteOnExit();
            success = true;
            return temp;
        } catch (IOException e) {
            LOG.debug("failed to install winutils into a temporary location: {}", temp, e); //$NON-NLS-1$
        } finally {
            if (success == false) {
                if (temp != null && temp.delete() == false) {
                    LOG.warn(MessageFormat.format(
                            Messages.getString("WinUtilsInstaller.warnFailedToDeleteTemporaryFile"), //$NON-NLS-1$
                            temp));
                }
            }
        }

        throw new IOException(MessageFormat.format(
                Messages.getString("WinUtilsInstaller.errorFailedToInstall"), //$NON-NLS-1$
                file));
    }

    /**
     * Registers a {@code winutils.exe} file.
     * @param executable the executable file path
     */
    public static void register(File executable) {
        try {
            WINUTILS_PATH.set(null, executable.getAbsolutePath());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static TemporaryFileInstaller prepare() throws IOException {
        TemporaryFileInstaller installer = CACHE.get();
        if (installer != null) {
            return installer;
        }
        InputStream input = WinUtilsInstaller.class.getResourceAsStream(SOURCE);
        if (input == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "missing {0} in the classpath", //$NON-NLS-1$
                    SOURCE));
        }
        try {
            installer = TemporaryFileInstaller.newInstance(input, true);
            CACHE.compareAndSet(null, installer);
        } finally {
            input.close();
        }
        return CACHE.get();
    }

    private static String getArchitecture() {
        String name = OSInfo.getArchName();
        if (name.equals(ARCH_32) || name.equals(ARCH_64)) {
            return name;
        }
        return ARCH_64;
    }

    private static String getUserHash() {
        String base = System.getProperty("user.name", UUID.randomUUID().toString()); //$NON-NLS-1$
        StringBuilder buf = new StringBuilder();
        for (char c : base.toCharArray()) {
            if ('0' <= c && c <= '9'
                    || 'A' <= c && c <= 'Z'
                    || 'a' <= c && c <= 'z'
                    || c == '_'
                    || c == '-') {
                buf.append(c);
            }
        }
        if (buf.length() < 4) {
            buf.append('-');
            buf.append(Integer.toHexString(base.hashCode()));
        }
        return buf.toString();
    }
}
