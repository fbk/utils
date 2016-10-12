package eu.fbk.utils.core;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

public abstract class Graph<V, E> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Nullable
    private transient Set<E> labels;

    @Nullable
    private transient Set<V> neighbours;

    @Nullable
    private transient Set<V> sources;

    @Nullable
    private transient Set<V> targets;

    @Nullable
    private transient Set<V> roots;

    @Nullable
    private transient Set<V> leaves;

    abstract Set<V> doGetVertices();

    abstract Set<Edge<V, E>> doGetEdges();

    abstract Set<Edge<V, E>> doGetEdges(V vertex);

    abstract Graph<V, E> doFilter(@Nullable Predicate<V> vertexFilter,
            @Nullable Predicate<Edge<V, E>> edgeFilter);

    public final Set<E> getLabels() {
        if (this.labels == null) {
            final Set<E> labels = Sets.newHashSet();
            for (final Edge<V, E> edge : doGetEdges()) {
                labels.add(edge.getLabel());
            }
            this.labels = Collections.unmodifiableSet(labels);
        }
        return this.labels;
    }

    public final Set<E> getLabels(@Nullable final V vertex) {
        final Set<E> labels = Sets.newHashSet();
        for (final Edge<V, E> edge : doGetEdges(vertex)) {
            labels.add(edge.getLabel());
        }
        return Collections.unmodifiableSet(labels);
    }

    public final Set<E> getLabels(@Nullable final V source, @Nullable final V target) {

        Set<Edge<V, E>> edgesToFilter;
        if (source != null) {
            edgesToFilter = doGetEdges(source);
        } else if (target != null) {
            edgesToFilter = doGetEdges(target);
        } else {
            return getLabels();
        }

        final Set<E> labels = Sets.newHashSet();
        for (final Edge<V, E> edge : edgesToFilter) {
            if (source == null || edge.getSource().equals(source) || target == null
                    || edge.getTarget().equals(target)) {
                labels.add(edge.getLabel());
            }
        }
        return Collections.unmodifiableSet(labels);
    }

    public final Set<Edge<V, E>> getEdges() {
        return doGetEdges();
    }

    public final Set<Edge<V, E>> getEdges(@Nullable final V vertex) {
        return vertex == null ? doGetEdges() : doGetEdges(vertex);
    }

    public final Set<Edge<V, E>> getEdges(@Nullable final V source, @Nullable final V target) {

        Set<Edge<V, E>> edgesToFilter;
        if (source != null) {
            edgesToFilter = doGetEdges(source);
        } else if (target != null) {
            edgesToFilter = doGetEdges(target);
        } else {
            return doGetEdges();
        }

        final List<Edge<V, E>> edges = Lists.newArrayList();
        for (final Edge<V, E> edge : edgesToFilter) {
            if (source == null || edge.getSource().equals(source) || target == null
                    || edge.getTarget().equals(target)) {
                edges.add(edge);
            }
        }
        return ImmutableSet.copyOf(edges);
    }

    public final Set<V> getVertices() {
        return doGetVertices();
    }

    public final Set<V> getNeighbours() {
        if (this.neighbours == null) {
            final Set<V> neighbours = Sets.newHashSet();
            for (final Edge<V, E> edge : doGetEdges()) {
                neighbours.add(edge.getSource());
                neighbours.add(edge.getTarget());
            }
            this.neighbours = ImmutableSet.copyOf(neighbours);
        }
        return this.neighbours;
    }

    public final Set<V> getNeighbours(final V vertex) {
        final List<V> neighbours = Lists.newArrayList();
        for (final Edge<V, E> edge : doGetEdges(vertex)) {
            if (edge.getSource().equals(vertex)) {
                neighbours.add(edge.getTarget());
            } else {
                neighbours.add(edge.getSource());
            }
        }
        return ImmutableSet.copyOf(neighbours);
    }

    public final Set<V> getSources() {
        if (this.sources == null) {
            final Set<V> sources = Sets.newHashSet();
            for (final Edge<V, E> edge : doGetEdges()) {
                sources.add(edge.getSource());
            }
            this.sources = ImmutableSet.copyOf(sources);
        }
        return this.sources;
    }

    public final Set<V> getSources(final V vertex) {
        final List<V> sources = Lists.newArrayList();
        for (final Edge<V, E> edge : doGetEdges(vertex)) {
            if (edge.getTarget().equals(vertex)) {
                sources.add(edge.getSource());
            }
        }
        return ImmutableSet.copyOf(sources);
    }

    public final Set<V> getTargets() {
        if (this.targets == null) {
            final Set<V> targets = Sets.newHashSet();
            for (final Edge<V, E> edge : doGetEdges()) {
                targets.add(edge.getTarget());
            }
            this.targets = ImmutableSet.copyOf(targets);
        }
        return this.targets;
    }

    public final Set<V> getTargets(final V vertex) {
        final List<V> targets = Lists.newArrayList();
        for (final Edge<V, E> edge : doGetEdges(vertex)) {
            if (edge.getSource().equals(vertex)) {
                targets.add(edge.getTarget());
            }
        }
        return ImmutableSet.copyOf(targets);
    }

    public final Set<V> getRoots() {
        if (this.roots == null) {
            final Set<V> roots = Sets.newHashSet(doGetVertices());
            for (final Edge<V, E> edge : doGetEdges()) {
                roots.remove(edge.getTarget());
            }
            this.roots = ImmutableSet.copyOf(roots);
        }
        return this.roots;
    }

    public final Set<V> getLeaves() {
        if (this.leaves == null) {
            final Set<V> leaves = Sets.newHashSet(doGetVertices());
            for (final Edge<V, E> edge : doGetEdges()) {
                leaves.remove(edge.getSource());
            }
            this.leaves = ImmutableSet.copyOf(leaves);
        }
        return this.leaves;
    }

    public final Set<Path<V, E>> getPaths(final V source, final V target, final boolean directed,
            final int maxLength) {

        final Multimap<V, List<Edge<V, E>>> map = HashMultimap.create();
        map.put(source, Collections.emptyList());

        int length = 0;
        Set<V> frontier = ImmutableSet.of(source);
        while (!frontier.isEmpty() && map.get(target).isEmpty() && length < maxLength) {
            ++length;
            final Set<V> seen = ImmutableSet.copyOf(map.keySet());
            for (final V vertex : frontier) {
                final Collection<List<Edge<V, E>>> paths = map.get(vertex);
                final Set<Edge<V, E>> edges = directed ? getEdges(vertex, null) : getEdges(vertex);
                for (final Edge<V, E> edge : edges) {
                    final V otherVertex = edge.getOpposite(vertex);
                    if (!seen.contains(otherVertex)) {
                        for (final List<Edge<V, E>> path : paths) {
                            final List<Edge<V, E>> newPath = Lists.newArrayList(path);
                            newPath.add(edge);
                            map.put(otherVertex, newPath);
                        }
                    }
                }
            }
            frontier = ImmutableSet.copyOf(Sets.difference(map.keySet(), frontier));
        }

        final List<Path<V, E>> paths = Lists.newArrayList();
        for (final List<Edge<V, E>> path : map.get(target)) {
            paths.add(Path.create(source, target, path));
        }
        return ImmutableSet.copyOf(paths);
    }

    public final Graph<V, E> filterLabels(final Iterable<E> labels) {
        final Set<E> labelSet = labels instanceof Set<?> ? (Set<E>) labels : Sets
                .newHashSet(labels);
        return doFilter(null, new Predicate<Edge<V, E>>() {

            @Override
            public boolean apply(final Edge<V, E> edge) {
                return labelSet.contains(edge.getLabel());
            }

        });
    }

    public final Graph<V, E> filterEdges(final Iterable<Edge<V, E>> edges) {
        final Set<Edge<V, E>> edgeSet = edges instanceof Set<?> ? (Set<Edge<V, E>>) edges : Sets
                .newHashSet(edges);
        return doFilter(null, Predicates.in(edgeSet));
    }

    public final Graph<V, E> filterVertices(final Iterable<V> vertices) {
        final Set<V> vertexSet = vertices instanceof Set<?> ? (Set<V>) vertices : Sets
                .newHashSet(vertices);
        return doFilter(Predicates.in(vertexSet), new Predicate<Edge<V, E>>() {

            @Override
            public boolean apply(final Edge<V, E> edge) {
                return vertexSet.contains(edge.getSource())
                        && vertexSet.contains(edge.getTarget());
            }

        });
    }

    public final Graph<V, E> filter(@Nullable final Predicate<V> vertexFilter,
            @Nullable final Predicate<Edge<V, E>> edgeFilter) {

        if (vertexFilter == null) {
            return edgeFilter == null ? this : doFilter(null, edgeFilter);
        } else if (edgeFilter == null) {
            return doFilter(vertexFilter, new Predicate<Edge<V, E>>() {

                @Override
                public boolean apply(final Edge<V, E> edge) {
                    return vertexFilter.apply(edge.getSource())
                            && vertexFilter.apply(edge.getTarget());
                }

            });
        } else {
            return doFilter(vertexFilter, new Predicate<Edge<V, E>>() {

                @Override
                public boolean apply(final Edge<V, E> edge) {
                    return edgeFilter.apply(edge) && vertexFilter.apply(edge.getSource())
                            && vertexFilter.apply(edge.getTarget());
                }

            });
        }
    }

    @Override
    public final boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Graph<?, ?>)) {
            return false;
        }
        final Graph<?, ?> other = (Graph<?, ?>) object;
        return doGetVertices().equals(other.doGetVertices())
                && doGetEdges().equals(other.doGetEdges());
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(doGetVertices(), doGetEdges());
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("V(").append(doGetVertices().size()).append(") = {");
        Joiner.on(", ").appendTo(builder, doGetVertices());
        builder.append("}, E(").append(doGetEdges().size()).append(") = {");
        Joiner.on(", ").appendTo(builder, doGetEdges());
        builder.append("}");
        return builder.toString();
    }

    public static <V, E> Builder<V, E> builder() {
        return new Builder<V, E>();
    }

    public static final class Builder<V, E> {

        private final Set<V> vertices;

        private final Set<Edge<V, E>> edges;

        private Builder() {
            this.vertices = Sets.newHashSet();
            this.edges = Sets.newHashSet();
        }

        public Builder<V, E> addVertices(@SuppressWarnings("unchecked") final V... vertices) {
            return addVertices(Arrays.asList(vertices));
        }

        public Builder<V, E> addVertices(final Iterable<? extends V> vertices) {
            Iterables.addAll(this.vertices, vertices);
            return this;
        }

        public Builder<V, E> addEdges(@SuppressWarnings("unchecked") final Edge<V, E>... edges) {
            return addEdges(Arrays.asList(edges));
        }

        public Builder<V, E> addEdges(final Iterable<Edge<V, E>> edges) {
            for (final Edge<V, E> edge : edges) {
                this.edges.add(edge);
                this.vertices.add(edge.getSource());
                this.vertices.add(edge.getTarget());
            }
            return this;
        }

        public Builder<V, E> addEdges(final V source, final V target,
                @SuppressWarnings("unchecked") final E... labels) {
            this.vertices.add(source);
            this.vertices.add(target);
            for (final E label : labels) {
                this.edges.add(Edge.create(source, target, label));
            }
            return this;
        }

        public Graph<V, E> build() {
            return new ConcreteGraph<V, E>(ImmutableSet.copyOf(this.vertices),
                    ImmutableSet.copyOf(this.edges));
        }

    }

    public static final class Edge<V, E> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final V source;

        private final V target;

        @Nullable
        private final E label;

        private Edge(final V source, final V target, @Nullable final E label) {
            this.source = source;
            this.target = target;
            this.label = label;
        }

        public static <V, E> Edge<V, E> create(final V source, final V target,
                @Nullable final E label) {
            return new Edge<V, E>(Preconditions.checkNotNull(source),
                    Preconditions.checkNotNull(target), label);
        }

        public V getSource() {
            return this.source;
        }

        public V getTarget() {
            return this.target;
        }

        public V getOpposite(final V vertex) {
            if (this.source.equals(vertex)) {
                return this.target;
            } else if (this.target.equals(vertex)) {
                return this.source;
            } else {
                throw new IllegalArgumentException("Vertex " + vertex + " not contained in "
                        + this);
            }
        }

        @Nullable
        public E getLabel() {
            return this.label;
        }

        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof Edge)) {
                return false;
            }
            final Edge<?, ?> other = (Edge<?, ?>) object;
            return this.source.equals(other.source) && this.target.equals(other.target)
                    && Objects.equal(this.label, other.label);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.source, this.target, this.label);
        }

        @Override
        public String toString() {
            return this.source + " -" + this.label + "-> " + this.target;
        }

    }

    public static final class Path<V, E> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final List<Edge<V, E>> edges;

        private final List<V> vertices;

        @Nullable
        private transient List<E> labels;

        @Nullable
        private transient Boolean directed;

        private Path(final Iterable<Edge<V, E>> edges, final Iterable<V> vertices) {
            this.edges = ImmutableList.copyOf(edges);
            this.vertices = ImmutableList.copyOf(vertices);
        }

        public static <V, E> Path<V, E> create(final V source, final V target,
                final Iterable<Edge<V, E>> edges) {

            final List<Edge<V, E>> edgeList = ImmutableList.copyOf(edges);
            final List<V> verticesList = Lists.newArrayListWithCapacity(edgeList.size() + 1);
            V vertex = source;
            for (final Edge<V, E> edge : edgeList) {
                verticesList.add(vertex);
                if (edge.getSource().equals(vertex)) {
                    vertex = edge.getTarget();
                } else if (edge.getTarget().equals(vertex)) {
                    vertex = edge.getSource();
                } else {
                    throw new IllegalArgumentException("Invalid path");
                }
            }
            verticesList.add(vertex);
            if (!vertex.equals(target)) {
                throw new IllegalArgumentException("Invalid path");
            }
            return new Path<V, E>(edgeList, verticesList);
        }

        public int length() {
            return this.edges.size();
        }

        public V getSource() {
            return this.vertices.get(0);
        }

        public V getTarget() {
            return this.vertices.get(this.vertices.size() - 1);
        }

        public List<V> getVertices() {
            return this.vertices;
        }

        public List<Edge<V, E>> getEdges() {
            return this.edges;
        }

        public List<E> getLabels() {
            if (this.labels == null) {
                final List<E> labels = Lists.newArrayListWithCapacity(this.edges.size());
                for (final Edge<V, E> edge : this.edges) {
                    labels.add(edge.getLabel());
                }
                this.labels = ImmutableList.copyOf(labels);
            }
            return this.labels;
        }

        public boolean isDirected() {
            if (this.directed == null) {
                boolean directed = true;
                for (int i = 0; i < this.edges.size(); ++i) {
                    if (!this.edges.get(i).getSource().equals(this.vertices.get(i))) {
                        directed = false;
                        break;
                    }
                }
                this.directed = directed;
            }
            return this.directed;
        }

        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof Path)) {
                return false;
            }
            final Path<?, ?> path = (Path<?, ?>) object;
            return this.edges.equals(path.edges);
        }

        @Override
        public int hashCode() {
            return this.edges.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < this.edges.size(); ++i) {
                final V vertex = this.vertices.get(i);
                final Edge<V, E> edge = this.edges.get(i);
                builder.append(vertex);
                if (edge.getSource().equals(vertex)) {
                    builder.append(" -").append(edge.getLabel()).append("-> ");
                } else {
                    builder.append(" <-").append(edge.getLabel()).append("- ");
                }
            }
            builder.append(this.vertices.get(this.vertices.size() - 1));
            return builder.toString();
        }

    }

    private static final class ConcreteGraph<V, E> extends Graph<V, E> {

        private static final long serialVersionUID = 1L;

        private final Set<V> vertices;

        private final Set<Edge<V, E>> edges;

        @Nullable
        private transient Map<V, Set<Edge<V, E>>> map;

        ConcreteGraph(final Set<V> vertices, final Set<Edge<V, E>> edges) {
            this.vertices = vertices;
            this.edges = edges;
        }

        @Override
        Set<V> doGetVertices() {
            return this.vertices;
        }

        @Override
        Set<Edge<V, E>> doGetEdges() {
            return this.edges;
        }

        @Override
        Set<Edge<V, E>> doGetEdges(final V vertex) {
            if (this.map == null) {
                final Map<V, List<Edge<V, E>>> map = Maps.newHashMap();
                for (final Edge<V, E> edge : this.edges) {
                    List<Edge<V, E>> sourceEdges = map.get(edge.getSource());
                    List<Edge<V, E>> targetEdges = map.get(edge.getTarget());
                    if (sourceEdges == null) {
                        sourceEdges = Lists.newArrayList();
                        map.put(edge.getSource(), sourceEdges);
                    }
                    if (targetEdges == null) {
                        targetEdges = Lists.newArrayList();
                        map.put(edge.getTarget(), targetEdges);
                    }
                    sourceEdges.add(edge);
                    targetEdges.add(edge);
                }
                final ImmutableMap.Builder<V, Set<Edge<V, E>>> builder = ImmutableMap.builder();
                for (final Map.Entry<V, List<Edge<V, E>>> entry : map.entrySet()) {
                    builder.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
                }
                this.map = builder.build();
            }
            final Set<Edge<V, E>> edges = this.map.get(vertex);
            return edges == null ? ImmutableSet.of() : edges;
        }

        @Override
        Graph<V, E> doFilter(final Predicate<V> vertexFilter,
                final Predicate<Edge<V, E>> edgeFilter) {
            return new FilteredGraph<V, E>(this, vertexFilter, edgeFilter);
        }

    }

    private static final class FilteredGraph<V, E> extends Graph<V, E> {

        private static final long serialVersionUID = 1L;

        private final Graph<V, E> graph;

        @Nullable
        private final Predicate<V> vertexFilter;

        @Nullable
        private final Predicate<Edge<V, E>> edgeFilter;

        FilteredGraph(final Graph<V, E> graph, @Nullable final Predicate<V> vertexFilter,
                @Nullable final Predicate<Edge<V, E>> edgeFilter) {
            this.graph = graph;
            this.vertexFilter = vertexFilter;
            this.edgeFilter = edgeFilter;
        }

        @Override
        Set<Edge<V, E>> doGetEdges() {
            return this.edgeFilter == null ? this.graph.doGetEdges() : Sets.filter(
                    this.graph.doGetEdges(), this.edgeFilter);
        }

        @Override
        Set<Edge<V, E>> doGetEdges(final V vertex) {
            return this.edgeFilter == null ? this.graph.doGetEdges(vertex) : Sets.filter(
                    this.graph.doGetEdges(vertex), this.edgeFilter);
        }

        @Override
        Set<V> doGetVertices() {
            return this.vertexFilter == null ? this.graph.doGetVertices() : Sets.filter(
                    this.graph.doGetVertices(), this.vertexFilter);
        }

        @Override
        Graph<V, E> doFilter(@Nullable final Predicate<V> vertexFilter,
                @Nullable final Predicate<Edge<V, E>> edgeFilter) {

            final Predicate<V> newVertexFilter = this.vertexFilter == null ? vertexFilter
                    : vertexFilter == null ? this.vertexFilter : Predicates.and(this.vertexFilter,
                    vertexFilter);
            final Predicate<Edge<V, E>> newEdgeFilter = this.edgeFilter == null ? edgeFilter
                    : edgeFilter == null ? this.edgeFilter : Predicates.and(this.edgeFilter,
                    edgeFilter);
            return new FilteredGraph<V, E>(this.graph, newVertexFilter, newEdgeFilter);
        }

    }

}
