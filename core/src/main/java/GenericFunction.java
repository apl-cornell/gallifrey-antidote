import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class GenericFunction implements Serializable {
    private static final long serialVersionUID = 3L;
    private String FunctionName;
    private List<Object> Arguments;

    public GenericFunction(String FunctionName, Object Argument) {
        this.FunctionName = FunctionName;
        this.Arguments = new ArrayList<Object>(1);
        this.Arguments.add(Argument);
    }

    public GenericFunction(String FunctionName, List<Object> Argument) {
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