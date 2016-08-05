package eu.fbk.utils.eval;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import eu.fbk.utils.core.CommandLine;

public class RankingScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int maxN;

    private final int numRanking;

    private final int[] numRankings;

    private final double[] precisions;

    private final double mrr;

    private final double ndcg;

    private final double[] ndcgs;

    private final double altNdcg;

    private final double[] altNdcgs;

    private final double map;

    private final double[] maps;

    private RankingScore(final int maxN, final int numRanking, final int[] numRankings,
            final double[] precisions, final double mrr, final double ndcg, final double[] ndcgs,
            final double altNdcg, final double[] altNdcgs, final double map, final double[] maps) {
        this.maxN = maxN;
        this.numRanking = numRanking;
        this.numRankings = numRankings;
        this.precisions = precisions;
        this.mrr = mrr;
        this.ndcg = ndcg;
        this.ndcgs = ndcgs;
        this.altNdcg = altNdcg;
        this.altNdcgs = altNdcgs;
        this.map = map;
        this.maps = maps;
    }

    private void checkNumber(final int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Negative number");
        } else if (number > this.maxN) {
            throw new IllegalArgumentException(
                    "No data for N = " + number + " (Max N: " + this.maxN + ")");
        }
    }

    public static <T> RankingScore evaluate(final Iterable<T> ranking,
            final Iterable<T> relevantElements) {
        return evaluator(Iterables.size(ranking)).add(ranking, relevantElements).get();
    }

    public static <T> RankingScore evaluate(final Iterable<T> ranking,
            final Map<T, Double> relevances) {
        return evaluator(Iterables.size(ranking)).add(ranking, relevances).get();
    }

    public static RankingScore average(final Iterable<RankingScore> scores) {
        int maxN = Integer.MAX_VALUE;
        for (final RankingScore score : scores) {
            maxN = Math.min(maxN, score.maxN);
        }
        if (maxN == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("No measure supplied");
        }
        final Evaluator evaluator = evaluator(maxN);
        for (final RankingScore score : scores) {
            evaluator.add(score);
        }
        return evaluator.get();
    }

    public int getMaxN() {
        return this.maxN;
    }

    public int getNumRankings() {
        return this.numRanking;
    }

    public int getNumRankings(final int atNumber) {
        checkNumber(atNumber);
        return this.numRankings[atNumber - 1];
    }

    public double getPrecision(final int atNumber) {
        checkNumber(atNumber);
        return this.precisions[atNumber - 1];
    }

    public double getMRR() {
        return this.mrr;
    }

    public double getNDCG() {
        return this.ndcg;
    }

    public double getNDCG(final int atNumber) {
        checkNumber(atNumber);
        return this.ndcgs[atNumber - 1];
    }

    public double getAltNDCG() {
        return this.altNdcg;
    }

    public double getAltNDCG(final int atNumber) {
        checkNumber(atNumber);
        return this.altNdcgs[atNumber - 1];
    }

    public double getMAP() {
        return this.map;
    }

    public double getMAP(final int atNumber) {
        checkNumber(atNumber);
        return this.maps[atNumber - 1];
    }

    public double get(final Measure measure) {
        switch (measure.getType()) {
        case "p":
            return getPrecision(measure.getNumber());
        case "mrr":
            return getMRR();
        case "ndcg":
            return measure.getNumber() > 0 ? getNDCG(measure.getNumber()) : getNDCG();
        case "altndcg":
            return measure.getNumber() > 0 ? getAltNDCG(measure.getNumber()) : getAltNDCG();
        case "map":
            return measure.getNumber() > 0 ? getMAP(measure.getNumber()) : getMAP();
        default:
            throw new IllegalArgumentException("Invalid measure " + measure);
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof RankingScore)) {
            return false;
        }
        final RankingScore other = (RankingScore) object;
        return this.maxN == other.maxN && this.numRanking == other.numRanking
                && Arrays.equals(this.numRankings, other.numRankings)
                && Arrays.equals(this.precisions, other.precisions) && this.mrr == other.mrr
                && this.ndcg == other.ndcg && Arrays.equals(this.ndcgs, other.ndcgs)
                && this.map == other.map && Arrays.equals(this.maps, other.maps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.maxN, this.numRanking, Arrays.hashCode(this.numRankings),
                Arrays.hashCode(this.precisions), this.mrr, this.ndcg, Arrays.hashCode(this.ndcgs),
                this.map, Arrays.hashCode(this.maps));
    }

    @Override
    public String toString() {

        final int[] ns = new int[30];
        int count = 0;
        int n = 1;
        while (n <= this.maxN) {
            ns[count++] = n;
            n *= 3;
            if (n <= this.maxN) {
                ns[count++] = n;
                n = n / 3 * 5;
                if (n <= this.maxN) {
                    ns[count++] = n;
                    n = n * 2;
                }
            }
        }
        final int maxN = ns[count - 1];

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            n = ns[i];
            builder.append("p@").append(n).append("=")
                    .append(String.format("%.3f", getPrecision(n))).append(" ");
        }
        builder.append("mrr=").append(String.format("%.3f", getMRR())).append(" ");
        builder.append("ndcg=").append(String.format("%.3f", getNDCG())).append(" ");
        builder.append("ndcg@").append(maxN).append("=")
                .append(String.format("%.3f", getNDCG(maxN))).append(" ");
        builder.append("map=").append(String.format("%.3f", getMAP())).append(" ");
        builder.append("map@").append(maxN).append("=").append(String.format("%.3f", getMAP(n)))
                .append(" ");
        builder.append("nr=").append(getNumRankings());

        return builder.toString();
    }

    public static Ordering<RankingScore> comparator(final Measure measure,
            final boolean higherFirst) {
        return new Ordering<RankingScore>() {

            @Override
            public int compare(final RankingScore left, final RankingScore right) {
                final double leftValue = left.get(measure);
                final double rightValue = right.get(measure);
                final int result = Double.compare(leftValue, rightValue);
                return higherFirst ? -result : result;
            }

        };
    }

    public static Evaluator evaluator(final int maxN) {
        return new Evaluator(maxN);
    }

    public static void main(final String... args) {
        try {
            // Parse command line
            final CommandLine cmd = CommandLine.parser().withName("ranking-score")
                    .withOption("g", "gold", "specifies the gold relevances FILE", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("r", "ranking", "specifies the ranking FILE", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withHeader("Evaluates the ranking from a file against the gold relevances " //
                            + "in another file. File format: rank_id item1_id[:rel] ... " //
                            + "where rel is 1 if omitted")
                    .parse(args);

            // Parse gold relevances
            final Map<String, Map<String, Double>> rels = Maps.newHashMap();
            for (final String line : Files.readAllLines(cmd.getOptionValue("g", Path.class))) {
                final String[] tokens = line.split("[\\s+,;]+");
                final Map<String, Double> map = Maps.newHashMap();
                rels.put(tokens[0], map);
                for (int i = 1; i < tokens.length; ++i) {
                    final int j = tokens[i].lastIndexOf(':');
                    if (j < 0) {
                        map.put(tokens[i], 1.0);
                    } else {
                        map.put(tokens[i].substring(0, j),
                                Double.parseDouble(tokens[i].substring(j + 1)));
                    }
                }
            }

            // Process rankings
            System.out.println("# key\tp@1\tp@3\tp@5\tp@10\tmrr\tndcg\tndcg@10\tmap\tmap@10");
            final RankingScore.Evaluator evaluator = RankingScore.evaluator(10);
            for (final String line : Files.readAllLines(cmd.getOptionValue("r", Path.class))) {
                final String[] tokens = line.split("[\\s+,;]+");
                final String key = tokens[0];
                final List<String> ranking = Lists.newArrayList();
                for (int i = 1; i < tokens.length; ++i) {
                    final int j = tokens[i].lastIndexOf(':');
                    ranking.add(j < 0 ? tokens[i] : tokens[i].substring(0, i));
                }
                if (!rels.containsKey(key)) {
                    throw new CommandLine.Exception("No gold relevances for key " + key);
                }
                final RankingScore s = RankingScore.evaluator(10).add(ranking, rels.get(key))
                        .get();
                evaluator.add(s);
                System.out.println(key + "\t" + s.getPrecision(1) + "\t" + s.getPrecision(3) + "\t"
                        + s.getPrecision(5) + "\t" + s.getPrecision(10) + "\t" + s.getMRR() + "\t"
                        + s.getNDCG() + "\t" + s.getNDCG(10) + "\t" + s.getMAP() + "\t"
                        + s.getMAP(10));
            }
            final RankingScore s = evaluator.get();
            System.out.println("ALL\t" + s.getPrecision(1) + "\t" + s.getPrecision(3) + "\t"
                    + s.getPrecision(5) + "\t" + s.getPrecision(10) + "\t" + s.getMRR() + "\t"
                    + s.getNDCG() + "\t" + s.getNDCG(10) + "\t" + s.getMAP() + "\t"
                    + s.getMAP(10));

        } catch (final Throwable ex) {
            // Display error information and terminate
            CommandLine.fail(ex);
        }
    }

    public static final class Measure {

        public static final Measure P1 = new Measure("p", 1);

        public static final Measure P3 = new Measure("p", 3);

        public static final Measure P5 = new Measure("p", 5);

        public static final Measure P10 = new Measure("p", 10);

        public static final Measure MRR = new Measure("mrr", 0);

        public static final Measure NDCG = new Measure("ndcg", 0);

        public static final Measure NDCG10 = new Measure("ndcg", 10);

        public static final Measure ALTNDCG = new Measure("altndcg", 0);

        public static final Measure ALTNDCG10 = new Measure("altndcg", 10);

        public static final Measure MAP = new Measure("map", 0);

        public static final Measure MAP10 = new Measure("map", 10);

        private final String type;

        private final int number;

        private Measure(final String type, final int number) {
            this.type = type.intern();
            this.number = number;
        }

        public static Measure create(final String spec) {
            final int index = spec.indexOf('@');
            if (index > 0) {
                final String type = spec.substring(0, index).trim().toLowerCase().intern();
                final int number = Integer.parseInt(spec.substring(index + 1).trim());
                Preconditions.checkArgument(number > 0);
                Preconditions.checkArgument(
                        type == "p" || type == "ndcg" || type == "altndcg" || type == "map");
                return new Measure(type, number);
            } else {
                final String type = spec.trim().toLowerCase().intern();
                Preconditions.checkArgument(
                        type == "mrr" || type == "ndcg" || type == "altndcg" || type == "map");
                return new Measure(type, 0);
            }
        }

        public String getType() {
            return this.type;
        }

        @Nullable
        public int getNumber() {
            return this.number;
        }

        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof Measure)) {
                return false;
            }
            final Measure other = (Measure) object;
            return this.type == other.type && this.number == other.number;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.number);
        }

        @Override
        public String toString() {
            return this.type + (this.number > 0 ? "@" + this.number : "");
        }

    }

    public static final class Evaluator {

        private int maxN;

        private int numRanking;

        private int[] numRankings;

        private double[] sumPrecision;

        private double sumMRR;

        private double sumNDCG;

        private double[] sumNDCGs;

        private double sumAltNDCG;

        private double[] sumAltNDCGs;

        private double sumMAP;

        private double[] sumMAPs;

        private RankingScore result;

        private Evaluator(final int maxN) {
            this.maxN = maxN;
            this.numRanking = 0;
            this.numRankings = new int[maxN];
            this.sumPrecision = new double[maxN];
            this.sumMRR = 0.0;
            this.sumNDCG = 0.0;
            this.sumNDCGs = new double[maxN];
            this.sumAltNDCG = 0.0;
            this.sumAltNDCGs = new double[maxN];
            this.sumMAP = 0.0;
            this.sumMAPs = new double[maxN];
            this.result = null;
        }

        private void shrinkIfNeeded(final int maxN) {
            if (maxN < this.maxN) {
                this.maxN = maxN;
                this.numRankings = Arrays.copyOf(this.numRankings, maxN);
                this.sumPrecision = Arrays.copyOf(this.sumPrecision, maxN);
                this.sumNDCGs = Arrays.copyOf(this.sumNDCGs, maxN);
                this.sumAltNDCGs = Arrays.copyOf(this.sumAltNDCGs, maxN);
                this.sumMAPs = Arrays.copyOf(this.sumMAPs, maxN);
            }
        }

        /**
         * Internal method used to update the scores.
         *
         * @param ranking
         *            the ranking returned by the system, not null
         * @param relItems
         *            the set of gold relevant items, not null
         * @param rels
         *            the gold relevance scores, null if not available
         */
        private <T> void update(final Iterable<T> ranking, final Set<T> relItems,
                @Nullable final Map<T, Double> rels) {

            double[] relsSorted = null;
            if (rels != null) {
                relsSorted = new double[rels.size()];
                int i = 0;
                for (final Double rel : rels.values()) {
                    relsSorted[i++] = rel;
                }
                Arrays.sort(relsSorted);
            }

            int n = 0; // index of current item
            int c = 0; // num relevant items
            double mapNum = 0.0; // MAP numerator
            double ndcgNum = 0.0; // NDCG numerator
            double ndcgDen = 0.0; // NDCG denominator
            double altNdcgNum = 0.0; // NDCG numerator
            double altNdcgDen = 0.0; // NDCG denominator
            final double ln2 = Math.log(2.0); // constant for transforming log_e(n) to log_2(n)
            double pn = 0.0;

            synchronized (this) {

                ++this.numRanking;
                this.result = null; // invalidate cached result

                for (final T item : ranking) {
                    ++n;
                    final int r = relItems.contains(item) ? 1 : 0; // item relevant?
                    c += r;
                    final double f = n == 1 ? 1 : ln2 / Math.log(n); // factor used for NDCG
                    final double altF = ln2 / Math.log(n + 1); // factor used for alt NDCG
                    pn = (double) c / n; // precision @ n
                    mapNum += pn * r;

                    if (r == 1 && c == 1.0) {
                        this.sumMRR += 1.0 / n; // first relevant result at position n
                    }

                    if (r == 1) {
                        ndcgNum += (rels == null ? 1.0 : rels.get(item)) * f;
                        altNdcgNum += (rels == null ? 1.0 : Math.pow(2.0, //
                                rels.get(item)) - 1) * altF;
                    }
                    if (n <= relItems.size()) {
                        ndcgDen += (rels == null ? 1.0 : relsSorted[relsSorted.length - n]) * f;
                        altNdcgDen += (rels == null ? 1.0
                                : Math.pow(2.0, relsSorted[relsSorted.length - n]) - 1) * altF;
                    }

                    if (n <= this.maxN) {
                        ++this.numRankings[n - 1];
                        this.sumPrecision[n - 1] += pn;
                        this.sumNDCGs[n - 1] += ndcgNum / ndcgDen;
                        this.sumAltNDCGs[n - 1] += altNdcgNum / altNdcgDen;
                        if (!relItems.isEmpty()) {
                            // division by relItems.size() and not Math.min(n, relItems.size()) is
                            // justified by: http://www.msr-waypoint.net/pubs/130616/fp146-radlinski.pdf
                            this.sumMAPs[n - 1] += mapNum / relItems.size();
                        }
                    }
                }

                final int limit = Math.max(this.maxN, relItems.size());
                for (++n; n <= limit; ++n) {
                    if (n <= relItems.size()) {
                        final double f = n == 1 ? 1 : ln2 / Math.log(n); // factor used for NDCG
                        final double altF = ln2 / Math.log(n + 1); // factor used for alt NDCG
                        ndcgDen += (rels == null ? 1.0 : relsSorted[relsSorted.length - n]) * f;
                        altNdcgDen += (rels == null ? 1.0
                                : Math.pow(2.0, relsSorted[relsSorted.length - n]) - 1) * altF;
                    }
                    if (n <= this.maxN) {
                        this.sumNDCGs[n - 1] += ndcgNum / ndcgDen;
                        this.sumAltNDCGs[n - 1] += altNdcgNum / altNdcgDen;
                        if (!relItems.isEmpty()) {
                            this.sumMAPs[n - 1] += mapNum / relItems.size();
                        }
                    }
                }

                if (!relItems.isEmpty()) {
                    this.sumNDCG += ndcgNum / ndcgDen;
                    this.sumAltNDCG += altNdcgNum / altNdcgDen;
                    this.sumMAP += mapNum / relItems.size();
                }
            }
        }

        public <T> Evaluator add(final Iterable<T> ranking, final Iterable<T> relItems) {
            update(ranking, relItems instanceof Set<?> ? (Set<T>) relItems : //
                    ImmutableSet.copyOf(relItems), null);
            return this;
        }

        public <T> Evaluator add(final Iterable<T> ranking, final Map<T, Double> rels) {
            update(ranking, rels.keySet(), rels);
            return this;
        }

        public Evaluator add(final RankingScore score) {
            synchronized (this) {
                shrinkIfNeeded(score.maxN);
                this.numRanking += score.numRanking;
                this.sumMRR += score.mrr * score.numRanking;
                this.sumNDCG += score.ndcg * score.numRanking;
                this.sumAltNDCG += score.altNdcg * score.numRanking;
                this.sumMAP += score.map * score.numRanking;
                for (int i = 0; i < this.maxN; ++i) {
                    this.numRankings[i] += score.numRankings[i];
                    this.sumPrecision[i] += score.numRankings[i] == 0 ? 0
                            : score.precisions[i] * score.numRankings[i];
                    this.sumNDCGs[i] += score.ndcgs[i] * score.numRanking;
                    this.sumAltNDCGs[i] += score.altNdcgs[i] * score.numRanking;
                    this.sumMAPs[i] += score.maps[i] * score.numRanking;
                }
                this.result = null;
            }
            return this;
        }

        public Evaluator add(final Evaluator evaluator) {
            synchronized (evaluator) {
                synchronized (this) {
                    shrinkIfNeeded(evaluator.maxN);
                    this.numRanking += evaluator.numRanking;
                    this.sumMRR += evaluator.sumMRR;
                    this.sumNDCG += evaluator.sumNDCG;
                    this.sumAltNDCG += evaluator.sumAltNDCG;
                    this.sumMAP += evaluator.sumMAP;
                    for (int i = 0; i < this.maxN; ++i) {
                        this.numRankings[i] += evaluator.numRankings[i];
                        this.sumPrecision[i] += evaluator.sumPrecision[i];
                        this.sumNDCGs[i] += evaluator.sumNDCGs[i];
                        this.sumAltNDCGs[i] += evaluator.sumAltNDCGs[i];
                        this.sumMAPs[i] += evaluator.sumMAPs[i];
                    }
                    this.result = null;
                }
            }
            return this;
        }

        public RankingScore get() {
            synchronized (this) {
                if (this.result == null) {
                    final double factor = this.numRanking == 0 ? 0.0 : 1.0 / this.numRanking;
                    final double mrr = this.sumMRR * factor;
                    final double ndcg = this.sumNDCG * factor;
                    final double altNdcg = this.sumAltNDCG * factor;
                    final double map = this.sumMAP * factor;
                    final double[] precisions = new double[this.maxN];
                    final double[] ndcgs = new double[this.maxN];
                    final double[] altNdcgs = new double[this.maxN];
                    final double[] maps = new double[this.maxN];
                    for (int i = 0; i < this.maxN; ++i) {
                        precisions[i] = this.numRankings[i] == 0 ? Double.NaN
                                : this.sumPrecision[i] / this.numRankings[i];
                        ndcgs[i] = this.sumNDCGs[i] * factor;
                        altNdcgs[i] = this.sumAltNDCGs[i] * factor;
                        maps[i] = this.sumMAPs[i] * factor;
                    }
                    this.result = new RankingScore(this.maxN, this.numRanking,
                            this.numRankings.clone(), precisions, mrr, ndcg, ndcgs, altNdcg,
                            altNdcgs, map, maps);
                }
                return this.result;
            }
        }

    }

}
