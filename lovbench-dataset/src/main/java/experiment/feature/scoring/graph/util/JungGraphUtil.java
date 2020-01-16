package experiment.feature.scoring.graph.util;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import experiment.model.Ontology;
import experiment.model.Term;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Helper class to work with Jung graphs.
 *
 */
public class JungGraphUtil {

    private static final Logger log = LoggerFactory.getLogger( JungGraphUtil.class );

//    /**
//     * Creates a Jung graph from BindingSet list.
//     *
//     * @param queryResult
//     * @return
//     */
//    public static Graph<Ontology,String> createOntologyRepositoryGraph(List<BindingSet> queryResult) {
//        Graph<Ontology,String> graph = new DirectedSparseGraph();
//        if (queryResult != null) {
//            int i = 0;
//
//            for (BindingSet importStatement : queryResult) {
//                String fromOntologyIRI = importStatement.getValue("importingOntology").stringValue();
//                String toOntologyIRI = importStatement.getValue("importedOntology").stringValue();
//                Ontology fromOntology = new Ontology(fromOntologyIRI);
//                Ontology toOntology = new Ontology(toOntologyIRI);
//
//                log.debug(String.format("Adding edge: %s => %s", fromOntology, toOntology));
//
//                if (!graph.containsVertex(fromOntology)) {
//                    graph.addVertex(fromOntology);
//                }
//                if (!graph.containsVertex(toOntology)) {
//                    graph.addVertex(toOntology);
//                }
//                graph.addEdge(fromOntologyIRI + "_imports_" + toOntologyIRI, fromOntology, toOntology, EdgeType.DIRECTED);
//                i++;
//            }
//            log.debug(i + " edges added to graph.");
//        }
//        return graph;
//    }


    /**
     * Creates an ontology repository graph based on given "node - node" pairs with generic edges, e.g. based on imports.
     *
     * @param vertices
     * @param edgeType
     * @return
     */
    public static Graph<Ontology,String> createRepositoryGraph(Set<Pair<Ontology,Ontology>> vertices, EdgeType edgeType) {
        Graph<Ontology,String> graph = new SparseGraph();

        if (vertices != null && !vertices.isEmpty()) {
            int i = 0;

            for (Pair<Ontology,Ontology> vertice : vertices) {
                Ontology fromVertice = vertice.getLeft();
                Ontology toVertice = vertice.getRight();
                log.debug(String.format("Adding edge: %s => %s", fromVertice, toVertice));

                if (!graph.containsVertex(fromVertice)) {
                    graph.addVertex(fromVertice);
                }
                if (!graph.containsVertex(toVertice)) {
                    graph.addVertex(toVertice);
                }
                graph.addEdge(fromVertice + "_imports_" + toVertice, fromVertice, toVertice, edgeType);
                i++;
            }
            log.debug(i + " edges added to graph.");
        }
        return graph;
    }

    /**
     * Creates an ontology graph from a given list of "node - edge - node" triples.
     *
     * @param triples
     * @param edgeType
     * @return
     */
    public static Graph<Term,String> createOntologyGraph(List<Triple<Term, Term, Term>> triples, EdgeType edgeType) {
        Graph<Term,String> graph = new SparseMultigraph<>();

        if (triples != null && !triples.isEmpty()) {
            for (Triple<Term, Term, Term> triple : triples) {
                Term subject = triple.getLeft();
                Term predicate = triple.getMiddle();
                Term object = triple.getRight();
                if (!graph.containsVertex(subject)) {
                    graph.addVertex(subject);
                }
                if (!graph.containsVertex(object)) {
                    graph.addVertex(object);
                }
                graph.addEdge(predicate + "::" + subject + "::" + object, subject, object, edgeType);
            }
        }
        return graph;
    }
}
