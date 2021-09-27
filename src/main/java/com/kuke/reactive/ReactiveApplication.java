package com.kuke.reactive;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
@EnableAsync
public class ReactiveApplication {

	@RestController
	public static class MyController {

		@Autowired MyService myService;

		static final String URL1 = "http://localhost:8081/service?req={req}";
		static final String URL2 = "http://localhost:8081/service2?req={req}";


		AsyncRestTemplate rt = new AsyncRestTemplate(
				new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

		@GetMapping("/rest")
		public DeferredResult<String> rest(int idx) {
			// Controller가 이것을 리턴하면 스레드는 반납하고,
			// 콜백은 스프링 mvc가 알아서 등록해줌. 응답 값에 따라서 리턴해준다.
			// 톰캣 스레드는 1개지만, AsyncRestTemplate은 외부 API를 호출할 때,
			// 1개당 백그라운드 스레드 1개씩 만듦. 굉장히 큰 비용.
			// netty가 제공하는 호출 기법 사용하면, 스레드가 약간 말고는 별로 안늘어남.

			// 언젠가 이 오브젝트의 값을 써주면 이 요청의 응답으로 처리해줌
			DeferredResult<String> dr = new DeferredResult<>();

			Completion
					.from(rt.getForEntity(URL1, String.class, "hello" + idx))
					.andApply(s -> rt.getForEntity(URL2, String.class, s.getBody()))
					.andApply(s -> myService.work(s.getBody()))
					.andError(e -> dr.setErrorResult(e.toString()))
					.andAccept(s -> dr.setResult(s));

//			ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity(URL1, String.class, "hello" + idx);
//
//			f1.addCallback(s -> {
//				ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity(URL2, String.class, s.getBody());
//				f2.addCallback(s2 -> {
//					ListenableFuture<String> f3 = myService.work(s2.getBody());
//					f3.addCallback(s3 -> {
//						dr.setResult(s3);
//					}, e-> {
//						dr.setErrorResult(e.getMessage());
//					});
//				}, e-> {
//					dr.setErrorResult(e.getMessage());
//				});
//			}, e-> {
//				dr.setErrorResult(e.getMessage());
//			});
			return dr;
		}
	}

	public static class AcceptCompletion<S> extends Completion<S, Void>{
		public Consumer<S> con;
		public AcceptCompletion(Consumer<S> con) {
			this.con = con;
		}

		@Override
		protected void run(S value) {
			con.accept(value);
		}
	}

	public static class ErrorCompletion<T> extends Completion<T, T>{
		public Consumer<Throwable> econ;
		public ErrorCompletion(Consumer<Throwable> econ) {
			this.econ = econ;
		}

		@Override
		protected void run(T value) {
			if(next != null) next.run(value);
		}

		@Override
		protected void error(Throwable e) {
			econ.accept(e);
		}
	}

	public static class ApplyCompletion<S, T> extends Completion<S, T> {
		public Function<S, ListenableFuture<T>> fn;
		public ApplyCompletion(Function<S, ListenableFuture<T>> fn) {
			this.fn = fn;
		}

		@Override
		protected void run(S value) {
			final ListenableFuture<T> lf = fn.apply(value);
			lf.addCallback(s -> complete(s), e->error(e));
		}
	}

	public static class Completion<S, T> {
		Completion next;

		public void andAccept(Consumer<T> con) {
			Completion<T, Void> c = new AcceptCompletion<>(con);
			this.next = c;
		}

		public Completion<T, T> andError(Consumer<Throwable> econ) {
			Completion<T, T> c = new ErrorCompletion<>(econ);
			this.next = c;
			return c;
		}

		public <V> Completion<T, V> andApply(Function<T, ListenableFuture<V>> fn) {
			Completion<T, V> c = new ApplyCompletion<>(fn);
			this.next = c;
			return c;
		}

		public static <S, T> Completion<S, T> from(ListenableFuture<T> lf) {
			Completion<S, T> c = new Completion<>();
			lf.addCallback(s -> {
				c.complete(s);
			}, e-> {
				c.error(e);
			});
			return c;
		}

		protected void error(Throwable e) {
			if(next != null) next.error(e);
		}

		protected void complete(T s) {
			if(next != null) next.run(s);
		}

		protected void run(S value) {

		}
	}

	@Service
	public static class MyService {
		@Async
		public ListenableFuture<String> work(String req) {
			return new AsyncResult<>(req + "/asyncwork");
		}
	}

	@Bean
	ThreadPoolTaskExecutor myThreadPool() {
		final ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(1);
		te.setMaxPoolSize(1);
		te.initialize();
		return te;
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveApplication.class, args);
	}

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
//		// DeferredResult는 별도의 워크 스레드가 생기지 않음
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
//		// 굳이 작업을 서블릿 스레드가 계속 물고 있을 필요가 없음
//
//		@GetMapping("/callable")
//		public Callable<String> callable() throws InterruptedException {
//			log.info("callable");
//			return () -> {
//				// 서블릿 스레드는 즉시 리턴하고, 작업 스레드가 처리
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
//		// 모든 빈이 다 뜨면 실행
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
