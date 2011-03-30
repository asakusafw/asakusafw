/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.compiler.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * テスト開始時にテンポラリフォルダを作成し、終了時に削除する。
 */
public class TemporaryFolder implements MethodRule {

    volatile File folder;

    final AtomicInteger serial = new AtomicInteger();

    /**
     * 新しいテンポラリフォルダを返す。
     * @return 新しいテンポラリフォルダ
     */
    public File newFolder() {
        File target = createLock();
        assertThat(target.mkdirs(), is(true));
        return target;
    }

    /**
     * 指定の入力をテンポラリフォルダ上に展開する。
     * @param input 入力
     * @return 展開先
     */
    public File copy(InputStream input) {
        File target = createLock();
        try {
            copyTo(input, target);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return target;
    }

    /**
     * 指定の入力をテンポラリフォルダ上に展開する。
     * @param path 展開先の相対パス
     * @param input 入力
     * @return 展開先
     */
    public File copy(String path, InputStream input) {
        File target = new File(folder, path);
        try {
            copyTo(input, target);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return target;
    }

    /**
     * 指定のアーカイブをテンポラリフォルダ上に展開する。
     * @param input 入力
     * @return 展開先のルートディレクトリ
     */
    public File extract(ZipInputStream input) {
        File root = createLock();
        assertThat(root.mkdirs(), is(true));
        try {
            while (true) {
                ZipEntry entry = input.getNextEntry();
                if (entry == null) {
                    break;
                }
                if (entry.isDirectory()) {
                    continue;
                }
                File file = new File(root, entry.getName());
                copyTo(input, file);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return root;
    }

    private void copyTo(InputStream input, File target) throws IOException {
        assert input != null;
        assert target != null;
        File parent = target.getParentFile();
        assertThat(parent.getAbsolutePath(), parent, not(nullValue()));
        if (parent.isDirectory() == false) {
            assertThat(parent.getAbsolutePath(), parent.mkdirs(), is(true));
        }

        FileOutputStream output = new FileOutputStream(target);
        try {
            byte[] buf = new byte[1024];
            while (true) {
                int read = input.read(buf);
                if (read < 0) {
                    break;
                }
                output.write(buf, 0, read);
            }
        } finally {
            output.close();
        }
    }

    private File createLock() {
        assertThat(folder, not(nullValue()));
        assertThat(folder.isDirectory(), is(true));
        for (int i = 0; i < 100; i++) {
            int number = serial.incrementAndGet();
            String name = String.format("resource_%d", number);
            File lock = new File(folder, name + ".lock");
            if (lock.mkdirs()) {
                return new File(folder, name);
            }
        }
        throw new AssertionError("Failed to aquire a temporary file lock");
    }

    @Override
    public Statement apply(
            final Statement base,
            final FrameworkMethod method,
            Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                create(method);
                try {
                    base.evaluate();
                } finally {
                    clean();
                }
            }
        };
    }

    void create(FrameworkMethod method) throws Exception {
        folder = File.createTempFile(
                String.format("%s_%s",
                        method.getMethod().getDeclaringClass().getSimpleName(),
                        method.getMethod().getName()),
                ".tmp");
        folder.delete();
        assertThat(folder.getAbsolutePath(), folder.mkdirs(), is(true));
    }

    void clean() {
        if (folder == null || folder.exists() == false) {
            return;
        }
        assertThat(folder.getAbsolutePath(), delete(folder), is(true));
    }

    private boolean delete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (delete(child) == false) {
                    return false;
                }
            }
        }
        return file.delete();
    }
}
