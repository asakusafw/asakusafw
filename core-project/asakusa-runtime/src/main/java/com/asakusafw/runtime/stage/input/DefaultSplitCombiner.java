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
 * @version 0.7.0
 */
public class DefaultSplitCombiner extends SplitCombiner {

    static final Log LOG = LogFactory.getLog(DefaultSplitCombiner.class);

    static final String KEY_SLOTS_PER_INPUT = "com.asakusafw.input.combine.max"; //$NON-NLS-1$

    static final String KEY_GENERATIONS = "com.asakusafw.input.combine.ga.generation"; //$NON-NLS-1$

    static final String KEY_POPULATIONS = "com.asakusafw.input.combine.ga.population"; //$NON-NLS-1$

    static final String KEY_MUTATION_RATIO = "com.asakusafw.input.combine.ga.mutation"; //$NON-NLS-1$

    /**
     * The configuration key of the limit input size (in bytes) for
     * combining all splits for a mapper into a single split.
     * @since 0.6.2
     */
    static final String KEY_TINY_LIMIT = "com.asakusafw.input.combine.tiny.limit"; //$NON-NLS-1$

    /**
     * The configuration key of weight for average time comparing to the worst time penalty.
     * @since 0.7.0
     */
    static final String KEY_AVERAGE_TIME_WEIGHT = "com.asakusafw.input.combine.ga.averageTimeWeight"; //$NON-NLS-1$

    /**
     * The configuration key of penalty ratio for non-local data accesses (should be {@code > 1.0}).
     * @since 0.7.0
     */
    static final String KEY_NON_LOCAL_PENALTY_RATIO = "com.asakusafw.input.combine.ga.nonLocalPenalty"; //$NON-NLS-1$

    /**
     * The configuration key of initial locality ratio (should be {@code [0, 1]}).
     * @since 0.7.0
     */
    static final String KEY_INITIAL_LOCALITY_RATIO = "com.asakusafw.input.combine.ga.initialLoacality"; //$NON-NLS-1$

    static final int DEFAULT_SLOTS_PER_INPUT = Integer.MAX_VALUE;

    static final int DEFAULT_POPULATIONS = 50;

    static final int DEFAULT_GENERATIONS = 100;

    static final float DEFAULT_MUTATION_RATIO = 0.001f;

    static final long DEFAULT_TINY_LIMIT = -1L;

    static final float DEFAULT_AVERAGE_TIME_WEIGHT = 1.0f;

    static final float DEFAULT_NON_LOCAL_PENALTY_RATIO = 2.0f;

    static final float DEFAULT_INITIAL_LOCALITY_RATIO = 0.8f;

    static final int MIN_POPULATIONS = 10;

    static final int MIN_GENERATIONS = 5;

    static final double MIN_MUTATION_RATIO = 0;

    static final double LOCALITY_TOTAL_FACTOR = 0.5;

    static final double LOCALITY_COMPARISON_FACTOR = 0.8;

    @Override
    protected List<StageInputSplit> combine(
            JobContext context,
            List<StageInputSplit> splits) throws IOException, InterruptedException {
        Configuration conf = new Configuration();
        conf.withSlotsPerInput(getMaxSplitsPerMapper(context));
        conf.withPopulations(context.getConfiguration().getInt(KEY_POPULATIONS, DEFAULT_POPULATIONS));
        conf.withGenerations(context.getConfiguration().getInt(KEY_GENERATIONS, DEFAULT_GENERATIONS));
        conf.withMutations(context.getConfiguration().getFloat(KEY_MUTATION_RATIO, DEFAULT_MUTATION_RATIO));
        conf.withTinyLimit(context.getConfiguration().getLong(KEY_TINY_LIMIT, DEFAULT_TINY_LIMIT));
        conf.withAverageTimeWeight(context.getConfiguration().getFloat(
                KEY_AVERAGE_TIME_WEIGHT,
                DEFAULT_AVERAGE_TIME_WEIGHT));
        conf.withNonLocalPenaltyRatio(context.getConfiguration().getFloat(
                KEY_NON_LOCAL_PENALTY_RATIO,
                DEFAULT_NON_LOCAL_PENALTY_RATIO));
        conf.withInitialLocalityRatio(context.getConfiguration().getFloat(
                KEY_INITIAL_LOCALITY_RATIO,
                DEFAULT_INITIAL_LOCALITY_RATIO));
        return combine(conf, splits);
    }

