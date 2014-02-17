/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.input;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper;

import com.asakusafw.runtime.stage.input.StageInputSplit.Source;

/**
 * A default implementation of {@link SplitCombiner}.
 * @since 0.2.6
 */
public class DefaultSplitCombiner extends SplitCombiner {

    static final Log LOG = LogFactory.getLog(DefaultSplitCombiner.class);

    static final String KEY_MAX = "com.asakusafw.input.combine.max";

    static final String KEY_GENERATIONS = "com.asakusafw.input.combine.ga.generation";

    static final String KEY_POPULATIONS = "com.asakusafw.input.combine.ga.population";

    static final String KEY_MUTATION_RATIO = "com.asakusafw.input.combine.ga.mutation";

    static final double DEFAULT_LOCAL_SCORE_FACTOR = 3.0;

    static final double DEFAULT_GOBAL_SCORE_FACTOR = 5.0;

    static final int DEFAULT_POPULATIONS = 20;

    static final int DEFAULT_GENERATIONS = 50;

    static final float DEFAULT_MUTATION_RATIO = 0.001f;

    static final int MIN_POPULATIONS = 10;

    static final int MIN_GENERATIONS = 5;

    static final double MIN_MUTATION_RATIO = 0;

    static final double LOCALITY_TOTAL_FACTOR = 0.5;

    static final double LOCALITY_COMPARISON_FACTOR = 0.8;

    @Override
    protected List<StageInputSplit> combine(
            JobContext context,
            List<StageInputSplit> splits) throws IOException, InterruptedException {
        int max = getMaxSplitsPerMapper(context);
        int populations = context.getConfiguration().getInt(KEY_POPULATIONS, DEFAULT_POPULATIONS);
        int generations = context.getConfiguration().getInt(KEY_GENERATIONS, DEFAULT_GENERATIONS);
        double mutations = context.getConfiguration().getFloat(KEY_MUTATION_RATIO, DEFAULT_MUTATION_RATIO);
        populations = Math.max(populations, MIN_POPULATIONS);
        generations = Math.max(generations, MIN_GENERATIONS);
        mutations = Math.max(mutations, MIN_MUTATION_RATIO);
        return combine(max, populations, generations, mutations, splits);
    }

    List<StageInputSplit> combine(
            int max,
            int populations,
            int generations,
            double mutations,
            List<StageInputSplit> splits) throws IOException, InterruptedException {
        assert splits != null;
        Map<Class<? extends Mapper<?, ?, ?, ?>>, List<Source>> groups = Util.groupByMapper(splits);
        List<StageInputSplit> results = new ArrayList<StageInputSplit>();
        for (Map.Entry<Class<? extends Mapper<?, ?, ?, ?>>, List<Source>> entry : groups.entrySet()) {
            Class<? extends Mapper<?, ?, ?, ?>> mapper = entry.getKey();
            List<Source> sources = entry.getValue();
            List<StageInputSplit> combined = combineSources(mapper, sources, max, populations, generations, mutations);
            results.addAll(combined);
        }
        return results;
    }

