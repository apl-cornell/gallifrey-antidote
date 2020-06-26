package gallifrey.core;

import java.io.*;
import java.util.List;
import java.util.ArrayList;


public class GenericFunction implements Serializable {
    private static final long serialVersionUID = 3L;
    private String FunctionName;
    public final MergeComparator merge_strategy;
    private List<Object> Arguments;

    public GenericFunction(String FunctionName, MergeComparator merge_strategy) {
        this.merge_strategy = merge_strategy;
        this.FunctionName = FunctionName;
        this.Arguments = new ArrayList<Object>();
    }

    public GenericFunction(String FunctionName, MergeComparator merge_strategy, Object Argument) {
	this.merge_strategy = merge_strategy;
        this.FunctionName = FunctionName;
        this.Arguments = new ArrayList<Object>(1);
        this.Arguments.add(Argument);
    }

    public GenericFunction(String FunctionName, MergeComparator merge_strategy, List<Object> Argument) {
	this.merge_strategy = merge_strategy;
        this.FunctionName = FunctionName;
        this.Arguments = Argument;
    }

    public String getFunctionName() {
        return FunctionName;
    }

    public List<Object> getArguments() {
        return Arguments;
    }
}
