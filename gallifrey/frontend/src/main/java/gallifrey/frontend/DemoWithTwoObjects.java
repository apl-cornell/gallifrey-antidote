package gallifrey.frontend;

import java.util.ArrayList;
import java.util.List;

import gallifrey.core.*;

public class DemoWithTwoObjects {
    public static void main(String[] args) {
        Counter counter1 = new Counter(0);
        SharedObject obj1 = new SharedObject(counter1);
        System.out.print("The start value of counter1 is: ");
        System.out.println(obj1.const_call("value"));

        Counter counter2 = new Counter(0);
        SharedObject obj2 = new SharedObject(counter2);
        System.out.print("The start value of counter2 is: ");
        System.out.println(obj2.const_call("value"));

        List<Object> args1 = new ArrayList<Object>();
        args1.add(1);
        obj1.void_call("increment", args1);

        System.out.print("The start value of counter2 after incrementing counter1 is: ");
        System.out.println(obj2.const_call("value"));

        System.out.print("The start value of counter1 after incrementing is: ");
        System.out.println(obj1.const_call("value"));
    }
}