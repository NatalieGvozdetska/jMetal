package org.uma.jmetal.runner.singleobjective;

import org.apache.commons.lang3.math.NumberUtils;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.EvolutionStrategyBuilder;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.problem.singleobjective.TSP;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.impl.IntegerPermutationSolutionFromVariables;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.TimeOut;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EvolutionStrategyTSPRunner {

  private static HashMap<String, String> parseArguments(String[] args){
    HashMap<String, String> map = new HashMap<>();
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

  private static List<PermutationSolution<Integer>> parseSolutions(String variables, TSP problem) {
      String regexprPattern = "(\\[([0-9]*,? ?)*\\])";
      Pattern regexp = Pattern.compile(regexprPattern);

      List<PermutationSolution<Integer>> parsed_solutions = new ArrayList<>();
      Matcher matcher = regexp.matcher(variables);

      String stringArray;
      String[] stringInts;
      IntegerPermutationSolutionFromVariables parsed_solution = new IntegerPermutationSolutionFromVariables(problem);
      while (matcher.find()) {
          MatchResult result = matcher.toMatchResult();
          stringArray = result.group(1).replaceAll("[\\[\\] ]", "");
          stringInts = stringArray.split(",");

          // cheap copying
          parsed_solution = new IntegerPermutationSolutionFromVariables(parsed_solution);
          for (int i=0; i < problem.getPermutationLength(); i++){
              parsed_solution.setVariableValue(i, Integer.valueOf(stringInts[i]));
          }
          parsed_solutions.add(parsed_solution);

      }
      return parsed_solutions;
  }


  public static List<PermutationSolution<Integer>> runSolver (String[] args) throws IOException {
      HashMap<String, String> m_args = parseArguments(args);

      String tsp_instance = m_args.get("tsp_scenario");
      TSP problem;
      if (tsp_instance != null) {
          problem = new TSP(tsp_instance);
      }
      else {
          throw new IllegalArgumentException("TSP scenario file name missed! (add tsp_scenario=%FILE_NAME call argument)");
      }

      List<PermutationSolution<Integer>> init_solutions;
      init_solutions = parseSolutions(m_args.getOrDefault("initial_solutions", ""), problem);

      float mutation_probability = NumberUtils.toFloat(m_args.getOrDefault("mutation_probability", "0"));

      // since no other operators..
      MutationOperator<PermutationSolution<Integer>> mutationOperator = new PermutationSwapMutation<>(mutation_probability);

      EvolutionStrategyBuilder.EvolutionStrategyVariant variant;
      switch (m_args.getOrDefault("elitist", "True")) {
          case "False":
              variant = EvolutionStrategyBuilder.EvolutionStrategyVariant.NON_ELITIST;
              break;
          default: // case "True":
              variant = EvolutionStrategyBuilder.EvolutionStrategyVariant.ELITIST;
      }

      int maxEvaluations = NumberUtils.toInt(m_args.get("max_evaluations"), Integer.MAX_VALUE);
      int mu = NumberUtils.toInt(m_args.getOrDefault("mu", "1"));
      int lambda = NumberUtils.toInt(m_args.getOrDefault("lambda", "1"));
      long running_seconds = NumberUtils.toLong(m_args.getOrDefault("timeout_seconds", "5"));
      TimeOut timeout = new TimeOut(running_seconds, TimeUnit.SECONDS);

      EvolutionStrategyBuilder<PermutationSolution<Integer>> algorithm_builder;
      algorithm_builder = new EvolutionStrategyBuilder<>(problem, mutationOperator, variant);
      algorithm_builder.setMaxEvaluations(maxEvaluations);
      algorithm_builder.setMu(mu);
      algorithm_builder.setLambda(lambda);
      algorithm_builder.setTimeOut(timeout);
      algorithm_builder.setInitialSolutions(init_solutions);

      Algorithm<PermutationSolution<Integer>> algorithm = algorithm_builder.build();

      AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

      // reporting
      PermutationSolution<Integer> solution = algorithm.getResult();
      List<PermutationSolution<Integer>> resulting_solutions = ((AbstractEvolutionaryAlgorithm) algorithm).getPopulation();
      String ptc = m_args.get("paths_to_stdout") != null ? m_args.get("paths_to_stdout") : "No";

      StringBuilder report = new StringBuilder();
      report.append("Total execution time: ").append(algorithmRunner.getComputingTime()).append("ms\n");

      if (ptc.equalsIgnoreCase("true")) {
          report.append("Paths START\n");
          for (PermutationSolution sol: resulting_solutions){
              report.append(sol.getVariables()).append("\n");
          }
          report.append("Paths END\n");
      }

      double improvement = 0;
      double c_improvement;
      for (PermutationSolution<Integer> init_solution: init_solutions){
          //initial solutions are evaluated when the algorithm starts, since they form (part of) initial population
          c_improvement = (init_solution.getObjective(0) - solution.getObjective(0)) / solution.getObjective(0);
          if (c_improvement > improvement) improvement = c_improvement;
      }
      report.append("Path length:").append("\n").append(solution.getObjective(0)).append("\n");
      report.append("Improvement:").append("\n").append(improvement).append("\n");
      JMetalLogger.logger.info(report.toString());

      return resulting_solutions;
  }

  public static void main(String[] args) throws Exception {
      runSolver(args);
  }
}
