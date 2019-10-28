package main;

/**
 * Counter
 */
public class Counter extends Object {
    int count;

    public Counter(){
        this.count = 0;
    }

    public int value() {
        return count;
    }

    public void increment(int val) {
        count += val;
    }

    public void decrement(int val) {
        count -= val;
    }
}