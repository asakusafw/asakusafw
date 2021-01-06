/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.windows.WinUtilsInstaller;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;

/**
 * Configures testing environment for Windows platform.
 * @since 0.7.4
 */
public class WindowsEnvironmentConfigurator extends TestingEnvironmentConfigurator {

    static final Logger LOG = LoggerFactory.getLogger(WindowsEnvironmentConfigurator.class);

    /**
     * The system property key of enabling this feature.
     */
    public static final String KEY_FEATURE_ENABLE = KEY_ENABLE + ".windows"; //$NON-NLS-1$

    /**
     * The default value of {@link #KEY_FEATURE_ENABLE}.
     */
    public static final String DEFAULT_FEATURE_ENABLE = "true"; //$NON-NLS-1$

    /**
     * The system property key of path to a {@code winutils.exe} file.
     */
    public static final String KEY_PATH_WINUTILS = KEY_ENABLE + ".winutils"; //$NON-NLS-1$

    @Override
    protected void configure() {
        if (WinUtilsInstaller.isTarget() == false) {
            return;
        }
        if (isEnabled() == false) {
            LOG.debug("Windows environment configurator is not active"); //$NON-NLS-1$
            return;
        }
        try {
            LOG.debug("checking winutils.exe"); //$NON-NLS-1$
            if (WinUtilsInstaller.isAlreadyInstalled()) {
                LOG.info(Messages.getString("WindowsEnvironmentConfigurator.infoAlreadyInstalled")); //$NON-NLS-1$
                return;
            }
            File install = prepare();
            assert install != null;
            WinUtilsInstaller.register(install);
            LOG.info(MessageFormat.format(
                    Messages.getString("WindowsEnvironmentConfigurator.infoCompleteInstall"), //$NON-NLS-1$
                    install));
        } catch (Exception e) {
            LOG.warn(Messages.getString("WindowsEnvironmentConfigurator.warnFailedToInstall"), e); //$NON-NLS-1$
        }
    }

    private boolean isEnabled() {
        String value = System.getProperty(KEY_FEATURE_ENABLE, DEFAULT_FEATURE_ENABLE);
        return value.equals("true"); //$NON-NLS-1$
    }

    private File prepare() throws IOException {
        String prepared = System.getProperty(KEY_PATH_WINUTILS);
        if (prepared != null) {
            File f = new File(prepared);
            if (f.canExecute()) {
                return f;
            }
            throw new FileNotFoundException(prepared);
        }
        File temporary = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
        File file = WinUtilsInstaller.put(temporary);
        return file;
    }
}
