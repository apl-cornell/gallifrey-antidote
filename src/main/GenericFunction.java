package main;

import java.io.*;
import java.util.Random;

import com.ericsson.otp.erlang.OtpErlangBinary;

public class GenericFunction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String FunctionName;
    private Object Argument;
    private Integer Id;

    public GenericFunction(String FunctionName, Object Argument) {
        this.FunctionName = FunctionName;
        this.Argument = Argument;
        this.Id = new Random().nextInt(10000000);
    }

    public String getFunctionName() {
        return FunctionName;
    }

    public Object getArgument() {
        return Argument;
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
            System.out.println(func.getArgument());
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