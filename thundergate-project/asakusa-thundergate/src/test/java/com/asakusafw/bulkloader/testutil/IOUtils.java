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
package com.asakusafw.bulkloader.testutil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * バイトストリームのためのユーティリティです。
 *
 * @author akira.kawaguchi
 */
public class IOUtils extends StreamCloseLogic {

    protected IOUtils() {
    }

    /**
     * <p>
     * 入力元からバイトを読み込んで出力先に書き込みます。
     * </p>
     * 引数に指定された入出力ストリームは、 結果の如何に関わらず確実にクローズされます。
     *
     * @see #piping(InputStream, OutputStream)
     */
    public static long pipingAndClose(final InputStream src,
            final OutputStream dest) throws IOException {
        return pipingAndClose(src, dest, -1);
    }

    /**
     * <p>
     * 入力元からバイトを読み込んで出力先に書き込みます。
     * </p>
     * 引数に指定された入出力ストリームは、 結果の如何に関わらず確実にクローズされます。
     *
     * @see #piping(InputStream, OutputStream, long)
     */
    public static long pipingAndClose(final InputStream src,
            final OutputStream dest, final long len) throws IOException {
        return pipingAndClose(src, dest, 0, len);
    }

    /**
     * <p>
     * 入力元からバイトを読み込んで出力先に書き込みます。
     * </p>
     * 引数に指定された入出力ストリームは、 結果の如何に関わらず確実にクローズされます。
     *
     * @see #piping(InputStream, OutputStream, long, long)
     */
    public static long pipingAndClose(final InputStream src,
            final OutputStream dest, final long off, final long len)
            throws IOException {
        return pipingAndClose(src, dest, off, len, DEFAULT_BUFFER_SIZE);
    }

    /**
     * <p>
     * 入力元からバイトを読み込んで出力先に書き込みます。
     * </p>
     * 引数に指定された入出力ストリームは、 結果の如何に関わらず確実にクローズされます。
     *
     * @see #piping(InputStream, OutputStream, long, long, int)
     */
    public static long pipingAndClose(final InputStream src,
            final OutputStream dest, final long off, final long len,
            final int bufSize) throws IOException {
        check(src, dest, off, bufSize);

        // ----------------------------------------------------------------------
        long result = 0;

        try {
            result = piping(src, dest, off, len, bufSize);
        } finally {
            closeGently(src);
            closeGently(dest);
        }

        return result;
    }

}

class StreamPipingLogic {
    static final int DEFAULT_BUFFER_SIZE = 8192;

    static final int EOF = -1;

    /**
     * 入力元からバイトを読み込んで出力先に書き込みます。
     *
     * @param src
     *            入力元
     * @param dest
     *            出力先
     * @return 書き込まれたバイト数
     * @exception IOException
     *                if an error occurs
     */
    public static long piping(final InputStream src, final OutputStream dest)
            throws IOException {
        return piping(src, dest, -1);
    }

    /**
     * 入力元から指定された長さのバイトを読み込んで出力先に書き込みます。
     *
     * @param src
     *            入力元
     * @param dest
     *            出力先
     * @param len
     *            書き込むバイト数
     * @return 書き込まれたバイト数
     * @exception IOException
     *                if an error occurs
     */
    public static long piping(final InputStream src, final OutputStream dest,
            final long len) throws IOException {
        return piping(src, dest, 0, len);
    }

