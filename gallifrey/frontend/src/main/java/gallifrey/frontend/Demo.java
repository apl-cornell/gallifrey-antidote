package gallifrey.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gallifrey.core.*;

public class Demo {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("Test1", 1);
        // SharedMap is the Map class but with class fields and renamed to not conflict
        // with Map itself
        SharedMap<String, Integer> shareableMap = new SharedMap<String, Integer>(map);
        SharedObject sharedMap = new SharedObject(shareableMap);

        List<Object> putArgs = new ArrayList<Object>();
        putArgs.add("Test2");
        putArgs.add(2);
        sharedMap.void_call("put", putArgs);

        List<Object> key1 = new ArrayList<Object>();
        key1.add("Test1");
        if ((Boolean) sharedMap.const_call("containsKey", key1)) {
            System.out.print("Expected 1 and we got: ");
            System.out.println(sharedMap.const_call("get", key1));
        } else {
            System.out.println("We couldn't find the key");
        }

        List<Object> key2 = new ArrayList<Object>();
        key2.add("Test2");
        if ((Boolean) sharedMap.const_call("containsKey", key2)) {
            System.out.print("Expected 2 and we got: ");
            System.out.println(sharedMap.const_call("get", key2));
        } else {
            System.out.println("We couldn't find the key");
        }

        System.out.print("The value of our Shared Map is: ");
        System.out.println(sharedMap.const_call("value"));
    }
}