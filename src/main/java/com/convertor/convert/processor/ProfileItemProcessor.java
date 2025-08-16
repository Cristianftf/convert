package com.convertor.convert.processor;

import com.convertor.convert.model.Profile;
import com.convertor.convert.service.MappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@StepScope // Importante: para poder inyectar JobParameters
@RequiredArgsConstructor
public class ProfileItemProcessor implements ItemProcessor<String, Profile> {

    private final MappingService mappingService;

    // Inyectamos el profileTemplateId desde los parámetros del Job
    private final String profileTemplateId;

    public ProfileItemProcessor(@Value("#{jobParameters['profileTemplateId']}") String profileTemplateId,
                                MappingService mappingService) {
        this.profileTemplateId = profileTemplateId;
        this.mappingService = mappingService;
    }

    @Override
    public Profile process(@NonNull String rawJson) throws Exception {
        // Toda la lógica de transformación compleja ahora se delega al MappingService.
        // El procesador actúa como un simple adaptador entre el mundo de Spring Batch y el servicio de negocio.
        return mappingService.transformRawJsonToProfile(rawJson, this.profileTemplateId);
    }
}