package experiment.feature.scoring.graph.util;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import experiment.model.Ontology;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.openrdf.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Helper class to work with JGraphT graphs.
 *
 * @deprecated
 */
public class JGraphTUtil {

    private static final Logger log = LoggerFactory.getLogger( JGraphTUtil.class );

    /**
     * Creates a JGraphT graph from BindingSet list.
     *
     * @param queryResult
     * @return Graph<Ontology,DefaultEdge>
     */
    public static Graph<Ontology,DefaultEdge> createOntologyGraph(List<BindingSet> queryResult) {
        Graph<Ontology,DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        int i = 0;

        for (BindingSet importStatement : queryResult) {
            String fromOntologyIRI = importStatement.getValue("importingOntology").stringValue();
            String toOntologyIRI = importStatement.getValue("importedOntology").stringValue();

            Ontology fromOntology = new Ontology(fromOntologyIRI);
            Ontology toOntology = new Ontology(toOntologyIRI);

            if (!graph.containsVertex(fromOntology)) {
                graph.addVertex(fromOntology);
            }
            if (!graph.containsVertex(toOntology)) {
                graph.addVertex(toOntology);
            }
            graph.addEdge(fromOntology, toOntology);
            i++;
        }
        log.debug(i + " edges added to graph.");
        return graph;
    }

    /**
     * Visualises a JGraphT graph
     * @param graph
     */
    public static void visualiseGraph(Graph<Ontology,DefaultEdge> graph) {
        JGraphXAdapter<Ontology, DefaultEdge> jgxAdapter = new JGraphXAdapter<Ontology, DefaultEdge>(graph);
        Dimension DEFAULT_SIZE = new Dimension(530, 320);
        JApplet applet = new JApplet();

        applet.setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        applet.getContentPane().add(component);
        applet.resize(DEFAULT_SIZE);
        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);

        // center the circle
        int radius = 100;
        layout.setX0((DEFAULT_SIZE.width / 2.0) - radius);
        layout.setY0((DEFAULT_SIZE.height / 2.0) - radius);
        layout.setRadius(radius);
        layout.setMoveCircle(true);

        layout.execute(jgxAdapter.getDefaultParent());

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("Graph Visualisation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