    List<StageInputSplit> combine(
            Configuration configuration,
            List<StageInputSplit> splits) throws IOException, InterruptedException {
        assert splits != null;
        Map<Class<? extends Mapper<?, ?, ?, ?>>, List<Source>> groups = Util.groupByMapper(splits);
        List<StageInputSplit> results = new ArrayList<StageInputSplit>();
        for (Map.Entry<Class<? extends Mapper<?, ?, ?, ?>>, List<Source>> entry : groups.entrySet()) {
            Class<? extends Mapper<?, ?, ?, ?>> mapper = entry.getKey();
            List<Source> sources = entry.getValue();
            List<StageInputSplit> combined = combineSources(mapper, sources, configuration);
            results.addAll(combined);
        }
        return results;
    }

    private int getMaxSplitsPerMapper(JobContext context) {
        assert context != null;
        int max = context.getConfiguration().getInt(KEY_SLOTS_PER_INPUT, -1);
        if (max > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Combine split configuration: {0}={1}", //$NON-NLS-1$
                        KEY_SLOTS_PER_INPUT,
                        max));
            }
            return max;
        }
        return DEFAULT_SLOTS_PER_INPUT;
    }

    private List<StageInputSplit> combineSources(
            Class<? extends Mapper<?, ?, ?, ?>> mapper,
            List<Source> sources,
            Configuration configuration) throws IOException, InterruptedException {
        assert sources != null;
        assert configuration.slotsPerInput > 0;
        if (isTinyInput(sources, configuration.tinyLimit)) {
            LOG.debug(MessageFormat.format(
                    "Mapper inputs are tiny: < {2} bytes in {1} input(s) ({0})", //$NON-NLS-1$
                    mapper.getName(),
                    sources.size(),
                    configuration.tinyLimit));
            return Collections.singletonList(new StageInputSplit(mapper, sources));
        }
        if (sources.size() <= configuration.slotsPerInput) {
            List<StageInputSplit> results = new ArrayList<StageInputSplit>();
            for (Source source : sources) {
                results.add(new StageInputSplit(mapper, Collections.singletonList(source)));
            }
            return results;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Combining splits: {1} -> {2} ({0})", //$NON-NLS-1$
                    mapper.getName(),
                    sources.size(),
                    configuration.slotsPerInput));
        }
        if (configuration.slotsPerInput == 1) {
            return Collections.singletonList(new StageInputSplit(mapper, sources));
        }
        long begin = 0;
        if (LOG.isDebugEnabled()) {
            begin = System.currentTimeMillis();
            LOG.debug(MessageFormat.format(
                    "Start GA: {1}splits -> {2}slots (mapper={0})", //$NON-NLS-1$
                    mapper.getName(),
                    sources.size(),
                    configuration.slotsPerInput));
            LOG.debug(MessageFormat.format(
                    "GA parameters: {0}", //$NON-NLS-1$
                    configuration.getGaParametersString()));
        }
        Environment env = createEnvironment(configuration, sources);
        Gene gene = compute(env);
        List<StageInputSplit> results = resolve(env, gene, mapper);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish GA: {1}splits -> {2}slots (elapsed={4}ms, locality={3}, mapper={0})", //$NON-NLS-1$
                    mapper.getName(),
                    sources.size(),
                    results.size(),
                    gene.getLocality(),
                    System.currentTimeMillis() - begin));
        }
        return results;
    }

    private boolean isTinyInput(List<Source> sources, long limit) throws IOException, InterruptedException {
        if (limit < 0L || sources.size() <= 1) {
            return false;
        }
        long total = 0L;
        for (Source source : sources) {
            total += source.getSplit().getLength();
            if (total > limit) {
                return false;
            }
        }
        return true;
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
            Configuration configuration,
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
            double localScore = length;
            double globalScore = length * configuration.nonLocalPenaltyRatio;
            results.add(new SplitDef(source, locations, localScore, globalScore));
        }
        if (locationIds.isEmpty()) {
            locationIds.put("DUMMY-LOCATION", locationIds.size()); //$NON-NLS-1$
        }
        String[] locations = new String[locationIds.size()];
        for (Map.Entry<String, Integer> entry : locationIds.entrySet()) {
            locations[entry.getValue()] = entry.getKey();
        }
        SplitDef[] splitDefs = results.toArray(new SplitDef[results.size()]);
        SlotDef[] slotDefs = resolveSlots(configuration.slotsPerInput, locations, results);
        return new Environment(locations, splitDefs, slotDefs, configuration);
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
            LocationAndTime pair = pairs[i % pairs.length];
            SlotDef slot = new SlotDef(pair.location, locationNames[pair.location]);
            results[i] = slot;
            if (LOG.isTraceEnabled()) {
                LOG.trace(MessageFormat.format(
                        "Slot[{0}]: {1}", //$NON-NLS-1$
                        i, slot));
            }
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

        if (LOG.isTraceEnabled()) {
            for (int i = 0; i < current.length; i++) {
                LOG.trace(MessageFormat.format(
                        "Gene[{0}]: {1}", //$NON-NLS-1$
                        i, current[i]));
            }
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
            if (random.nextDouble() < env.locality) {
                schema[i] = env.getRandomLocalSlot(env.splits[i]);
            } else {
                schema[i] = env.getRandomSlot();
            }
            assert 0 <= schema[i] && schema[i] < env.slots.length;
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
                    "Current best gene: {0}", //$NON-NLS-1$
                    best));
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
        nextBest.score = parentBest.score;

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

    /**
     * Configuration for {@link DefaultSplitCombiner}.
     * @since 0.7.0
     */
    public static final class Configuration {

        int slotsPerInput = DEFAULT_SLOTS_PER_INPUT;

        int populations = DEFAULT_POPULATIONS;

        int generations = DEFAULT_GENERATIONS;

        double mutations = DEFAULT_MUTATION_RATIO;

        long tinyLimit = DEFAULT_TINY_LIMIT;

        double averageTimeWeight = DEFAULT_AVERAGE_TIME_WEIGHT;

        double nonLocalPenaltyRatio = DEFAULT_NON_LOCAL_PENALTY_RATIO;

        double initialLocalityRatio = DEFAULT_INITIAL_LOCALITY_RATIO;

        /**
         * Sets the allocated slots per each input (mapper).
         * @param value the value
         * @return this
         */
        public Configuration withSlotsPerInput(int value) {
            this.slotsPerInput = Math.max(value, 1);
            return this;
        }

        /**
         * Sets the GA populations.
         * @param value the value
         * @return this
         */
        public Configuration withPopulations(int value) {
            this.populations = Math.max(value, MIN_POPULATIONS);
            return this;
        }

        /**
         * Sets the GA generations.
         * @param value the value
         * @return this
         */
        public Configuration withGenerations(int value) {
            this.generations = Math.max(value, MIN_GENERATIONS);
            return this;
        }

        /**
         * Sets the GA mutation ratio.
         * @param value the value
         * @return this
         */
        public Configuration withMutations(double value) {
            this.mutations = Math.max(value, MIN_MUTATION_RATIO);
            return this;
        }

        /**
         * Sets the limit input size (in bytes) for combining all splits for a mapper into a single split.
         * @param value the value, or {@code -1} to disable
         * @return this
         */
        public Configuration withTinyLimit(long value) {
            this.tinyLimit = value;
            return this;
        }

        /**
         * Sets the initial locality ratio (should be {@code > 1.0}).
         * @param value the value
         * @return this
         */
        public Configuration withInitialLocalityRatio(double value) {
            this.initialLocalityRatio = value;
            return this;
        }

        /**
         * Sets the average time weight to the worst time penalty for the gene score.
         * @param value the value
         * @return this
         */
        public Configuration withAverageTimeWeight(double value) {
            this.averageTimeWeight = value;
            return this;
        }

        /**
         * Sets the penalty ratio for non-local data accesses (should be {@code > 1.0}).
         * @param value the value
         * @return this
         */
        public Configuration withNonLocalPenaltyRatio(double value) {
            this.nonLocalPenaltyRatio = Math.max(value, 1.01);
            return this;
        }

        /**
         * Returns the GA parameters as string.
         * @return the GA parameters
         */
        String getGaParametersString() {
            return MessageFormat.format(
                    "schema-base={0}, populations={1}, generations={2}, mutation-ratio={3}, " //$NON-NLS-1$
                    + "initial-locality={4}, non-local-penalty={5}, average-time-weight={6}", //$NON-NLS-1$
                    slotsPerInput,
                    populations,
                    generations,
                    mutations,
                    initialLocalityRatio,
                    nonLocalPenaltyRatio,
                    averageTimeWeight);
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

        final double averageTimeWeight;

        final double locality;

        Environment(
                String[] locations,
                SplitDef[] splits,
                SlotDef[] slots,
                Configuration configuration) {
            assert locations != null;
            assert splits != null;
            assert slots != null;
            this.locations = locations;
            this.splits = splits;
            this.slots = slots;
            this.populations = configuration.populations;
            this.generations = configuration.generations;
            this.mutations = configuration.mutations;
            this.locality = configuration.initialLocalityRatio;
            this.averageTimeWeight = configuration.averageTimeWeight;
        }

        public int getRandomSlot() {
            return random.nextInt(slots.length);
        }

        public int getRandomLocalSlot(SplitDef split) {
            BitSet localSlots = computeLocalSlots(split);
            int cardinality = localSlots.cardinality();
            if (cardinality == 0) {
                return getRandomSlot();
            }
            int nth = random.nextInt(cardinality);
            int current = localSlots.nextSetBit(0);
            for (int i = 0; i < nth; i++) {
                assert current >= 0;
                assert current < slots.length;
                current = localSlots.nextSetBit(current + 1);
            }
            assert current >= 0;
            assert current < slots.length;
            return current;
        }

        private BitSet computeLocalSlots(SplitDef split) {
            if (split.locations.cardinality() == 0) {
                return new BitSet();
            }
            BitSet bits = new BitSet();
            SlotDef[] ss = slots;
            for (int i = 0; i < ss.length; i++) {
                if (split.isLocal(ss[i])) {
                    bits.set(i);
                }
            }
            return bits;
        }
    }

    private static final class SlotDef {

        final int location;

        final String symbol;

        SlotDef(int location, String symbol) {
            this.location = location;
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0}[{1}]", //$NON-NLS-1$
                    symbol, location);
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

        boolean isLocal(SlotDef slot) {
            return locations.get(slot.location);
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

        double bestTime;

        double worstTime;

        double averageTime;

        double score;

        private final double[] slotScoreBuf;

        Gene(Environment env) {
            this.environment = env;
            this.schema = new int[env.splits.length];
            this.slotScoreBuf = new double[env.slots.length];
        }

        public boolean isBetterThan(Gene other) {
            return score < other.score;
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
            double best = slotScores[0];
            double worst = slotScores[0];
            double total = slotScores[0];
            for (int i = 1; i < slotScores.length; i++) {
                double slotScore = slotScores[i];
                total += slotScore;
                if (slotScore < best) {
                    best = slotScore;
                }
                if (slotScore > worst) {
                    worst = slotScore;
                }
            }
            double average = total / slotScores.length;
            this.bestTime = best;
            this.worstTime = worst;
            this.averageTime = average;
            this.score = worst + average * env.averageTimeWeight;
        }

        public double getLocality() {
            double totalTime = 0;
            double localTime = 0;
            for (int splitId = 0; splitId < schema.length; splitId++) {
                int slotId = schema[splitId];
                SplitDef split = environment.splits[splitId];
                SlotDef slot = environment.slots[slotId];
                totalTime += split.localTime;
                if (split.isLocal(slot)) {
                    localTime += split.localTime;
                }
            }
            return localTime / totalTime;
        }

        @Override
        public String toString() {
            eval();
            return MessageFormat.format(
                    "best-time={0}, worst-time={1}, average-time={2}, locality={3}", //$NON-NLS-1$
                    Math.round(bestTime),
                    Math.round(worstTime),
                    Math.round(averageTime),
                    getLocality());
        }

        private enum GeneComparator implements Comparator<Gene> {

            INSTANCE,
            ;
            @Override
            public int compare(Gene o1, Gene o2) {
                double a = o1.score;
                double b = o2.score;
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
