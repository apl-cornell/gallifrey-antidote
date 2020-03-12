import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class GenericFunction implements Serializable {
    private static final long serialVersionUID = 1L;
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

    public static void main(String[] args) {
        encode();
        try {
            GenericFunction func;
            FileInputStream f = new FileInputStream("file.txt");
            ObjectInputStream oos = new ObjectInputStream(f);
            func = (GenericFunction) oos.readObject();
            System.out.println(func.getFunctionName());
            System.out.println(func.getArguments());
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void encode() {
        try {
            GenericFunction func = new GenericFunction("dothings", 1);
            FileOutputStream f = new FileOutputStream("file.txt");
            ObjectOutputStream oos = new ObjectOutputStream(f);
            oos.writeObject(func);
            oos.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}