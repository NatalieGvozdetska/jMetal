package org.uma.jmetal.algorithm.singleobjective.evolutionstrategy;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionaryAlgorithm;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.TimeOut;

import java.util.concurrent.TimeUnit;

/**
 * Class implementing a (mu , lambda) Evolution Strategy (lambda must be divisible by mu)
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class EvolutionStrategyBuilder<S extends Solution<?>> implements AlgorithmBuilder<Algorithm<S>> {
  public enum EvolutionStrategyVariant {ELITIST, NON_ELITIST}

  private Problem<S> problem;
  private int mu;
  private int lambda;
  private int maxEvaluations;
  private MutationOperator<S> mutation;
  private EvolutionStrategyVariant variant ;
  private TimeOut timeOut;

  public EvolutionStrategyBuilder(Problem<S> problem, MutationOperator<S> mutationOperator,
      EvolutionStrategyVariant variant) {
    this.problem = problem;
    this.mu = 1;
    this.lambda = 10;
    this.maxEvaluations = 250000;
    this.mutation = mutationOperator;
    this.variant = variant ;
    this.timeOut = new TimeOut(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
  }

  public EvolutionStrategyBuilder<S> setMu(int mu) {
    this.mu = mu;

    return this;
  }

  public EvolutionStrategyBuilder<S> setLambda(int lambda) {
    this.lambda = lambda;

    return this;
  }

  public EvolutionStrategyBuilder<S> setMaxEvaluations(int maxEvaluations) {
    this.maxEvaluations = maxEvaluations;

    return this;
  }

  public EvolutionStrategyBuilder<S> setTimeOut(TimeOut timeOut){
    this.timeOut = timeOut;

    return this;
  }

  @Override public Algorithm<S> build() {
    AbstractEvolutionaryAlgorithm algorithm;
    if (variant == EvolutionStrategyVariant.ELITIST) {
      algorithm = new ElitistEvolutionStrategy<S>(problem, mu, lambda, maxEvaluations, mutation);
    } else if (variant == EvolutionStrategyVariant.NON_ELITIST) {
      algorithm = new NonElitistEvolutionStrategy<S>(problem, mu, lambda, maxEvaluations, mutation);
    } else {
      throw new JMetalException("Unknown variant: " + variant) ;
    }
    algorithm.setTimeOut(timeOut);
    return algorithm;
  }

  /* Getters */
  public int getMu() {
    return mu;
  }

  public int getLambda() {
    return lambda;
  }

  public int getMaxEvaluations() {
    return maxEvaluations;
  }

  public MutationOperator<S> getMutation() {
    return mutation;
  }
}
