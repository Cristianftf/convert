package com.convertor.convert.config;

import com.convertor.convert.dto.ProfileTemplateDTO;
import com.convertor.convert.processor.ProfileItemProcessor;
import com.convertor.convert.reader.JsonItemReader;
import com.convertor.convert.writer.ProfileTemplateWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<String> jsonItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        return new FlatFileItemReaderBuilder<String>()
                .name("jsonItemReader")
                .resource(new FileSystemResource(inputFile))
                .lineMapper((line, lineNumber) -> line) // Lee cada línea como un JSON completo
                .build();
    }

    @Bean
    public Step migrateProfilesStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    ProfileItemProcessor processor,
                                    ProfileTemplateWriter writer,
                                    FlatFileItemReader<String> reader) {

        return new StepBuilder("migrateProfilesStep", jobRepository)
                .<String, ProfileTemplateDTO>chunk(5, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job migrateProfileJob(JobRepository jobRepository, Step migrateProfilesStep) {
        return new JobBuilder("migrateProfileJob", jobRepository)
                .start(migrateProfilesStep)
                .build();
    }
}