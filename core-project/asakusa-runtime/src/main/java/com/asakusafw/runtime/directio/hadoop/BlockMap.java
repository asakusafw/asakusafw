/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import com.asakusafw.runtime.directio.DirectInputFragment;

/**
 * Utilities for file blocks.
 * @see BlockInfo
 * @since 0.7.0
 */
public final class BlockMap {

    static final double MIN_LOCALITY = 0.125;

    static final double PRUNE_REL_LOCALITY = 0.75;

    private final String path;

    private final BlockInfo[] blocks;

    private final long size;

    private BlockMap(String path, BlockInfo[] blocks) {
        assert path != null;
        assert blocks != null;
        assert blocks.length >= 1;
        this.path = path;
        this.blocks = blocks;
        this.size = blocks[blocks.length - 1].end - blocks[0].start;
    }

    /**
     * Returns the file size.
     * @return the size
     */
    public long getFileSize() {
        return size;
    }

    /**
     * Returns the file blocks in this map.
     * @return the file blocks
     */
    public List<BlockInfo> getBlocks() {
        return Arrays.asList(blocks);
    }

    /**
     * Returns a list of {@link BlockInfo} for the target file.
     * @param fs the target file
     * @param status the target file status
     * @return the computed information
     * @throws IOException if failed to compute information
     */
    public static List<BlockInfo> computeBlocks(FileSystem fs, FileStatus status) throws IOException {
        BlockLocation[] locations = fs.getFileBlockLocations(status, 0, status.getLen());
        List<BlockInfo> results = new ArrayList<>();
        for (BlockLocation location : locations) {
            long length = location.getLength();
            long start = location.getOffset();
            results.add(new BlockInfo(start, start + length, location.getHosts()));
        }
        return results;
    }

    /**
     * Create {@link BlockMap}.
     * @param path the target file path
     * @param fileSize the target file size
     * @param blockList the original block list
     * @param combineBlocks {@code true} to combine consecutive blocks with same owners
     * @return the built object
     */
    public static BlockMap create(
            String path, long fileSize,
            Collection<BlockInfo> blockList,
            boolean combineBlocks) {
        assert path != null;
        assert blockList != null;
        BlockInfo[] blocks = blockList.toArray(new BlockInfo[blockList.size()]);
        Arrays.sort(blocks, (o1, o2) -> {
            int startDiff = Long.compare(o1.start, o2.start);
            if (startDiff != 0) {
                return startDiff;
            }
            return Integer.compare(o2.hosts.length, o1.hosts.length);
        });
        long lastOffset = 0;
        List<BlockInfo> results = new ArrayList<>();
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
        if (combineBlocks) {
            results = combine(results);
        }
        return new BlockMap(path, results.toArray(new BlockInfo[results.size()]));
    }

    private static List<BlockInfo> combine(List<BlockInfo> blocks) {
        assert blocks != null;
        List<BlockInfo> results = new ArrayList<>(blocks.size());
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
        return results;
    }

    /**
     * Returns {@link DirectInputFragment} for the range.
     * @param start the start offset (inclusive)
     * @param end the end offset (exclusive)
     * @return the computed fragment
     */
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
        List<String> results = new ArrayList<>();
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
        Map<String, Long> ownBytes = new HashMap<>();
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
            for (String node : block.getHosts()) {
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
        List<Map.Entry<String, Long>> entries = new ArrayList<>(ownBytes.entrySet());
        Collections.sort(entries, (o1, o2) -> Long.compare(o2.getValue(), o1.getValue()));
        return entries;
    }
}