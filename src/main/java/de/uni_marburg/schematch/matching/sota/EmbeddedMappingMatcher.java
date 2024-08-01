package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.similarity.list.EuclideanDistance;
import de.uni_marburg.schematch.similarity.list.ProbabilityMassFunction;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Uninterpreted Schema Matching with Embedded Value Mapping under Opaque Column Names and Data Values
 */
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedMappingMatcher extends Matcher {

    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {
        return match(matchTask, matchStep, Integer.MAX_VALUE);
    }

    public float[][] match(MatchTask matchTask, MatchingStep matchStep, int maxIterations) {
        // what the pseudo code looks like:
        float[][] simMatrix;
        var scenario = matchTask.getScenario();
        Database t1 = scenario.getSourceDatabase();
        Database t2 = scenario.getTargetDatabase();
        // TODO: Alles
//        var sMatchScore = Float.POSITIVE_INFINITY;
//        var sSpace = getSchemaMatchSearchSpace();
//        while (!sSpace.isEmpty()) {
//            var tmpS = getNextSchemaMatch(sSpace);
//            var vSpace = getValueMappingSpace();
//            var vMapScore = Float.POSITIVE_INFINITY;
//            var v1;
//            while (!vSpace.isEmpty()) {
//                var tmpV = getNextValueMapping(vSpace);
//                float score = computeDissimilarity(tmpS, tmpV, t1, t2);
//                if (score < vMapScore) {
//                    vMapScore = score;
//                    v1 = tmpV;
//                }
//                vSpace.remove(tmpV);
//            }
//            if (vMapScore < sMatchScore) {
//                s = tmpS;
//                v = v1;
//                sMatchScore = vMapScore;
//            }
//            sSpace.remove(tmpS);
//        }

        // how I interpret 2.4:
        double[][] similarityByColumn = new double[t1.getNumColumns()][t2.getNumColumns()];
        ProbabilityMassFunction<String> similarityMeasure = new EuclideanDistance<>();
        for (int i = 0; i < similarityByColumn.length; i++) {
            for (int j = 0; j < similarityByColumn[i].length; j++) {
                similarityByColumn[i][j] = similarityMeasure.compare(t1.getColumnByIndex(i).getValues(), t2.getColumnByIndex(j).getValues());
            }
        }
        int maxColumns = Integer.min(similarityByColumn.length, similarityByColumn[0].length);
        boolean[][] initialMatch = new boolean[maxColumns][maxColumns];
        IntStream.range(0, maxColumns).forEach(i -> initialMatch[i][i] = true);
        boolean[][] bestMatch = initialMatch;
        double disBestMatch = lookUpDissimilarity(similarityByColumn, bestMatch);
        //now to some two-opt switching
        int currentIteration = 0;
//        boolean furtherImprovement = true;
//        while (furtherImprovement && currentIteration <= maxIterations) {
//            furtherImprovement = false;
        for (var outer = 0; outer < maxColumns; outer++) {
            for (var inner = outer; inner < maxColumns; inner++) {
                currentIteration++;
                if (currentIteration > maxIterations) break;
                if (bestMatch[outer][inner]) continue;

                boolean[][] newMatch = twoOptSwitch(bestMatch, maxColumns, inner, outer);

                double disNewMatch = lookUpDissimilarity(similarityByColumn, newMatch);
                if (disNewMatch < disBestMatch) {
                    bestMatch = newMatch;
                    disBestMatch = disNewMatch;
//                    furtherImprovement = true;
                }
            }
        }
//        }

        float[][] finalResultAsPrimitiveFloat = new float[t1.getNumColumns()][t2.getNumColumns()];
        for (int i = 0; i < finalResultAsPrimitiveFloat.length; i++) {
            for (int j = 0; j < finalResultAsPrimitiveFloat[i].length; j++) {
                finalResultAsPrimitiveFloat[i][j] = bestMatch[i][j] ? 1.0f : 0.0f;
            }
        }
        return finalResultAsPrimitiveFloat;
    }

    // 1 0 x    0 0 1      1 x 0    0 1 0
    // 0 1 0 -> 0 1 0  or  0 1 0 -> 1 0 0  or  ...
    // 0 0 1    1 0 0      0 0 1    0 0 1
    private static boolean[][] twoOptSwitch(boolean[][] bestMatch, int maxColumns, int inner, int outer) {
        boolean[][] newMatch = bestMatch.clone();
        boolean[] lookUpHelp = new boolean[maxColumns];
        lookUpHelp[inner] = true;
        int oldOuter = IntStream.range(0, maxColumns).filter(index -> Arrays.equals(bestMatch[index], lookUpHelp)).findFirst().orElseThrow();
        int oldInner = ArrayUtils.indexOf(bestMatch[outer], true);
        newMatch[outer][inner] = true;
        newMatch[oldOuter][inner] = false;
        newMatch[outer][oldInner] = false;
        newMatch[oldOuter][oldInner] = true;
        return newMatch;
    }

    private double lookUpDissimilarity(double[][] simMatrix, boolean[][] match) {
        return IntStream.range(0, simMatrix.length).mapToDouble(sourceColumn ->
                        IntStream.range(0, simMatrix[sourceColumn].length).mapToDouble(targetColumn ->
                                        match[sourceColumn][targetColumn] ? simMatrix[sourceColumn][targetColumn] : 0)
                                .sum())
                .sum();
    }

//    private float computeDissimilarity(something[] tmpS, something[] tmpV, Database t1, Database t2) {
//        for (int i = 1; i < tmpS.length; i++) {
//            for (int i1 = 1; i1 < tmpV.length; i1++) {
//                float y = 0;
//                for (int j = 1; j < tmpS[i].length; j++) {
//                    float x = 0;
//                    for (int j1 = 0; j1 < tmpS[i].length) {
//                        x += delta(j - valueMapping(i, i1, j1) * pi1(j1);
//                    }
//                    y += p(i, j) * x;
//                }
//                delta(i - matchedAttributeFunction(i1)) *
//            }
//        }
//    }
}
