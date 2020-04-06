package org.uma.jmetal.runner.singleobjective;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.PMXCrossover;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.PermutationProblem;
import org.uma.jmetal.problem.singleobjective.TSP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.TimeOut;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class to configure and run a generational genetic algorithm. The target problem is TSP.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerationalGeneticAlgorithmTSPRunner {
  /**
   * Usage: java org.uma.jmetal.runner.singleobjective.BinaryGenerationalGeneticAlgorithmRunner
   */
  public static void main(String[] args) throws Exception {

      List<String> pr_instances = Arrays.asList(
//              "kroA100.tsp",
              "eil101.tsp"
//              , "pla7397.tsp"
//              , "mona-lisa100K.tsp"
      );

      for (int i = 20; i <= 20; i++) {

          for (String pr_inst: pr_instances) {
              

    PermutationProblem<PermutationSolution<Integer>> problem;
    Algorithm<PermutationSolution<Integer>> algorithm;
    CrossoverOperator<PermutationSolution<Integer>> crossover;
    MutationOperator<PermutationSolution<Integer>> mutation;
    SelectionOperator<List<PermutationSolution<Integer>>, PermutationSolution<Integer>> selection;

    problem = new TSP("/tspInstances/" + pr_inst);

    crossover = new PMXCrossover(0.9) ;

//    double mutationProbability = 1.0 / problem.getNumberOfVariables() ;
    double mutationProbability = 0.5 ;
    mutation = new PermutationSwapMutation<Integer>(mutationProbability) ;

    selection = new BinaryTournamentSelection<PermutationSolution<Integer>>(new RankingAndCrowdingDistanceComparator<PermutationSolution<Integer>>());

    algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
            .setPopulationSize(100)
            .setMaxEvaluations(950000000)
            .setTimeOut(new TimeOut(1, TimeUnit.SECONDS))
            .setSelectionOperator(selection)
            .build() ;

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
            .execute() ;

    PermutationSolution<Integer> solution = algorithm.getResult() ;
    List<PermutationSolution<Integer>> population = new ArrayList<>(1) ;
    population.add(solution) ;

    long computingTime = algorithmRunner.getComputingTime() ;

    new SolutionListOutput(population)
            .setSeparator("\t")
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR" + pr_inst + "_" + i + ".tsv"))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN" + pr_inst + "_" + i + ".tsv"))
            .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
    JMetalLogger.logger.info("Objective: " + solution.getObjective(0));
      }
      }

  }
}
