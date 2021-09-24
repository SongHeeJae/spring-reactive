package com.kuke.reactive.operator;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Operators
 * Publisher -> [Data1] -> Operator1 -> [Data2] -> Operator2 -> [Data3] -> Subscriber
 * Operator 를 통해서 가공이 일어남.
 *
 * pub -> [Data1] -> mapPub -> [Data2] -> logSub
 * 1. map (d1 -> f -> d2)
 *
 *
 * Function<T, R> : T -> return R
 * BiFunction<T, U, R> : T, U -> return R
 */

@Slf4j
public class PubSubV2 {

    public static void main(String[] args) {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10)
                .collect(Collectors.toList()));
        Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
//        Publisher<Integer> reducePub = reducePub(pub, 0, (a, b) -> a + b);
        mapPub.subscribe(logSub());
    }

//    private static Publisher<Integer> reducePub(Publisher<Integer> pub, int init, BiFunction<Integer, Integer, Integer> bf) {
//        return new Publisher<Integer>() {
//            @Override
//            public void subscribe(Subscriber<? super Integer> sub) {
//                pub.subscribe(new DelegateSub(sub) {
//                    int result = init;
//
//                    @Override
//                    public void onNext(Integer integer) {
//                        result = bf.apply(result, integer);
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        sub.onNext(result);
//                        sub.onComplete();
//                    }
//                });
//            }
//        };
//    }

    private static <T> Publisher<T> mapPub(Publisher<T> pub, Function<T, T> f) {
        return new Publisher<T>() {
            @Override
            public void subscribe(Subscriber<? super T> sub) {
                pub.subscribe(new DelegateSub<T>(sub) {
                    @Override
                    public void onNext(T t) {
                        sub.onNext(f.apply(t));
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T t) {
                log.debug("onNext:{}", t);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("onError:{}", t);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(List<Integer> iter) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(s -> sub.onNext(s));
                            sub.onComplete();
                        } catch (Throwable t) {
                            sub.onError(t);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}
