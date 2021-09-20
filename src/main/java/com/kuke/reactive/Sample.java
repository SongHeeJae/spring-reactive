package com.kuke.reactive;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Sample {

    // duality : 두 가지가 기능은 똑같은데, 그거를 반대방향으로 표현하는 것
    // Iterable                     <--->   Observable
    // Pull(다음 것을 끌어오는 방식)  <--->   Push(데이터를 가져가라고 밀어넣는 방식)

    void iterableSample() {
        Iterable<Integer> iter = () ->
                new Iterator<>() {
                    int i = 0;
                    final static int MAX = 10;
                    @Override
                    public boolean hasNext() {
                        return i < MAX;
                    }

                    @Override
                    public Integer next() {
                        return ++i;
                    }
                };

        for (Integer integer : iter) {
            System.out.println("integer = " + integer);
        }

        for(Iterator<Integer> it = iter.iterator(); it.hasNext();) {
            System.out.println("it.next() = " + it.next());
        }
    }

    public static void main(String[] args) {
        Sample sample = new Sample();
        sample.iterableSample();
    }
}
