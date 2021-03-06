package gallifrey.frontend;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import gallifrey.core.*;

public class SharedLinkedListTest {
    @Test
    public void test() {
        SharedLinkedList<String> node1 = new SharedLinkedList<String>("node1");
        SharedObject obj1 = new SharedObject(node1);

        // Check node 1
        assertEquals("node1", (String) obj1.const_call("getData"));
        assertEquals(null, obj1.const_call("getNext"));

        SharedLinkedList<String> node2 = new SharedLinkedList<String>("node2");
        SharedObject obj2 = new SharedObject(node2);

        // Check node 2
        assertEquals("node2", obj2.const_call("getData"));
        assertEquals(null, obj2.const_call("getNext"));

        // Add node 2 as the next node for node 1
        List<Object> args1 = new ArrayList<Object>();
        args1.add(obj2);
        obj1.void_call("setNext", args1);
        SharedObject objnext = (SharedObject) obj1.const_call("getNext");
        assertEquals(obj2.key, objnext.key);

        // Update node 2 on it's own
        List<Object> args2 = new ArrayList<Object>();
        args2.add("changed data value");
        obj2.void_call("setData", args2);

        // Follow the path of node 1 to node 2 and confirm that the value changed
        SharedObject next_val = (SharedObject) obj1.const_call("getNext");
        assertEquals(obj2.key, next_val.key);
        assertEquals("changed data value", next_val.const_call("getData"));
        assertEquals(null, next_val.const_call("getNext"));
    }
}