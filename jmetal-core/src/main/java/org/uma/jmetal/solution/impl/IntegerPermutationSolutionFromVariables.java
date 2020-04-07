package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.PermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegerPermutationSolutionFromVariables extends AbstractGenericSolution<Integer, PermutationProblem<?>>
        implements PermutationSolution<Integer> {

    /** Constructor */
    public IntegerPermutationSolutionFromVariables(PermutationProblem<?> problem, List<Integer> variables) {
        super(problem) ;
        for (int i = 0; i < getNumberOfVariables(); i++) {
            setVariableValue(i, variables.get(i)) ;
        }
    }

    /** Copy Constructor */
    public IntegerPermutationSolutionFromVariables(IntegerPermutationSolutionFromVariables solution) {
        super(solution.problem) ;
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            setObjective(i, solution.getObjective(i)) ;
        }

        for (int i = 0; i < problem.getNumberOfVariables(); i++) {
            setVariableValue(i, solution.getVariableValue(i));
        }

        attributes = new HashMap<Object, Object>(solution.attributes) ;
    }

    /** Empty Constructor */
    public IntegerPermutationSolutionFromVariables(PermutationProblem<?> problem) {
        super(problem);
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return attributes;
    }
    @Override public String getVariableValueString(int index) {
        return getVariableValue(index).toString();
    }

    @Override
    public IntegerPermutationSolutionFromVariables copy() {
        return new IntegerPermutationSolutionFromVariables(this);
    }
}
