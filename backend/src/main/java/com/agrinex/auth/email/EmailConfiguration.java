package com.agrinex.auth.email;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Selects the email adapter from {@code agrinex.email.provider} and provides the bounded executor that sends
 * account emails after the response (ADR-0048 items 5 and 8). With {@code resend}, a missing API key or sender
 * stops the start-up, so a misconfiguration is never discovered by a user.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfiguration {

	/** Name of the executor bean that delivers account emails. */
	public static final String EMAIL_EXECUTOR = "authEmailExecutor";

	/** Account emails are rare; two threads and a short queue bound the work a burst can create. */
	private static final int EMAIL_THREADS = 2;

	private static final int EMAIL_QUEUE_CAPACITY = 100;

	private static final int SHUTDOWN_GRACE_SECONDS = 15;

	@Bean
	EmailSender emailSender(EmailProperties properties, RestClient.Builder restClientBuilder) {
		if (properties.provider() != EmailProperties.Provider.RESEND) {
			return new NonDeliveringEmailSender();
		}
		if (!StringUtils.hasText(properties.resendApiKey())) {
			throw new IllegalStateException("agrinex.email.provider is 'resend' but RESEND_API_KEY is not set");
		}
		if (!StringUtils.hasText(properties.from())) {
			throw new IllegalStateException("agrinex.email.provider is 'resend' but AGRINEX_EMAIL_FROM is not set");
		}
		Duration timeout = properties.timeout();
		RestClient restClient = restClientBuilder.baseUrl(properties.resendBaseUrl())
			.requestFactory(ClientHttpRequestFactoryBuilder.jdk()
				.build(HttpClientSettings.defaults().withTimeouts(timeout, timeout)))
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.resendApiKey())
			.build();
		return new ResendEmailSender(restClient, properties.from());
	}

	@Bean(name = EMAIL_EXECUTOR)
	ThreadPoolTaskExecutor authEmailExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("agrinex-email-");
		executor.setCorePoolSize(EMAIL_THREADS);
		executor.setMaxPoolSize(EMAIL_THREADS);
		executor.setQueueCapacity(EMAIL_QUEUE_CAPACITY);
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(SHUTDOWN_GRACE_SECONDS);
		return executor;
	}

}
