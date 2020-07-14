package gallifrey.frontend;

import java.util.ArrayList;
import java.util.List;

import gallifrey.core.*;

import com.google.protobuf.ByteString;

public class DemoWithSetName {
    public static void main(String[] args) {
        Counter counter = new Counter(0);
        byte[] bytes = { (byte) 204, (byte) 29, (byte) 207, (byte) 217, (byte) 204, (byte) 29, (byte) 207, (byte) 217,
                (byte) 204, (byte) 29 };

        ByteString key = ByteString.copyFrom(bytes);
        SharedObject obj = new SharedObject(counter, key);
        System.out.print("The start value of this counter is: ");
        System.out.println(obj.const_call("value"));
        for (int i = 1; i <= 1000000; i++) {
            List<Object> args1 = new ArrayList<Object>();
            args1.add(i + 1);
            obj.void_call("increment", args1);
            List<Object> args3 = new ArrayList<Object>();
            obj.const_call("value");
            args3.add(i);
            obj.void_call("decrement", args3);
        }
        System.out.print("The end value of this counter is: ");
        System.out.println(obj.const_call("value"));
    }
}