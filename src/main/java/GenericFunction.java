import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class GenericFunction implements Serializable {
    private static final long serialVersionUID = new Random().nextLong();
    private String FunctionName;
    private List<Object> Arguments;
    private Integer Id;

    public GenericFunction(String FunctionName, Object Argument) {
        this.FunctionName = FunctionName;
        this.Arguments = new ArrayList<Object>(1);
        this.Arguments.add(Argument);
        this.Id = new Random().nextInt(10000000);
    }

    public GenericFunction(String FunctionName, List<Object> Argument) {
        this.FunctionName = FunctionName;
        this.Arguments = Argument;
        this.Id = new Random().nextInt(10000000);
    }

    public String getFunctionName() {
        return FunctionName;
    }

    public List<Object> getArguments() {
        return Arguments;
    }

    public Integer getId() {
        return Id;
    }
}