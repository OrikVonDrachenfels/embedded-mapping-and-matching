package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.DBGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.Metadata2Graph;
import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.SQL2Graph;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.Flooder;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderA;
import de.uni_marburg.schematch.boosting.sf_algorithm.flooding.FlooderC;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationNode;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.WaterWeightingGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.similarity.string.Levenshtein;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Similarity Flooding Matrix Boosting
 */
public class SimFloodingSimMatrixBoosting implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(SimFloodingSimMatrixBoosting.class);

    @Override
    public float[][] run(int line, MatchTask matchTask, TablePair tablePair, Matcher matcher) {
        // Create a DatabaseGraph
        DBGraph dbGraphSource = new SQL2Graph(line, matchTask, tablePair, matcher, true);
        DBGraph dbGraphTarget = new SQL2Graph(line, matchTask, tablePair, matcher, false);

        // Create Similaritycalculator
        SimilarityCalculator levenshteinCalculator = new SimilarityCalculator(line, matchTask, tablePair, matcher) {
            final SimilarityMeasure<String> stringMeasure = new Levenshtein();
            @Override
            public float calcStringSim(String stringA, String stringB){
                return stringMeasure.compare(stringA, stringB);
            }
        };

        // Create PropagationGraph
        PropagationGraph<PropagationNode> pGraph = new WaterWeightingGraph(dbGraphSource, dbGraphTarget, levenshteinCalculator);

        // Create Flooder
        Flooder flooder = new FlooderC(pGraph);

        float[][] boostedMatrix = flooder.flood(100, 0.0000001F);

        float[][] simMatrix = switch (line) {
            case 1 -> tablePair.getResultsForFirstLineMatcher(matcher);
            case 2 -> tablePair.getResultsForSecondLineMatcher(matcher);
            default -> throw new RuntimeException("Illegal matcher line set for similarity matrix boosting");
        };
        
        return boostedMatrix;
    }

    private void printMatrix(float[][] sim){
        for(float[] row : sim){
            for(float a : row){
                System.out.print(a+" ");
            }
            System.out.print("\n");
        }
    }
}