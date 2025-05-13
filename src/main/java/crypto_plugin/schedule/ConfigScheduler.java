package crypto_plugin.schedule;

import crypto_plugin.client.BotvSingWsClient;
import crypto_plugin.v1.service.BotvSingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class ConfigScheduler implements SchedulingConfigurer {
    private final Logger log = LoggerFactory.getLogger(ConfigScheduler.class);
    @Autowired
    private BotvSingWsClient botvSingWsClient;
    @Autowired
    private BotvSingService botvSingService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("ScheduledTask-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }


    @Scheduled(initialDelay = 1000, fixedRate = 15000)
    public void getCoinSupply() {
        botvSingService.getCoinSupply();
    }

    @Scheduled(initialDelay = 1000, fixedRate = 30000)
    public void keepWebSocketAliveCheck() {
        botvSingWsClient.connect();
    }

}
