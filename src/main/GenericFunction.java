package main;

import java.io.*;

public class GenericFunction implements Serializable {
    private static final long serialVersionUID = 1L;
    private int ObjectId;
    private String FunctionName;
    private int Argument;

    public GenericFunction(int ObjectId, String FunctionName, int Argument) {
        this.ObjectId = ObjectId;
        this.FunctionName = FunctionName;
        this.Argument = Argument;
    }

    public int getObjectId() {
        return ObjectId;
    }

    public String getFunctionName() {
        return FunctionName;
    }

    public int getArgument() {
        return Argument;
    }

    public static void main(String[] args) {
        encode();
        try {
            GenericFunction func;
            FileInputStream f = new FileInputStream("file.txt");
            ObjectInputStream oos = new ObjectInputStream(f);
            func = (GenericFunction) oos.readObject();
            System.out.println(func.getObjectId());
            System.out.println(func.getFunctionName());
            System.out.println(func.getArgument());
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void encode() {
        try {
            GenericFunction func = new GenericFunction(0, "dothings", 1);
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