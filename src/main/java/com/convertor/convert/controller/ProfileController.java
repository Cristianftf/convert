package com.convertor.convert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final JobLauncher jobLauncher;
    private final Job parseProfileJob;

    @PostMapping("/import")
    public ResponseEntity<String> importProfile(
            @RequestBody String rawResponse,
            @RequestParam(name = "profileTemplateId", required = false) String profileTemplateId) throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("rawResponse", rawResponse)
                .addString("profileTemplateId", profileTemplateId) // Parámetro para el MappingService
                .addLong("time", System.currentTimeMillis()) // Evita colisiones de JobInstance
                .toJobParameters();

        jobLauncher.run(parseProfileJob, jobParameters);
        return ResponseEntity.ok("Job iniciado para procesar el perfil.");
    }
}