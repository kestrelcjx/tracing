package com.baidu.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

@SpringBootApplication
public class TracingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TracingApplication.class, args);

		String endpoint = "http://kestrel-host-001:9411/api/v2/spans";
		BytesMessageSender sender = OkHttpSender.create(endpoint);
		AsyncZipkinSpanHandler zipkinSpanHandler = AsyncZipkinSpanHandler.create(sender);
		Tracing tracing = Tracing.newBuilder()
				.localServiceName("scoped-span-service")
				.addSpanHandler(zipkinSpanHandler)
				.build();

		Tracer tracer = tracing.tracer();
		Span parentSpan = tracer.nextSpan()
				.name("parent-span")
				.start();
		System.out.println(tracer.currentSpan());
		Tracer.SpanInScope scope = tracer.withSpanInScope(parentSpan);
		System.out.println(tracer.currentSpan());
		Span childSpan = tracer.nextSpan()
				.name("child-span")
				.start();
		System.out.println(childSpan.context().parentIdString());

		childSpan.finish();
		parentSpan.finish();
		scope.close();

		tracing.close();

		zipkinSpanHandler.flush();
		zipkinSpanHandler.close();
	}

}
