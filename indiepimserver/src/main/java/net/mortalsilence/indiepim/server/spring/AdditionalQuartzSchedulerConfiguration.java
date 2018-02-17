package net.mortalsilence.indiepim.server.spring;

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class AdditionalQuartzSchedulerConfiguration implements SchedulerFactoryBeanCustomizer {

    @Override
    public void customize(SchedulerFactoryBean factory) {
        factory.setSchedulerName("RoboSched");
        factory.setAutoStartup(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setOverwriteExistingJobs(true);
    }
}
