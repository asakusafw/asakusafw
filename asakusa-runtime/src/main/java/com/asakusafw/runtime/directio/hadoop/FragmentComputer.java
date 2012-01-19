/**
 * Copyright 2012 Asakusa Framework Team.
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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.asakusafw.runtime.directio.DirectInputFragment;

/**
 * compute fragments.
 * @since 0.2.5
 */
class FragmentComputer {

    static final long MAX_MIN_SIZE = Long.MAX_VALUE / 8;

    static final double MIN_LOCALITY = 0.125;

    static final double PRUNE_REL_LOCALITY = 0.75;

    private final long minSize;

    private final long prefSize;

    /**
     * Creates a new instance.
     * @param minSize minimum fragment size, or {@code < 0} for restrict fragmentation
     * @param prefSize preferred fragment size
     */
    public FragmentComputer(long minSize, long prefSize) {
        this.minSize = Math.min(minSize, MAX_MIN_SIZE);
        this.prefSize = Math.max(minSize, prefSize);
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
        BlockMap map = createBlockMap(path, fileSize, blocks);
        List<DirectInputFragment> fragments = computeFragments(map);
        return fragments;
    }

    private BlockMap createBlockMap(String path, long fileSize, Collection<BlockInfo> blockList) {
        assert path != null;
        assert blockList != null;
        BlockInfo[] blocks = blockList.toArray(new BlockInfo[blockList.size()]);
        Arrays.sort(blocks, new Comparator<BlockInfo>() {
            @Override
            public int compare(BlockInfo o1, BlockInfo o2) {
                int startDiff = compareLong(o1.start, o2.start);
                if (startDiff != 0) {
                    return startDiff;
                }
                return -compareLong(o1.hosts.length, o2.hosts.length);
            }
        });
        long lastOffset = 0;
        List<BlockInfo> results = new ArrayList<BlockInfo>();
        for (BlockInfo block : blocks) {
            // if block is out of bounds, skip it
            if (block.start >= fileSize) {
                continue;
            }
            // if block is gapped, add a padding block
            if (lastOffset < block.start) {
                results.add(new BlockInfo(lastOffset, block.start, null));
            }
            long start = Math.max(lastOffset, block.start);
            long end = Math.min(fileSize, block.end);
            // if block is empty, skip it
            if (start >= end) {
                continue;
            }

            results.add(new BlockInfo(start, end, block.hosts));
            lastOffset = end;
        }
        assert lastOffset <= fileSize;
        if (lastOffset < fileSize) {
            results.add(new BlockInfo(lastOffset, fileSize, null));
        }
        if (results.isEmpty()) {
            results.add(new BlockInfo(0, fileSize, null));
        }
        return new BlockMap(path, compaction(results));
    }

    private BlockInfo[] compaction(List<BlockInfo> blocks) {
        assert blocks != null;
        List<BlockInfo> results = new ArrayList<BlockInfo>(blocks.size());
        Iterator<BlockInfo> iter = blocks.iterator();
        assert iter.hasNext();
        BlockInfo last = iter.next();
        while (iter.hasNext()) {
            BlockInfo next = iter.next();
            if (last.isSameOwner(next)) {
                last = new BlockInfo(last.start, next.end, last.hosts);
            } else {
                results.add(last);
                last = next;
            }
        }
        results.add(last);
        return results.toArray(new BlockInfo[results.size()]);
    }

    private List<DirectInputFragment> computeFragments(BlockMap map) {
        assert map != null;
        long size = map.size;
        if (canFragmentation() == false || size / 2 < minSize) {
            return Collections.singletonList(map.get(0, size));
        }
        assert minSize > 0;
        assert map.size > minSize;
        BitSet processed = new BitSet(map.blocks.length);
        BlockInfo[] blocks = map.blocks;
        assert size == blocks[blocks.length - 1].end;
        List<DirectInputFragment> results = new ArrayList<DirectInputFragment>();
        for (int index = processed.nextClearBit(0); index < blocks.length; index = processed.nextClearBit(index + 1)) {
            int lastIndex = index + 1;
            BlockInfo startBlock = blocks[index];
            assert size - startBlock.start >= minSize;
            while (blocks[lastIndex - 1].end - startBlock.start < minSize) {
                lastIndex++;
                assert lastIndex <= blocks.length;
            }
            long rest = size - blocks[lastIndex - 1].end;
            if (rest < minSize) {
                lastIndex = blocks.length;
            }
            processed.set(index, lastIndex);
            long start = startBlock.start;
            long end = blocks[lastIndex - 1].end;
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
        assert map.size == expectedOffset : map.size + " != " + expectedOffset;
        return true;
    }

    static int compareLong(long offset1, long offset2) {
        if (offset1 < offset2) {
            return -1;
        } else if (offset1 > offset2) {
            return +1;
        }
        return 0;
    }

    private static class BlockMap {

        final String path;

        final BlockInfo[] blocks;

        final long size;

        BlockMap(String path, BlockInfo[] blocks) {
            assert path != null;
            assert blocks != null;
            assert blocks.length >= 1;
            this.path = path;
            this.blocks = blocks;
            this.size = blocks[blocks.length - 1].end - blocks[0].start;
        }

        public DirectInputFragment get(long start, long end) {
            List<String> hosts = computeHosts(start, end);
            return new DirectInputFragment(path, start, end - start, hosts);
        }

        private List<String> computeHosts(long start, long end) {
            assert start <= end;
            if (start == end) {
                return Collections.emptyList();
            }
            List<Map.Entry<String, Long>> rank = computeLocalityRank(start, end);
            if (rank.isEmpty()) {
                return Collections.emptyList();
            }
            long max = rank.get(0).getValue();
            if (max < (end - start) * MIN_LOCALITY) {
                return Collections.emptyList();
            }
            long threshold = (long) (max * PRUNE_REL_LOCALITY);
            List<String> results = new ArrayList<String>();
            for (int i = 0, n = rank.size(); i < n; i++) {
                Map.Entry<String, Long> block = rank.get(i);
                if (block.getValue() < threshold) {
                    break;
                }
                results.add(block.getKey());
            }
            return results;
        }

        private List<Map.Entry<String, Long>> computeLocalityRank(long start, long end) {
            Map<String, Long> ownBytes = new HashMap<String, Long>();
            for (BlockInfo block : blocks) {
                if (block.end < start) {
                    continue;
                }
                if (block.start >= end) {
                    break;
                }
                long s = Math.max(start, block.start);
                long e = Math.min(end, block.end);
                long length = e - s;
                for (String node : block.hosts) {
                    Long bytes = ownBytes.get(node);
                    if (bytes == null) {
                        ownBytes.put(node, length);
                    } else {
                        ownBytes.put(node, bytes + length);
                    }
                }
            }
            if (ownBytes.isEmpty()) {
                return Collections.emptyList();
            }
            List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(ownBytes.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<String, Long>>() {
                @Override
                public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                    return -compareLong(o1.getValue(), o2.getValue());
                }
            });
            return entries;
        }
    }
}
