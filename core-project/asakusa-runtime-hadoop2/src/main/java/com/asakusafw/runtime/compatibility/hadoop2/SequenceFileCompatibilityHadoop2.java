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
package com.asakusafw.runtime.compatibility.hadoop2;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;
import org.apache.hadoop.io.SequenceFile;

import com.asakusafw.runtime.compatibility.hadoop.SequenceFileCompatibilityHadoop;

/**
 * Compatibility for {@link SequenceFile} APIs (Hadoop {@code 2.x}).
 * Clients should not use this class directly.
 * @since 0.7.4
 */
public final class SequenceFileCompatibilityHadoop2 extends SequenceFileCompatibilityHadoop {

    @Override
    public SequenceFile.Reader openReader(
            InputStream in,
            long length,
            Configuration conf) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        return new SequenceFile.Reader(
                conf,
                SequenceFile.Reader.stream(new FSDataInputStream(new WrappedInputStream(in))),
                SequenceFile.Reader.length(length));
    }

    private static class WrappedInputStream extends InputStream implements Seekable, PositionedReadable {

        private final InputStream input;

        private long current;

        WrappedInputStream(InputStream input) {
            assert input != null;
            this.input = input;
            this.current = 0L;
        }

        @Override
        public int read(long position, byte[] buffer, int offset, int length) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void readFully(long position, byte[] buffer) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void seek(long pos) throws IOException {
            if (pos < current) {
                throw new UnsupportedOperationException();
            }
            while (pos > current) {
                skip0(pos - current);
            }
        }

        @Override
        public long getPos() throws IOException {
            return current;
        }

        @Override
        public boolean seekToNewSource(long targetPos) throws IOException {
            return false;
        }

        @Override
        public int read() throws IOException {
            int result = input.read();
            if (result >= 0) {
                current++;
            }
            return result;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int result = input.read(b);
            if (result >= 0) {
                current += result;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int result = input.read(b, off, len);
            if (result >= 0) {
                current += result;
            }
            return result;
        }

        @Override
        public long skip(long n) throws IOException {
            return skip0(n);
        }

        private long skip0(long n) throws IOException {
            long result = input.skip(n);
            if (result >= 0) {
                current += result;
            }
            return result;
        }

        @Override
        public int available() throws IOException {
            return input.available();
        }

        @Override
        public void close() throws IOException {
            input.close();
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public synchronized void mark(int readlimit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void reset() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