    private int getMaxSplitsPerMapper(JobContext context) {
        assert context != null;
        int max = context.getConfiguration().getInt(KEY_MAX, -1);
        if (max > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Combine split configuration: {0}={1}",
                        KEY_MAX,
                        max));
            }
            return max;
        }
        return Integer.MAX_VALUE;
    }

    private List<StageInputSplit> combineSources(
            Class<? extends Mapper<?, ?, ?, ?>> mapper,
            List<Source> sources,
            int max,
            int populations,
            int generations,
            double mutations) throws IOException, InterruptedException {
        assert sources != null;
        assert max > 0;
        if (sources.size() <= max) {
            List<StageInputSplit> results = new ArrayList<StageInputSplit>();
            for (Source source : sources) {
                results.add(new StageInputSplit(mapper, Collections.singletonList(source)));
            }
            return results;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Combining splits: {1} -> {2} ({0})",
                    mapper.getName(),
                    sources.size(),
                    max));
        }
        if (max == 1) {
            return Collections.singletonList(new StageInputSplit(mapper, sources));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start GA: {1} -> {2} ({0})",
                    mapper.getName(),
                    sources.size(),
                    max));
            LOG.debug(MessageFormat.format(
                    "GA parameters: schema-length={0}, populations={1}, generations={2}, mutation-ratio={3}",
                    max,
                    populations,
                    generations,
                    mutations));
        }
        Environment env = createEnvironment(max, populations, generations, mutations, sources);
        Gene gene = compute(env);
        List<StageInputSplit> results = resolve(env, gene, mapper);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish GA: {1} -> {2} ({0})",
                    mapper.getName(),
                    sources.size(),
                    results.size()));
        }
        return results;
    }

    private List<StageInputSplit> resolve(Environment env, Gene gene, Class<? extends Mapper<?, ?, ?, ?>> mapper) {
        List<List<SplitDef>> slots = new ArrayList<List<SplitDef>>();
        for (int i = 0, n = env.slots.length; i < n; i++) {
            slots.add(new ArrayList<SplitDef>());
        }
        int[] schema = gene.schema;
        for (int splitId = 0; splitId < schema.length; splitId++) {
            int slotId = schema[splitId];
            slots.get(slotId).add(env.splits[splitId]);
        }
        List<StageInputSplit> results = new ArrayList<StageInputSplit>();
        for (List<SplitDef> splits : slots) {
            if (splits.isEmpty() == false) {
                List<Source> sources = new ArrayList<Source>();
                for (SplitDef split : splits) {
                    sources.add(split.origin);
                }
                String[] locations = computeLocations(env, splits);
                results.add(new StageInputSplit(mapper, sources, locations));
            }
        }
        return results;
    }

    private String[] computeLocations(Environment env, List<SplitDef> splits) {
        String[] locationNames = env.locations;
        LocationAndTime[] pairs = new LocationAndTime[locationNames.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = new LocationAndTime(i, 0);
        }
        double totalLocalTime = 0.0;
        for (SplitDef split : splits) {
            BitSet locations = split.locations;
            totalLocalTime += split.localTime;
            for (int i = locations.nextSetBit(0); i >= 0; i = locations.nextSetBit(i + 1)) {
                pairs[i].time += split.localTime;
            }
        }
        Arrays.sort(pairs);
        double first = pairs[0].time;
        if (first == 0) {
            return null;
        }
        List<String> locations = new ArrayList<String>();
        locations.add(locationNames[pairs[0].location]);
        for (int i = 1; i < pairs.length; i++) {
            double totalScore = pairs[i].time / totalLocalTime;
            double comparisonScore = pairs[i].time / first;
            if (totalScore < LOCALITY_TOTAL_FACTOR) {
                break;
            }
            if (comparisonScore < LOCALITY_COMPARISON_FACTOR) {
                break;
            }
            locations.add(locationNames[pairs[i].location]);
        }
        return locations.toArray(new String[locations.size()]);
    }

    private Environment createEnvironment(
            int slots,
            int populations,
            int generations,
            double mutations,
            List<Source> sources) throws IOException, InterruptedException {
        assert sources != null;
        Map<String, Integer> locationIds = new HashMap<String, Integer>();
        List<SplitDef> results = new ArrayList<SplitDef>(sources.size());
        for (Source source : sources) {
            String[] locationArray = source.getSplit().getLocations();
            long length = source.getSplit().getLength();
            BitSet locations = new BitSet();
            if (locationArray != null) {
                for (String location : locationArray) {
                    Integer id = locationIds.get(location);
                    if (id == null) {
                        id = locationIds.size();
                        locationIds.put(location, id);
                    }
                    locations.set(id);
                }
            }
            double localScore = length * DEFAULT_LOCAL_SCORE_FACTOR;
            double globalScore = length * DEFAULT_GOBAL_SCORE_FACTOR;
            results.add(new SplitDef(source, locations, localScore, globalScore));
        }
        if (locationIds.isEmpty()) {
            locationIds.put("DUMMY-LOCATION", locationIds.size());
        }
        String[] locations = new String[locationIds.size()];
        for (Map.Entry<String, Integer> entry : locationIds.entrySet()) {
            locations[entry.getValue()] = entry.getKey();
        }
        SplitDef[] splitDefs = results.toArray(new SplitDef[results.size()]);
        SlotDef[] slotDefs = resolveSlots(slots, locations, results);
        return new Environment(locations, splitDefs, slotDefs, populations, generations, mutations);
    }

    private SlotDef[] resolveSlots(int slots, String[] locationNames, List<SplitDef> splits) {
        assert locationNames != null;
        assert locationNames.length >= 1;
        assert splits != null;
        double[] locationScores = new double[locationNames.length];
        for (SplitDef split : splits) {
            BitSet locations = split.locations;
            for (int i = locations.nextSetBit(0); i >= 0; i = locations.nextSetBit(i + 1)) {
                locationScores[i] += split.localTime;
            }
        }
        LocationAndTime[] pairs = new LocationAndTime[locationNames.length];
        for (int i = 0; i < pairs.length; i++) {
            LocationAndTime pair = new LocationAndTime(i, locationScores[i]);
            pairs[i] = pair;
        }
        Arrays.sort(pairs);

        SlotDef[] results = new SlotDef[slots];
        for (int i = 0; i < results.length; i++) {
            results[i] = new SlotDef(pairs[i % pairs.length].location);
        }
        return results;
    }

    private static Gene compute(Environment env) {
        assert env != null;
        Gene[] current = createGenes(env);
        Gene[] parent = createGenes(env);
        for (Gene gene : current) {
            initializeGene(env, gene);
        }

        int generations = env.generations;
        for (int iteration = 0; iteration < generations; iteration++) {
            // swap current to parent
            Gene[] hold = parent;
            parent = current;
            current = hold;

            // populate
            populate(env, parent, current);
        }
        return findBest(current);
    }

    private static Gene[] createGenes(Environment env) {
        assert env != null;
        Gene[] genes = new Gene[env.populations];
        for (int geneIndex = 0; geneIndex < genes.length; geneIndex++) {
            genes[geneIndex] = new Gene(env);
        }
        return genes;
    }

    private static void initializeGene(Environment env, Gene gene) {
        assert env != null;
        assert gene != null;
        Random random = env.random;
        int[] schema = gene.schema;
        for (int i = 0; i < schema.length; i++) {
            schema[i] = random.nextInt(env.slots.length);
        }
        gene.eval();
    }

    private static Gene findBest(Gene[] genes) {
        Gene best = genes[0];
        boolean changed = false;
        for (int i = 1; i < genes.length; i++) {
            if (genes[i].isBetterThan(best)) {
                best = genes[i];
                changed = true;
            }
        }
        if (changed && LOG.isTraceEnabled()) {
            LOG.trace(MessageFormat.format(
                    "Current best gene: {0}",
                    best.time));
        }
        return best;
    }

    private static void populate(Environment env, Gene[] parent, Gene[] next) {
        assert parent != null;
        assert next != null;
        assert env != null;

        int schemaLength = next[0].schema.length;

        // keep best gene
        Gene parentBest = findBest(parent);
        Gene nextBest = next[0];
        System.arraycopy(parentBest.schema, 0, nextBest.schema, 0, schemaLength);
        nextBest.time = parentBest.time;

        // sort by score
        Arrays.sort(parent, Gene.COMPARATOR);

        // crossover
        Random random = env.random;
        for (int i = 1; i < next.length; i++) {
            int limit = (next.length + i + 1) / 2;
            assert limit > 0;
            assert limit <= next.length;
            int p1 = random.nextInt(limit);
            int p2 = env.random.nextInt(limit);
            crossOver(env, parent[p1], parent[p2], next[i]);
        }

        // mutate and eval
        for (int i = 1; i < next.length; i++) {
            Gene gene = next[i];
            mutate(env, gene);
            gene.eval();
        }
    }

    private static void crossOver(Environment env, Gene parent1, Gene parent2, Gene child) {
        Random random = env.random;
        int schemaLength = parent1.schema.length;
        int point = random.nextInt(schemaLength - 2) + 1;
        System.arraycopy(parent1.schema, 0, child.schema, 0, point);
        System.arraycopy(parent2.schema, point, child.schema, point, schemaLength - point);
    }

    private static void mutate(Environment env, Gene gene) {
        Random random = env.random;
        int[] schema = gene.schema;
        double mutations = env.mutations;
        for (int i = 0; i < schema.length; i++) {
            if (random.nextDouble() < mutations) {
                schema[i] = random.nextInt(env.slots.length);
            }
        }
    }

    private static final class Environment {

        final Random random = new Random(1234567L);

        final String[] locations;

        final SplitDef[] splits;

        final SlotDef[] slots;

        final int populations;

        final int generations;

        final double mutations;

        Environment(
                String[] locations,
                SplitDef[] splits,
                SlotDef[] slots,
                int populations,
                int generations,
                double mutations) {
            assert locations != null;
            assert splits != null;
            assert slots != null;
            this.locations = locations;
            this.splits = splits;
            this.slots = slots;
            this.populations = populations;
            this.generations = generations;
            this.mutations = mutations;
        }
    }

    private static final class SlotDef {

        final int location;

        SlotDef(int location) {
            this.location = location;
        }
    }

    private static final class SplitDef {

        final Source origin;

        final BitSet locations;

        final double localTime;

        final double globalTime;

        SplitDef(Source origin, BitSet locations, double localScore, double globalScore) {
            assert origin != null;
            assert locations != null;
            this.origin = origin;
            this.locations = locations;
            this.localTime = localScore;
            this.globalTime = globalScore;
        }

        double eval(SlotDef slot) {
            return eval(slot.location);
        }

        double eval(int location) {
            if (locations.get(location)) {
                return localTime;
            } else {
                return globalTime;
            }
        }
    }

    private static final class LocationAndTime implements Comparable<LocationAndTime> {
        final int location;
        double time;
        LocationAndTime(int location, double score) {
            this.location = location;
            this.time = score;
        }
        @Override
        public int compareTo(LocationAndTime o) {
            double a = time;
            double b = o.time;
            if (a < b) {
                return +1;
            } else if (a > b) {
                return -1;
            } else {
                return 0;
            }
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + location;
            long temp;
            temp = Double.doubleToLongBits(time);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LocationAndTime other = (LocationAndTime) obj;
            if (location != other.location) {
                return false;
            }
            if (Double.doubleToLongBits(time) != Double.doubleToLongBits(other.time)) {
                return false;
            }
            return true;
        }
    }

    private static final class Gene {

        static final Comparator<Gene> COMPARATOR = GeneComparator.INSTANCE;

        final Environment environment;

        final int[] schema;

        double time;

        private final double[] slotScoreBuf;

        Gene(Environment env) {
            this.environment = env;
            this.schema = new int[env.splits.length];
            this.slotScoreBuf = new double[env.slots.length];
        }

        public boolean isBetterThan(Gene other) {
            return time < other.time;
        }

        public void eval() {
            Environment env = this.environment;
            double[] slotScores = slotScoreBuf;
            int[] splitSlots = schema;
            Arrays.fill(slotScores, 0);
            for (int splitId = 0; splitId < splitSlots.length; splitId++) {
                int slotId = splitSlots[splitId];
                SplitDef split = env.splits[splitId];
                SlotDef slot = env.slots[slotId];
                slotScores[slotId] += split.eval(slot);
            }
            double max = slotScores[0];
            for (int i = 1; i < slotScores.length; i++) {
                if (slotScores[i] > max) {
                    max = slotScores[i];
                }
            }
            this.time = max;
        }

        private enum GeneComparator implements Comparator<Gene> {

            INSTANCE,
            ;
            @Override
            public int compare(Gene o1, Gene o2) {
                double a = o1.time;
                double b = o2.time;
                if (a < b) {
                    return -1;
                } else if (a > b) {
                    return +1;
                } else {
                    return 0;
                }
            }
        }
    }
}
