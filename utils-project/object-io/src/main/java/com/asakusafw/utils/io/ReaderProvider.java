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
package com.asakusafw.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * Provides a {@link Reader}.
 * @since 0.6.0
 */
public class ReaderProvider implements Provider<Reader> {

    private final Provider<? extends InputStream> streamProvider;

    private final Charset encoding;

    /**
     * Creates a new instance.
     * @param streamProvider the source {@link InputStream} {@link Provider provider}
     * @param encoding the {@link InputStream} encoding charset
     */
    public ReaderProvider(Provider<? extends InputStream> streamProvider, Charset encoding) {
        this.streamProvider = streamProvider;
        this.encoding = encoding;
    }

    @Override
    public Reader open() throws IOException, InterruptedException {
        return new InputStreamReader(streamProvider.open(), encoding);
    }

    @Override
    public void close() throws IOException {
        streamProvider.close();
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}@{1}", //$NON-NLS-1$
                streamProvider,
                encoding);
    }
}
