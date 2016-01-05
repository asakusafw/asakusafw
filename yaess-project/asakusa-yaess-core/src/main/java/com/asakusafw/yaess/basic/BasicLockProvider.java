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
package com.asakusafw.yaess.basic;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionLock;
import com.asakusafw.yaess.core.ExecutionLock.Scope;
import com.asakusafw.yaess.core.ExecutionLockProvider;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Basic implementation of {@link ExecutionLockProvider} using file system.
 * @since 0.2.3
 */
public class BasicLockProvider extends ExecutionLockProvider {

    static final Logger LOG = LoggerFactory.getLogger(BasicLockProvider.class);

    /**
     * Profile key name of session storage directory.
     * This value can includes environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    public static final String KEY_DIRECTORY = "directory";

    private volatile File directory;

    @Override
    public void doConfigure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        LOG.debug("Configuring file sessions: {}",
                profile.getPrefix());
        directory = prepareDirectory(profile);
        LOG.debug("Configured file sessions: {}",
                directory);
    }

    private File prepareDirectory(ServiceProfile<?> profile) throws IOException {
        assert profile != null;
        String path = profile.getConfiguration(KEY_DIRECTORY, true, true);
        File dir = new File(path);
        if (dir.isDirectory() == false && dir.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to prepare lock directory: {0}",
                    dir.getAbsolutePath()));
        }
        return dir;
    }

    @Override
    protected ExecutionLock newInstance(Scope lockScope, String batchId) throws IOException {
        if (lockScope == null) {
            throw new IllegalArgumentException("lockScope must not be null"); //$NON-NLS-1$
        }
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        return new FileExecutionLock(lockScope, batchId, directory);
    }
}
