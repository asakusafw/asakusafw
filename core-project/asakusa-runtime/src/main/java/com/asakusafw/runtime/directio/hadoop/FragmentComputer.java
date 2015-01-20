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
package com.asakusafw.runtime.directio.hadoop;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.asakusafw.runtime.directio.DirectInputFragment;

/**
 * compute fragments.
 * @since 0.2.5
 * @version 0.7.0
 */
class FragmentComputer {

    static final long MAX_MIN_SIZE = Long.MAX_VALUE / 8;

    private final long minSize;

    private final long prefSize;

    private final boolean combineBlocks;

    private final boolean splitBlocks;

    /**
     * Creates a new instance.
     * @since 0.7.0
     */
    public FragmentComputer() {
        this(-1L, -1L, false, false);
    }

    /**
     * Creates a new instance.
     * @param minSize minimum fragment size, or {@code < 0} for restrict fragmentation
     * @param prefSize preferred fragment size
     * @param combineBlocks whether combines blocks
     * @param splitBlocks {@code true} to split fragments
     */
    public FragmentComputer(long minSize, long prefSize, boolean combineBlocks, boolean splitBlocks) {
        this.minSize = Math.min(minSize, MAX_MIN_SIZE);
        this.prefSize = Math.max(minSize, prefSize);
        this.combineBlocks = combineBlocks;
        this.splitBlocks = splitBlocks;
    }

    /**
     * Computes fragments from blocks hint.
     * @param path the target path
     * @param fileSize target file size
     * @param blocks block hints
     * @return the computed fragments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<DirectInputFragment> computeFragments(String path, long fileSize, Collection<BlockInfo> blocks) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        if (blocks == null) {
            throw new IllegalArgumentException("blocks must not be null"); //$NON-NLS-1$
        }
        BlockMap map = BlockMap.create(path, fileSize, blocks, combineBlocks);
        List<DirectInputFragment> fragments = computeFragments(map);
        return fragments;
    }

    private List<DirectInputFragment> computeFragments(BlockMap map) {
        assert map != null;
        long size = map.getFileSize();
        if (canFragmentation() == false || size / 2 < minSize) {
            return Collections.singletonList(map.get(0, size));
        }
        assert minSize > 0;
        assert map.getFileSize() > minSize;
        List<BlockInfo> blocks = map.getBlocks();
        BitSet processed = new BitSet(blocks.size());
        assert size == blocks.get(blocks.size() - 1).end;
        List<DirectInputFragment> results = new ArrayList<DirectInputFragment>();
        for (int index = processed.nextClearBit(0); index < blocks.size(); index = processed.nextClearBit(index + 1)) {
            int lastIndex = index + 1;
            BlockInfo startBlock = blocks.get(index);
            assert size - startBlock.start >= minSize;
            while (blocks.get(lastIndex - 1).end - startBlock.start < minSize) {
                lastIndex++;
                assert lastIndex <= blocks.size();
            }
            long rest = size - blocks.get(lastIndex - 1).end;
            if (rest < minSize) {
                lastIndex = blocks.size();
            }
            processed.set(index, lastIndex);
            long start = startBlock.start;
            long end = blocks.get(lastIndex - 1).end;
            if (splitBlocks) {
                long groupSize = end - start;
                int fragmentCount = Math.max((int) (groupSize / prefSize), 1);
                long eachFragmentSize = (groupSize + fragmentCount - 1) / fragmentCount;
                long offset = start;
                while (offset != end) {
                    long fragmentSize = Math.min(end - offset, eachFragmentSize);
                    results.add(map.get(offset, offset + fragmentSize));
                    offset += fragmentSize;
                    assert offset <= end : offset + " > " + end;
                }
                assert offset == end;
            } else {
                results.add(map.get(start, end));
            }
        }
        assert validFragments(map, results);
        return results;
    }

    private boolean canFragmentation() {
        return minSize >= 1;
    }

    private boolean validFragments(BlockMap map, List<DirectInputFragment> results) {
        assert map != null;
        assert results != null;
        Collections.sort(results, new Comparator<DirectInputFragment>() {
            @Override
            public int compare(DirectInputFragment o1, DirectInputFragment o2) {
                long i1 = o1.getOffset();
                long i2 = o2.getOffset();
                if (i1 < i2) {
                    return -1;
                }
                if (i1 > i2) {
                    return +1;
                }
                return 0;
            }
        });
        long expectedOffset = 0;
        for (DirectInputFragment fragment : results) {
            long offset = fragment.getOffset();
            assert offset == expectedOffset : offset + " != " + expectedOffset;
            expectedOffset = offset + fragment.getSize();
        }
        assert map.getFileSize() == expectedOffset : map.getFileSize() + " != " + expectedOffset;
        return true;
    }
}
