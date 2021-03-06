package com.kuke.reactive;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@SpringBootApplication
//@EnableAsync
@RestController
public class ReactiveApplication {

	@GetMapping("/event/{id}")
	Mono<List<Event>> event(@PathVariable Long id) {
		List<Event> list = Arrays.asList(new Event(1L, "event1")
				, new Event(2L, "event2"));
		return Mono.just(list).log();
	}

	@GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Event> events() {
		Flux<Event> es = Flux
				.<Event, Long>generate(() -> 1L, (id, sink) -> {
					sink.next(new Event(id, "value" + id));
					return id + 1;
				});
		Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
		
		return Flux.zip(
				es, interval
		).map(tu -> tu.getT1());

//		return Flux
//				.<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value")))
//				.delayElements(Duration.ofSeconds(1))
//				.take(10);
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveApplication.class, args);
	}

	@Data @AllArgsConstructor
	public static class Event {
		long id;
		String value;
	}

// ---

//	@GetMapping("/")
//	Mono<String> hello() {
//		// subscriber??? ????????? ????????? ???????????? ???????????? ?????????.
//		// ????????? publisher??? ?????? ?????? subscriber??? ?????? ??? ??????
//		// publisher??? ????????? ????????? ??? ?????? ????????? ??????
//		// 1. Cold : ?????? Subscriber??? ??????????????? ?????? ????????? ????????? ?????????????????? ??????
//		// ????????? ??? ??? ?????? publisher??? ????????? ?????? ???????????? ???????????? ??? ???????????? ???
//		// 2. Hot : ????????? ???????????? ??????????????? ???????????? ????????? ????????????.
//		log.info("pos1");
//
//		String msg = generateHello();
//		Mono<String> m = Mono.just(msg).doOnNext(c -> log.info(c)).log();
////		m.subscribe();
//
//		log.info("pos2");
//		return m;
//	}
//
//	private String generateHello() {
//		log.info("method generateHello()");
//		return "Hello Mono";
//	}

//	---

//	static final String URL1 = "http://localhost:8081/service?req={req}";
//	static final String URL2 = "http://localhost:8081/service2?req={req}";
//
//	@Autowired MyService myService;
//
//	WebClient client = WebClient.create();
//
//	@GetMapping("/rest")
//	public Mono<String> rest(int idx) {
//		// ???????????? webflux??? ??????????????? ?????? ????????????
//		return client.get().uri(URL1, idx).exchange()
//				.flatMap(c -> c.bodyToMono(String.class))
//				.flatMap(res -> client.get().uri(URL2, res).exchange())
//				.flatMap(c -> c.bodyToMono(String.class))
//				.flatMap(res -> Mono.fromCompletionStage(myService.work(res)));
//	}
//
//	@Service
//	public static class MyService {
//		// ?????? ?????? ????????? ????????? ????????????, ??? ????????? netty ???????????? ?????? ??????
//		// ???????????? ?????? ??????????????? ??????????????? ?????????. ????????? ?????????
//		// ????????? ???????????? ???????????????
//		@Async
//		public CompletableFuture<String> work(String req) {
//			return CompletableFuture.completedFuture(req + "/asyncwork");
//		}
//	}

// ---

