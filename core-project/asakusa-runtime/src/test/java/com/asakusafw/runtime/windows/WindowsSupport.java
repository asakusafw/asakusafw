/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assume;
import org.junit.rules.ExternalResource;

/**
 * Enables tests in Windows platform.
 * @since 0.9.0
 */
public class WindowsSupport extends ExternalResource {

    static final Log LOG = LogFactory.getLog(WinUtilsInstaller.class);

    private final boolean skip;

    private volatile boolean cleanup = false;

    /**
     * Creates a new instance.
     */
    public WindowsSupport() {
        this(false);
    }

    /**
     * Creates a new instance.
     * @param skip skip if windows
     */
    public WindowsSupport(boolean skip) {
        this.skip = skip;
    }

    @Override
    protected void before() throws IOException {
        if (WinUtilsInstaller.isTarget()) {
            Assume.assumeFalse("skip this test", skip);
        } else {
            return;
        }
        try {
            LOG.debug("checking winutils.exe"); //$NON-NLS-1$
            if (WinUtilsInstaller.isAlreadyInstalled()) {
                return;
            }
            File install = prepare();
            assert install != null;
            WinUtilsInstaller.register(install);
            LOG.info(MessageFormat.format(
                    "winutils.exe was successfully installed", //$NON-NLS-1$
                    install));
            cleanup = true;
        } catch (Exception e) {
            LOG.warn("failed to install winutils.exe", e); //$NON-NLS-1$
        }
    }

    private File prepare() throws IOException {
        File temporary = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
        File file = WinUtilsInstaller.put(temporary);
        return file;
    }

    @Override
    protected void after() {
        if (cleanup) {
            WinUtilsInstaller.register(null);
        }
    }
}
