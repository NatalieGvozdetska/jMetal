package org.uma.jmetal.runner.singleobjective;

import org.apache.commons.lang3.math.NumberUtils;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.ElitistEvolutionStrategy;
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.EvolutionStrategyBuilder;
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.NonElitistEvolutionStrategy;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.operator.impl.mutation.SimpleRandomMutation;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.singleobjective.Rastrigin;
import org.uma.jmetal.problem.singleobjective.Sphere;
import org.uma.jmetal.problem.singleobjective.TSP;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.impl.DoubleSolutionFromVariables;
import org.uma.jmetal.solution.impl.IntegerPermutationSolutionFromVariables;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.TimeOut;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EvolutionStrategyTSPRunner {

  public static String problemType = "";

    private static HashMap<String, String> parseArguments(String[] args){
    HashMap<String, String> map = new HashMap<>();
    StringBuilder args_as_str = new StringBuilder();
    for (String arg : args) {
      if (arg.contains("=")) {
        //works only if the key doesn't have any '='
        map.put(arg.substring(0, arg.indexOf('=')),
                arg.substring(arg.indexOf('=') + 1));
        args_as_str.append(arg).append("\t");
      }
    }

    if (args.length != map.size())
          JMetalLogger.logger.warning("Some args were specified not as key-values. " +
                  "Length of args: " + args.length + "Length of parsed args: " + map.size());
    JMetalLogger.logger.info("Call arguments: " + args_as_str.toString());
    return map;
  }

  private static List<PermutationSolution<Integer>> readPermutationSolutions(String solutionsFilePath, TSP problem) {
    List<PermutationSolution<Integer>> read_solutions = new ArrayList<>();
    try {
      BufferedReader buffReader = new BufferedReader(new FileReader(new File(solutionsFilePath)));
      // create random solution to use the copy interface
      IntegerPermutationSolutionFromVariables parsed_solution = new IntegerPermutationSolutionFromVariables(problem);

      String path;
      String[] stringInts;
      while ((path = buffReader.readLine()) != null){
        stringInts = path.replaceAll("[\\[\\] ]", "").split(",");
        for (int i=0; i < problem.getPermutationLength(); i++){
            parsed_solution.setVariableValue(i, Integer.valueOf(stringInts[i]));
        }
        read_solutions.add(parsed_solution);
        // spawn new solution object
        parsed_solution = new IntegerPermutationSolutionFromVariables(parsed_solution);

      }
    } catch (Exception exc) {
      JMetalLogger.logger.warning("Solving from scratch, since unable to load provided Solutions: " + exc.toString());
    }
    return read_solutions;
    }

    private static List<DoubleSolution> readDoubleSolutions(String solutionsFilePath, DoubleProblem problem) {
        List<DoubleSolution> read_solutions = new ArrayList<>();
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(new File(solutionsFilePath)));
            // create random solution to use the copy interface
            DoubleSolutionFromVariables parsed_solution = new DoubleSolutionFromVariables(problem);

            String solution;
            String[] stringInts;
            while ((solution = buffReader.readLine()) != null){
                stringInts = solution.replaceAll("[\\[\\] ]", "").split(",");
                for (int i=0; i < problem.getNumberOfVariables(); i++){
                    parsed_solution.setVariableValue(i, Double.valueOf(stringInts[i]));
                }
                read_solutions.add(parsed_solution);
                // spawn new solution object
                parsed_solution = new DoubleSolutionFromVariables(parsed_solution);

            }
        } catch (Exception exc) {
            JMetalLogger.logger.warning("Solving from scratch, since unable to load provided Solutions: " + exc.toString());
        }
        return read_solutions;
    }

  public static List<Solution> runSolver (String[] args) throws IOException {
      HashMap<String, String> m_args = parseArguments(args);
      List<Solution> resulting_solutions = null;
      // Algorithm's parameters
      float mutation_probability = NumberUtils.toFloat(m_args.getOrDefault("mutation_probability", "0"));

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

      // Problem init
      problemType = m_args.get("problem");

      Problem problem;
      List<Solution> init_solutions = new ArrayList<>();

      if (problemType.contains("TSP")) {
          String tsp_instance = m_args.get("tsp_scenario");
          TSP tspProblem;
          if (tsp_instance != null) {
              tspProblem = new TSP(tsp_instance);
          } else {
              throw new IllegalArgumentException("TSP scenario file name missed! (add tsp_scenario=%FILE_NAME call argument)");
          }

          List<PermutationSolution<Integer>> init_solutions_tsp;

          init_solutions_tsp = readPermutationSolutions(m_args.getOrDefault("initial_solutions", ""), tspProblem);
          problem = tspProblem;
          for (Solution s : init_solutions_tsp){
              init_solutions.add(s);
          }

          // since no other operators..
          MutationOperator<PermutationSolution<Integer>> mutationOperator = new PermutationSwapMutation<>(mutation_probability);

          EvolutionStrategyBuilder<PermutationSolution<Integer>> algorithm_builder;
          algorithm_builder = new EvolutionStrategyBuilder<>(problem, mutationOperator, variant);
          algorithm_builder.setMaxEvaluations(maxEvaluations);
          algorithm_builder.setMu(mu);
          algorithm_builder.setLambda(lambda);
          algorithm_builder.setTimeOut(timeout);
          if(m_args.get("isWarmStartupEnabled").equalsIgnoreCase("True")) {
              algorithm_builder.setInitialSolutions(init_solutions_tsp);
          }
          else{
              algorithm_builder.setInitialSolutions(new ArrayList<>());
          }

          Algorithm<PermutationSolution<Integer>> algorithm = algorithm_builder.build();
          AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
          resulting_solutions = report(algorithm, algorithmRunner, init_solutions, m_args);
      }
      else{
          Integer numberOfVariables = Integer.valueOf(m_args.get("numberOfVariables"));
          DoubleProblem doubleProblem;
          if (problemType.contains("Rastrigin")) {
              doubleProblem = new Rastrigin(numberOfVariables);
          }
          else if (problemType.contains("Sphere")) {
              doubleProblem = new Sphere(numberOfVariables);
          }
          else {
              throw new IllegalArgumentException("Problem parameters missed!");
          }

          List<DoubleSolution> init_solutions_double;

          init_solutions_double = readDoubleSolutions(m_args.getOrDefault("initial_solutions", ""), doubleProblem);
          problem = doubleProblem;
          for (Solution s : init_solutions_double){
              init_solutions.add(s);
          }

          // since no other operators..
          MutationOperator<DoubleSolution> mutationOperator = new SimpleRandomMutation(mutation_probability);

          EvolutionStrategyBuilder<DoubleSolution> algorithm_builder;
          algorithm_builder = new EvolutionStrategyBuilder<>(problem, mutationOperator, variant);
          algorithm_builder.setMaxEvaluations(maxEvaluations);
          algorithm_builder.setMu(mu);
          algorithm_builder.setLambda(lambda);
          algorithm_builder.setTimeOut(timeout);
          if(m_args.get("isWarmStartupEnabled").equalsIgnoreCase("True")) {
              algorithm_builder.setInitialSolutions(init_solutions_double);
          }
          else{
              algorithm_builder.setInitialSolutions(new ArrayList<>());
          }

          Algorithm<DoubleSolution> algorithm = algorithm_builder.build();
          AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
          resulting_solutions = report(algorithm, algorithmRunner, init_solutions, m_args);
      }

      return resulting_solutions;
  }

  public static List<Solution> report(Algorithm algorithm, AlgorithmRunner algorithmRunner, List<Solution> init_solutions, HashMap<String, String> m_args){
      // reporting
      Solution solution = (Solution) algorithm.getResult();
      List<Solution> resulting_solutions = ((AbstractEvolutionaryAlgorithm) algorithm).getPopulation();
      String ptc = m_args.get("paths_to_stdout") != null ? m_args.get("paths_to_stdout") : "No";

      StringBuilder report = new StringBuilder();
      report.append("Total execution time: ").append(algorithmRunner.getComputingTime()).append("ms\n");

      if (ptc.equalsIgnoreCase("true")) {
          report.append("Solutions START\n");
          for (Solution sol: resulting_solutions){
              report.append(sol.getVariables()).append("\n");
          }
          report.append("Solutions END\n");
      }

      double improvement = 0;
      double c_improvement;
      // workaround to get evaluations for initial population, if it was not used within the warm startup
      try {
          ((ElitistEvolutionStrategy)algorithm).evaluatePopulation(init_solutions);
      }
      catch (Exception e){
          ((NonElitistEvolutionStrategy)algorithm).evaluatePopulation(init_solutions);
      }
      for (Solution init_solution: init_solutions){
          //initial solutions are evaluated when the algorithm starts, since they form (part of) initial population
          c_improvement = (init_solution.getObjective(0) - solution.getObjective(0)) / init_solution.getObjective(0);
          if (c_improvement > improvement) improvement = c_improvement;
      }

      report.append("Objective:").append("\n").append(String.format("%f", solution.getObjective(0))).append("\n");
      report.append("Improvement:").append("\n").append(String.format("%f", improvement)).append("\n");
      JMetalLogger.logger.info(report.toString());
      return resulting_solutions;
  }

  public static void main(String[] args) throws Exception {
      runSolver(args);
  }
}
