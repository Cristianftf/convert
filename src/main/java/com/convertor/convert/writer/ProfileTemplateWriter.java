package com.convertor.convert.writer;

import com.convertor.convert.dto.ProfileTemplateDTO;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class ProfileTemplateWriter implements ItemWriter<ProfileTemplateDTO> {

    @Value("#{jobParameters['apiEndpoint']}")
    private String apiEndpoint;

    @Value("#{jobParameters['authToken']}")
    private String authToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void write(List<? extends ProfileTemplateDTO> items) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);

        for (ProfileTemplateDTO item : items) {
            HttpEntity<ProfileTemplateDTO> request = new HttpEntity<>(item, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                apiEndpoint + "/ms-profile/v3/profile-templates", 
                request, 
                String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error al crear perfil: " + response.getBody());
            }
        }
    }
}