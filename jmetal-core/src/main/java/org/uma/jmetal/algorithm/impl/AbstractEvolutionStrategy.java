package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing an evolution strategy algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

@SuppressWarnings("serial")
public abstract class AbstractEvolutionStrategy<S, Result> extends AbstractEvolutionaryAlgorithm<S, Result> {
  protected MutationOperator<S> mutationOperator ;
  protected int mu;
  protected int lambda;
  protected int maxEvaluations;
  protected int evaluations;
  private List<S> initialSolutions;

  /* Getter */
  public MutationOperator<S> getMutationOperator() {
    return mutationOperator;
  }

  /**
   * Constructor
   * @param problem The problem to solve
   */
  public AbstractEvolutionStrategy(Problem<S> problem) {
    setProblem(problem);
    initialSolutions = new ArrayList<>();
  }

  @Override protected List<S> createInitialPopulation() {
    List<S> population = new ArrayList<>(mu);
    for (int i = 0; i < mu; i++) {
      S newIndividual;
      if (initialSolutions.size() >= i + 1) newIndividual = initialSolutions.get(i);
      else newIndividual = getProblem().createSolution();
      population.add(newIndividual);
    }

    return population;
  }

  @Override protected boolean isStoppingConditionReached() {
    return evaluations >= maxEvaluations | timeOut.isTimeElapsed();
  }

  public void setInitialSolutions(List<S> initialSolutions) {
    this.initialSolutions = initialSolutions;
  }

}
