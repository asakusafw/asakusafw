/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Writable;

/**
 * Implementation of {@link ListBuffer} using a file as backing store.
 * @param <E> element type
 */
public class FileMapListBuffer<E extends Writable>
        extends AbstractList<E> implements ListBuffer<E>, RandomAccess {

    static final Log LOG = LogFactory.getLog(FileMapListBuffer.class);

    private static final int DEFAULT_BUFFER_SIZE = 256;

    private static final int MINIMUM_BUFFER_SIZE = 32;

    private final BackingStore backingStore;

    private final E[] pageBuffer;

    private int currentPage;

    private int size;

    private int cursor;

    private int limit;

    /**
     * Creates a new instance with default buffer size.
     */
    public FileMapListBuffer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a new instance with default buffer size.
     * If buffer size is too small, the recommended minimum buffer size is used.
     * @param bufferSize initial buffer size (number of objects)
     */
    @SuppressWarnings("unchecked")
    public FileMapListBuffer(int bufferSize) {
        this.backingStore = new BackingStore();
        this.pageBuffer = (E[]) new Writable[Math.max(bufferSize, MINIMUM_BUFFER_SIZE)];
        this.size = 0;
        this.cursor = -1;
        this.limit = 0;
    }

    @Override
    public void begin() {
        size = -1;
        cursor = 0;
        currentPage = 0;
        modCount++;
    }

    @Override
    public void end() {
        if (cursor >= 0) {
            size = cursor;
            cursor = -1;
            modCount++;
        }
    }

    @Override
    public boolean isExpandRequired() {
        return limit <= toElementOffsetInPage(cursor);
    }

    @Override
    public void expand(E value) {
        pageBuffer[limit] = value;
        limit++;
    }

    @Override
    public E advance() {
        escapePage(cursor);
        E next = pageBuffer[toElementOffsetInPage(cursor)];
        cursor++;
        return next;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        restorePage(index);
        return pageBuffer[toElementOffsetInPage(index)];
    }

    private int toElementOffsetInPage(int index) {
        return index % pageBuffer.length;
    }

    private void escapePage(int index) {
        int targetPage = getTargetPage(index);
        if (targetPage != currentPage) {
            saveCurrentPage();
            currentPage = targetPage;
        }
    }

    private void restorePage(int index) {
        int targetPage = getTargetPage(index);
        if (targetPage != currentPage) {
            saveCurrentPage();
            try {
                backingStore.restore(targetPage, pageBuffer);
            } catch (IOException e) {
                throw new BufferException(MessageFormat.format(
                        "Failed to restore a page: index={0}, targetPage={1}, pageSize={2}",
                        index,
                        targetPage,
                        pageBuffer.length), e);
            }
            currentPage = targetPage;
        }
    }

    private void saveCurrentPage() throws AssertionError {
        assert limit == pageBuffer.length : "page buffer should be full";
        if (backingStore.isSaved(currentPage) == false) {
            try {
                backingStore.save(currentPage, pageBuffer);
            } catch (IOException e) {
                throw new BufferException(MessageFormat.format(
                        "Failed to save a page: currentPage={0}, pageSize={1}",
                        currentPage,
                        pageBuffer.length), e);
            }
        }
    }

    private int getTargetPage(int index) {
        return index / pageBuffer.length;
    }

    @Override
    public void shrink() {
        try {
            backingStore.shrink();
        } catch (IOException e) {
            LOG.warn("Failed to shrink the backing store", e);
        }
    }

    private static class BackingStore {

        private static final int INITIAL_INDEX_SIZE = 16;

        private static final String PAGE_STORE_PREFIX = "FileMap";

        private static final String PAGE_STORE_SUFFIX = ".tmp";

        private static final int NOT_SAVED = -1;

        private File mapFilePath;

        private RandomAccessFile mapFile;

        private long cursor;

        private long[] pageIndex;

        public BackingStore() {
            cursor = NOT_SAVED;
            pageIndex = new long[INITIAL_INDEX_SIZE];
            Arrays.fill(pageIndex, NOT_SAVED);
        }

        public boolean isSaved(int page) {
            if (page < pageIndex.length) {
                return pageIndex[page] != NOT_SAVED;
            }
            return false;
        }

        public void save(int pageNumber, Writable[] objects) throws IOException {
            prepareMapFile(objects);
            prepaerPageIndex(pageNumber);
            assert mapFile != null;
            assert pageNumber < pageIndex.length;
            mapFile.seek(cursor);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Saving a page into backing store: path={0}, index={1}, cursor={2}",
                        mapFilePath,
                        pageNumber * objects.length,
                        cursor));
            }
            for (Writable writable : objects) {
                writable.write(mapFile);
            }
            pageIndex[pageNumber] = cursor;
            cursor = mapFile.getFilePointer();
        }

        private void prepareMapFile(Writable[] objects) throws IOException {
            if (cursor == NOT_SAVED) {
                assert mapFile == null;
                assert mapFilePath == null;
                mapFilePath = File.createTempFile(PAGE_STORE_PREFIX, PAGE_STORE_SUFFIX);
                LOG.info(MessageFormat.format(
                        "Initializing a backing store for FileMapListBuffer: {0}",
                        mapFilePath));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Preparing map file: path={0}, sample={1}",
                            mapFilePath,
                            objects[0]));
                }
                mapFile = new RandomAccessFile(mapFilePath, "rw");
                cursor = 0;
            }
        }

        private void prepaerPageIndex(int pageNumber) {
            if (pageNumber < pageIndex.length) {
                return;
            }
            long[] newPageIndex = Arrays.copyOf(pageIndex, Math.min(pageIndex.length * 2, pageNumber + 1));
            Arrays.fill(newPageIndex, pageIndex.length, newPageIndex.length, NOT_SAVED);
            pageIndex = newPageIndex;
        }

        public void restore(int pageNumber, Writable[] objects) throws IOException {
            if (isSaved(pageNumber) == false) {
                throw new IOException(MessageFormat.format(
                        "Page {0} is not saved",
                        pageNumber));
            }
            long start = pageIndex[pageNumber];
            assert start != NOT_SAVED;
            assert mapFile != null;
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Restoring a page from backing store: path={0}, index={1}, cursor={2}",
                        mapFilePath,
                        pageNumber * objects.length,
                        start));
            }
            mapFile.seek(start);
            for (Writable writable : objects) {
                writable.readFields(mapFile);
            }
        }

        public void shrink() throws IOException {
            if (cursor != NOT_SAVED) {
                assert mapFile != null;
                assert mapFilePath != null;
                mapFile.close();
                if (mapFilePath.delete() == false) {
                    LOG.warn(MessageFormat.format(
                            "Failed to delete map file: {0}",
                            mapFilePath));
                }
                Arrays.fill(pageIndex, NOT_SAVED);
                cursor = NOT_SAVED;
                mapFile = null;
                mapFilePath = null;
            }
            assert mapFile == null;
            assert mapFilePath == null;
        }
    }
}
