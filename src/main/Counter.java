package main;

/**
 * Counter
 */
public class Counter {
    int count;

    public Counter(int count){
        this.count = count;
    }

    public void increment(int val) {
        count += val;
    }

    public void decrement(int val) {
        count += val;
    }
}