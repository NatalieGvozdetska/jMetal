package org.uma.jmetal.runner.singleobjective;

import org.apache.commons.lang3.math.NumberUtils;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.EvolutionStrategyBuilder;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.problem.singleobjective.TSP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.TimeOut;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EvolutionStrategyTSPRunner {

  private static HashMap parseArguments(String[] args){
    HashMap map = new HashMap<String, String>();
    StringBuilder args_as_str = new StringBuilder();
    for (String arg : args) {
      if (arg.contains("=")) {
        //works only if the key doesn't have any '='
        map.put(arg.substring(0, arg.indexOf('=')),
                arg.substring(arg.indexOf('=') + 1));
        args_as_str.append(arg);
        args_as_str.append("\t");

      }
    }

    if (args.length != map.size())
          JMetalLogger.logger.warning("Some args were specified not as key-values. " +
                  "Length of args: " + args.length + "Length of parsed args: " + map.size());
    JMetalLogger.logger.info("Call arguments: " + args_as_str.toString());
    return map;
  }

  public static void main(String[] args) throws Exception {

    HashMap m_args = parseArguments(args);

    TSP problem = new TSP((String) m_args.get("tsp_scenario"));

    float mutation_probability = NumberUtils.toFloat((String) m_args.get("mutation_probability"), 0);

    // since no other operators..
    MutationOperator<PermutationSolution<Integer>> mutationOperator = new PermutationSwapMutation<>(mutation_probability);

    EvolutionStrategyBuilder.EvolutionStrategyVariant variant;
    switch ((String) m_args.get("elitist")) {
      case "False":
        variant = EvolutionStrategyBuilder.EvolutionStrategyVariant.NON_ELITIST;
        break;
      default:
        variant = EvolutionStrategyBuilder.EvolutionStrategyVariant.ELITIST;
    }

    int maxEvaluations = NumberUtils.toInt((String) m_args.get("max_evaluations"), Integer.MAX_VALUE);
    int mu = NumberUtils.toInt((String) m_args.get("mu"), 1);
    int lambda = NumberUtils.toInt((String) m_args.get("lambda_"), 1);
    TimeOut timeout = new TimeOut(NumberUtils.toLong((String) m_args.get("timeout_seconds"), 5), TimeUnit.SECONDS);

    EvolutionStrategyBuilder<PermutationSolution<Integer>> algorithm_builder;
    algorithm_builder = new EvolutionStrategyBuilder<PermutationSolution<Integer>>(problem, mutationOperator, variant);
    algorithm_builder.setMaxEvaluations(maxEvaluations);
    algorithm_builder.setMu(mu);
    algorithm_builder.setLambda(lambda);
    algorithm_builder.setTimeOut(timeout);

    Algorithm<PermutationSolution<Integer>> algorithm = algorithm_builder.build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute() ;

    PermutationSolution<Integer> solution = algorithm.getResult() ;
    List<PermutationSolution<Integer>> population = new ArrayList<>(1) ;
    population.add(solution) ;

    long computingTime = algorithmRunner.getComputingTime() ;

//    new SolutionListOutput(population)
//        .setSeparator("\t")
//        .setVarFileOutputContext(new DefaultFileOutputContext("EESVAR.tsv"))
//        .setFunFileOutputContext(new DefaultFileOutputContext("EESFUN.tsv"))
//        .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values: " + solution.getObjective(0));
//    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
//    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
  }
}
