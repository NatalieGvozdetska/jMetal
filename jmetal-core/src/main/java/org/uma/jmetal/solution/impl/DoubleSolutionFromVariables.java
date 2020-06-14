package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.PermutationProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoubleSolutionFromVariables
        extends AbstractGenericSolution<Double, DoubleProblem>
        implements DoubleSolution {

    /** Constructor */
    public DoubleSolutionFromVariables(DoubleProblem problem, List<Double> variables) {
        super(problem) ;
        for (int i = 0; i < getNumberOfVariables(); i++) {
            setVariableValue(i, variables.get(i)) ;
        }
    }

    /** Copy Constructor */
    public DoubleSolutionFromVariables(DoubleSolutionFromVariables solution) {
        super(solution.problem) ;
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            setObjective(i, solution.getObjective(i)) ;
        }

        for (int i = 0; i < problem.getNumberOfVariables(); i++) {
            setVariableValue(i, solution.getVariableValue(i));
        }

        attributes = new HashMap<Object, Object>(solution.attributes) ;
    }

    /**
     * Empty Constructor
     *
     * @param problem
     */
    public DoubleSolutionFromVariables(DoubleProblem problem) {
        super(problem);
    }

    @Override
    public Double getUpperBound(int index) {
        return problem.getUpperBound(index);
    }

    @Override
    public Double getLowerBound(int index) {
        return problem.getLowerBound(index) ;
    }

    @Override
    public String getVariableValueString(int index) {
        return getVariableValue(index).toString() ;
    }

    @Override
    public Solution<Double> copy() {
        return new DoubleSolutionFromVariables(this);
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return attributes;
    }
}
