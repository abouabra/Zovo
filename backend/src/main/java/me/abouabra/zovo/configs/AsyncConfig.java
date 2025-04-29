package me.abouabra.zovo.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>
 * The <code>AsyncConfig</code> class enables Spring's asynchronous method execution capability and
 * defines a bean for configuring a thread pool task executor used in asynchronous operations.
 * </p>
 * <p>
 * This configuration is particularly useful for managing and executing tasks asynchronously,
 * reducing blocking and improving application performance in high workload scenarios.
 * </p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Provides a {@link ThreadPoolTaskExecutor} configured for email-related asynchronous operations.
     * <p>
     * The executor is set with customized thread pool properties such as core pool size,
     * maximum pool size, queue capacity, thread name prefix, and rejection policy.
     *
     * @return an {@link Executor} instance configured for asynchronous email task execution.
     */
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("EmailThread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}