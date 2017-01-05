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
package com.asakusafw.runtime.windows;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Customize Hadoop APIs for Windows.
 * @since 0.9.0
 */
public final class WindowsConfigurator {

    static final Log LOG = LogFactory.getLog(WindowsConfigurator.class);

    /**
     * Installs configuration.
     */
    public static synchronized void install() {
        installWinUtils();
    }

    private static void installWinUtils() {
        if (WinUtilsInstaller.isTarget() == false) {
            return;
        }
        if (WinUtilsInstaller.isAlreadyInstalled()) {
            return;
        }
        File temporary = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
        try {
            File file = WinUtilsInstaller.put(temporary);
            WinUtilsInstaller.register(file);
        } catch (IOException e) {
            LOG.error("error occurred while configuring environment for Windows", e);
        }
    }

    private WindowsConfigurator() {
        return;
    }
}
