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
package com.asakusafw.bulkloader.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.asakusafw.bulkloader.common.StreamRedirectThread;

/**
 * An abstract implementation of {@link FileListProvider} using I/O streams.
 * @since 0.2.3
 */
public abstract class StreamFileListProvider implements FileListProvider {

    private final List<Thread> running = new ArrayList<Thread>();

    @Override
    public FileList.Reader openReader() throws IOException {
        InputStream stream = getInputStream();
        boolean succeed = false;
        try {
            FileList.Reader channel = FileList.createReader(stream);
            succeed = true;
            return channel;
        } finally {
            if (succeed == false) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @Override
    public FileList.Writer openWriter(boolean compress) throws IOException {
        OutputStream stream = getOutputStream();
        boolean succeed = false;
        try {
            FileList.Writer channel = FileList.createWriter(stream, compress);
            succeed = true;
            return channel;
        } finally {
            if (succeed == false) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @Override
    public void discardReader() throws IOException {
        redirect(getInputStream(), System.out);
    }

    @Override
    public void discardWriter() throws IOException {
        getOutputStream().close();
    }

    @Override
    public final void waitForComplete() throws IOException, InterruptedException {
        synchronized (running) {
            for (Iterator<Thread> iter = running.iterator(); iter.hasNext();) {
                Thread next = iter.next();
                if (next.isAlive()) {
                    next.join();
                }
                iter.remove();
            }
        }
        waitForDone();
    }

    /**
     * Redirects the {@link InputStream} into the other {@link OutputStream}.
     * @param in source input stream
     * @param out redirect target
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected final void redirect(InputStream in, OutputStream out) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (out == null) {
            throw new IllegalArgumentException("out must not be null"); //$NON-NLS-1$
        }
        Thread t = new StreamRedirectThread(in, out);
        t.setDaemon(true);
        t.start();
        synchronized (running) {
            running.add(t);
        }
    }

    /**
     * Opens {@link InputStream} for this requestor.
     * @return the stream
     * @throws IOException if failed to open
     * @see #openReader()
     * @see #discardReader()
     */
    protected abstract InputStream getInputStream() throws IOException;

    /**
     * Opens {@link OutputStream} for this requestor.
     * @return the stream
     * @throws IOException if failed to open
     * @see #openWriter(boolean)
     * @see #discardWriter()
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    /**
     * Waits for this request is completed.
     * This method is invoked in {@link #waitForComplete()}.
     * @throws IOException if this request was failed
     * @throws InterruptedException if interrupted while waiting
     */
    protected abstract void waitForDone() throws IOException, InterruptedException;
}