//	@RestController
//	public static class MyController {
//
//		@Autowired MyService myService;
//
//		static final String URL1 = "http://localhost:8081/service?req={req}";
//		static final String URL2 = "http://localhost:8081/service2?req={req}";
//
//		AsyncRestTemplate rt = new AsyncRestTemplate(
//				new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
//
//		@GetMapping("/rest")
//		public DeferredResult<String> rest(int idx) {
//			// Controller??? ????????? ???????????? ???????????? ????????????,
//			// ????????? ????????? mvc??? ????????? ????????????. ?????? ?????? ????????? ???????????????.
//			// ?????? ???????????? 1?????????, AsyncRestTemplate??? ?????? API??? ????????? ???,
//			// 1?????? ??????????????? ????????? 1?????? ??????. ????????? ??? ??????.
//			// netty??? ???????????? ?????? ?????? ????????????, ???????????? ?????? ????????? ?????? ????????????.
//
//			// ????????? ??? ??????????????? ?????? ????????? ??? ????????? ???????????? ????????????
//			DeferredResult<String> dr = new DeferredResult<>();
//
//			toCF(rt.getForEntity(URL1, String.class, "hello" + idx))
//					.thenCompose(s -> toCF(rt.getForEntity(URL2, String.class, s.getBody())))
//					.thenApplyAsync(s -> myService.work(s.getBody()))
//					.thenAccept(s -> dr.setResult(s))
//					.exceptionally(e ->{
//						dr.setErrorResult(e.getMessage());
//						return (Void) null;
//					});
//
////			Completion
////					.from(rt.getForEntity(URL1, String.class, "hello" + idx))
////					.andApply(s -> rt.getForEntity(URL2, String.class, s.getBody()))
////					.andApply(s -> myService.work(s.getBody()))
////					.andError(e -> dr.setErrorResult(e.toString()))
////					.andAccept(s -> dr.setResult(s));
//
////			ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity(URL1, String.class, "hello" + idx);
////
////			f1.addCallback(s -> {
////				ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity(URL2, String.class, s.getBody());
////				f2.addCallback(s2 -> {
////					ListenableFuture<String> f3 = myService.work(s2.getBody());
////					f3.addCallback(s3 -> {
////						dr.setResult(s3);
////					}, e-> {
////						dr.setErrorResult(e.getMessage());
////					});
////				}, e-> {
////					dr.setErrorResult(e.getMessage());
////				});
////			}, e-> {
////				dr.setErrorResult(e.getMessage());
////			});
//			return dr;
//		}
//
//		public <T> CompletableFuture<T> toCF(ListenableFuture<T> lf) {
//			CompletableFuture<T> cf = new CompletableFuture<>();
//			lf.addCallback(s -> {
//				cf.complete(s);
//			}, e -> {
//				cf.completeExceptionally(e);
//			});
//			return cf;
//		}
//	}
//
//	public static class AcceptCompletion<S> extends Completion<S, Void>{
//		public Consumer<S> con;
//		public AcceptCompletion(Consumer<S> con) {
//			this.con = con;
//		}
//
//		@Override
//		protected void run(S value) {
//			con.accept(value);
//		}
//	}
//
//	public static class ErrorCompletion<T> extends Completion<T, T>{
//		public Consumer<Throwable> econ;
//		public ErrorCompletion(Consumer<Throwable> econ) {
//			this.econ = econ;
//		}
//
//		@Override
//		protected void run(T value) {
//			if(next != null) next.run(value);
//		}
//
//		@Override
//		protected void error(Throwable e) {
//			econ.accept(e);
//		}
//	}
//
//	public static class ApplyCompletion<S, T> extends Completion<S, T> {
//		public Function<S, ListenableFuture<T>> fn;
//		public ApplyCompletion(Function<S, ListenableFuture<T>> fn) {
//			this.fn = fn;
//		}
//
//		@Override
//		protected void run(S value) {
//			final ListenableFuture<T> lf = fn.apply(value);
//			lf.addCallback(s -> complete(s), e->error(e));
//		}
//	}
//
//	public static class Completion<S, T> {
//		Completion next;
//
//		public void andAccept(Consumer<T> con) {
//			Completion<T, Void> c = new AcceptCompletion<>(con);
//			this.next = c;
//		}
//
//		public Completion<T, T> andError(Consumer<Throwable> econ) {
//			Completion<T, T> c = new ErrorCompletion<>(econ);
//			this.next = c;
//			return c;
//		}
//
//		public <V> Completion<T, V> andApply(Function<T, ListenableFuture<V>> fn) {
//			Completion<T, V> c = new ApplyCompletion<>(fn);
//			this.next = c;
//			return c;
//		}
//
//		public static <S, T> Completion<S, T> from(ListenableFuture<T> lf) {
//			Completion<S, T> c = new Completion<>();
//			lf.addCallback(s -> {
//				c.complete(s);
//			}, e-> {
//				c.error(e);
//			});
//			return c;
//		}
//
//		protected void error(Throwable e) {
//			if(next != null) next.error(e);
//		}
//
//		protected void complete(T s) {
//			if(next != null) next.run(s);
//		}
//
//		protected void run(S value) {
//
//		}
//	}
//
//	@Bean
//	ThreadPoolTaskExecutor myThreadPool() {
//		final ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
//		te.setCorePoolSize(1);
//		te.setMaxPoolSize(1);
//		te.initialize();
//		return te;
//	}

