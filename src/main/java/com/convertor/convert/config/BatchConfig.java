package com.convertor.convert.config;

import com.convertor.convert.model.Profile;
import com.convertor.convert.processor.ProfileItemProcessor;
import com.convertor.convert.reader.ProfileItemReader;
import com.convertor.convert.writer.ProfileItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    // 👉 Reader recibe el parámetro rawResponse
    @Bean
    @StepScope
    public ProfileItemReader profileItemReader(@Value("#{jobParameters['rawResponse']}") String rawResponse) {
        return new ProfileItemReader(rawResponse);
    }

    @Bean
    public Step processProfilesStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      ProfileItemProcessor processor,
                                      ProfileItemWriter writer,
                                      ProfileItemReader reader) {

        return new StepBuilder("processProfilesStep", jobRepository)
                .<String, Profile>chunk(5, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job parseProfileJob(JobRepository jobRepository, Step processProfilesStep) {
        return new JobBuilder("parseProfileJob", jobRepository)
                .start(processProfilesStep)
                .build();
    }
}