    /**
     * 入力元から指定された長さのバイトを読み込んで出力先に書き込みます。
     *
     * @param src
     *            入力元
     * @param dest
     *            出力先
     * @param off
     *            開始位置
     * @param len
     *            書き込むバイト数
     * @return 書き込まれたバイト数
     * @exception IOException
     *                if an error occurs
     */
    public static long piping(final InputStream src, final OutputStream dest,
            final long off, final long len) throws IOException {
        return piping(src, dest, off, len, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 入力元から指定された長さのバイトを読み込んで出力先に書き込みます。
     *
     * @param src
     *            入力元
     * @param dest
     *            出力先
     * @param off
     *            開始位置
     * @param len
     *            書き込むバイト数
     * @param bufSize
     *            バッファサイズ
     * @return 書き込まれたバイト数
     * @exception IOException
     *                if an error occurs
     */
    public static long piping(final InputStream src, final OutputStream dest,
            final long off, long len, final int bufSize) throws IOException {
        check(src, dest, off, bufSize);

        if (len < 0) {
            len = Long.MAX_VALUE;
        }

        // ----------------------------------------------------------------------
        skip(src, off);

        final byte[] buf = new byte[bufSize];

        long wroteBytes = 0;

        while (true) {
            final long rest = len - wroteBytes;

            if (rest <= 0) {
                break;
            }

            final int readableLength;

            if (rest <= Integer.MAX_VALUE) {
                readableLength = Math.min(bufSize, (int) rest);
            } else {
                readableLength = bufSize;
            }

            final int readLength = src.read(buf, 0, readableLength);

            if (readLength < 0) {
                break;
            }

            dest.write(buf, 0, readLength);

            wroteBytes += readLength;
        }

        return wroteBytes;
    }

    /**
     * 指定されたバイト数をスキップします。
     *
     * @param src
     * @param len
     * @exception IOException
     *                if an error occurs
     */
    public static void skip(final InputStream src, final long len)
            throws IOException {
        for (long rest = len; 0 < rest;) {
            final long skipped = src.skip(rest);

            if (skipped <= 0) {
                throw new IOException("can't skip " + len + " bytes (" + rest
                        + " bytes were skipped.)");
            }

            rest -= skipped;
        }
    }

    static void check(final InputStream src, final OutputStream dest,
            final long off, final int bufSize) {
        if (src == null) {
            throw new NullPointerException("src is null");
        } else if (dest == null) {
            throw new NullPointerException("dest is null");
        } else if (off < 0) {
            throw new IllegalArgumentException("off < 0");
        } else if (bufSize <= 0) {
            throw new IllegalArgumentException("bufSize <= 0");
        }
    }
}

class StreamCloseLogic extends StreamPipingLogic {

    /**
     * 確実にクローズ処理を実行します。 如何なる例外・エラーもスローしないことが保証されます。
     */
    public static void closeGently(final InputStream src) {
        ExceptionHandler.handle(close(src));
    }

    /**
     * 確実にクローズ処理を実行します。 如何なる例外・エラーもスローしないことが保証されます。
     */
    public static void closeGently(final OutputStream dest) {
        ExceptionHandler.handle(close(dest));
    }

    public static Throwable close(final InputStream src) {
        if (src != null) {
            try {
                src.close();
            } catch (final Throwable t) {
                return t;
            }
        }

        return null;
    }

    public static Throwable close(final OutputStream dest) {
        if (dest != null) {
            try {
                dest.close();
            } catch (final Throwable t) {
                return t;
            }
        }

        return null;
    }

    /**
     * 確実にクローズ処理を実行します。 如何なる例外・エラーもスローしないことが保証されます。
     */
    public static void closeGently(final Reader src) {
        ExceptionHandler.handle(close(src));
    }

    /**
     * 確実にクローズ処理を実行します。 如何なる例外・エラーもスローしないことが保証されます。
     */
    public static void closeGently(final Writer dest) {
        ExceptionHandler.handle(close(dest));
    }

    public static Throwable close(final Reader src) {
        if (src != null) {
            try {
                src.close();
            } catch (final Throwable t) {
                return t;
            }
        }

        return null;
    }

    public static Throwable close(final Writer dest) {
        if (dest != null) {
            try {
                dest.close();
            } catch (final Throwable t) {
                return t;
            }
        }

        return null;
    }
}

/**
 *
 * キャッチしても、なにもできない例外を処理する場合に使用します。
 *
 */
abstract class ExceptionHandler {

    private static final ExceptionHandler DEFAULT_HANDLER = new ExceptionHandler() {

        @Override
        public void handleThrowable(final Throwable t) {
            t.printStackTrace();
        }
    };

    private static volatile ExceptionHandler handler = DEFAULT_HANDLER;

    protected ExceptionHandler() {
    }

    public abstract void handleThrowable(final Throwable t);

    public static ExceptionHandler getHandler() {
        return handler;
    }

    public static void setHandler(ExceptionHandler handler) {
        if (handler == null) {
            handler = DEFAULT_HANDLER;
        }

        ExceptionHandler.handler = handler;
    }

    public static void handle(final Throwable t) {
        if (t == null) {
            return;
        }

        try {
            handler.handleThrowable(t);
        } catch (final Throwable error) {
            error.printStackTrace();
        }
    }
}