//	---

//	@RestController
//	public static class MyController {
//
//		@GetMapping("/emitter")
//		public ResponseBodyEmitter emitter() throws InterruptedException {
//			ResponseBodyEmitter emitter = new ResponseBodyEmitter();
//
//			Executors.newSingleThreadExecutor()
//					.submit(() -> {
//						for (int i = 1; i <= 50; i++) {
//							try {
//								emitter.send("<p>Stream" + i + "</p>");
//								Thread.sleep(200);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					});
//
//			return emitter;
//		}
//
//	}

//	---

//	@RestController
//	public static class MyController {
//
//		// DeferredResult??? ????????? ?????? ???????????? ????????? ??????
//
//		Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();
//
//		@GetMapping("/dr")
//		public DeferredResult<String> callable() throws InterruptedException {
//			log.info("dr");
//			DeferredResult<String> dr = new DeferredResult<>();
//			results.add(dr);
//			return dr;
//		}
//
//		@GetMapping("/dr/count")
//		public String drcount() {
//			return String.valueOf(results.size());
//		}
//
//		@GetMapping("/dr/event")
//		public String drevent(String msg) {
//			for (DeferredResult<String> dr : results) {
//				dr.setResult("Hello " + msg);
//				results.remove(dr);
//			}
//			return "OK";
//		}
//	}

//	---

//	@RestController
//	public static class MyController {
//
//		// ?????? ????????? ????????? ???????????? ?????? ?????? ?????? ????????? ??????
//
//		@GetMapping("/callable")
//		public Callable<String> callable() throws InterruptedException {
//			log.info("callable");
//			return () -> {
//				// ????????? ???????????? ?????? ????????????, ?????? ???????????? ??????
//				log.info("async");
//				Thread.sleep(2000);
//				return "hello";
//			};
//		}
////		public String callable() throws InterruptedException {
////			log.info("async");
////			Thread.sleep(2000);
////			return "hello";
////		}
//	}

//	---

//	@Component
//	public static class MyService {
//
//		@Async
//		public ListenableFuture<String> hello() throws InterruptedException {
//			log.info("hello()");
////			Thread.sleep(2000);
//			return new AsyncResult<>("Hello");
//		}
//	}
//
//	public static void main(String[] args) {
//		try (ConfigurableApplicationContext c = SpringApplication.run(ReactiveApplication.class, args)) {
//
//		}
//	}
//
//	@Autowired MyService myService;
//
//	@Bean
//	ApplicationRunner run() {
//		// ?????? ?????? ??? ?????? ??????
//		return args -> {
//			log.info("run()");
//			ListenableFuture<String> f = myService.hello();
//			f.addCallback(s -> System.out.println("s = " + s),
//					e -> System.out.println("e = " + e.getMessage()));
//			log.info("exit");
//		};
//	}

//	---

//	@RestController
//	public static class Controller {
//
//		@RequestMapping("/hello")
//		public Publisher<String> hello(String name) {
//			return new Publisher<String>() {
//				@Override
//				public void subscribe(Subscriber<? super String> s) {
//					s.onSubscribe(new Subscription() {
//						@Override
//						public void request(long n) {
//							log.info("request = {}", n);
//							s.onNext("Hello " + name);
//							s.onComplete();
//						}
//
//						@Override
//						public void cancel() {
//
//						}
//					});
//				}
//			};
//		}
//	}

}
