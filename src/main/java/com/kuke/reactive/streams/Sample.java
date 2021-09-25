package com.kuke.reactive.streams;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public class Sample {

    // duality : 두 가지가 기능은 똑같은데, 그거를 반대방향으로 표현하는 것
    // Iterable                     <--->   Observable
    // Pull(다음 것을 끌어오는 방식)  <--->   Push(데이터를 가져가라고 밀어넣는 방식)

    // 옵저버 패턴은 좋은 아이디어지만, 문제 두 가지를 지적함
    // 1. Complete 이란 개념이 없음. 데이터를 다 보내도 완료라는 개념을 표현할 수 없음.
    // 2. Error. 동작하다가 exception 이 발생하면? 그에 대한 대응책이 패턴에 녹아있지 않음.
    // 이 두 가지를 추가해서 새롭게 확장된 옵저버 패턴을 만듦.

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

    static class IntObservable extends Observable implements Runnable {
        @Override
        public void run() {
            for(int i=1; i<=10; i++) {
                setChanged();
                notifyObservers(i);
            }
        }
    }

    public void observableSample() {
        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + ", arg = " + arg);
            }
        };

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + ", EXIT");
        es.shutdown();
    }

    public static void main(String[] args) {
        Sample sample = new Sample();
//        sample.iterableSample();


        sample.observableSample();
    }
}
