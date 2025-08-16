package com.convertor.convert.utils;


import com.convertor.convert.model.Profile;
import com.convertor.convert.model.Field;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component("profileItemProcessorUtils")
public class ProfileItemProcessorUtils implements ItemProcessor<String, Profile> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Profile process(@NonNull String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);

        Profile profile = Profile.builder()
                .id(UUID.randomUUID().toString())
                .name(root.path("name").asText(null))
                .createdAt(java.time.Instant.now())
                .build();

        List<Field> fields = new ArrayList<>();

        Field ageField = Field.builder()
                .id(UUID.randomUUID().toString())
                .profileId(profile.getId())
                .typeField("NUMBER")
                .value(root.path("age").asText(null))
                .createdAt(java.time.Instant.now())
                .build();

        fields.add(ageField);

        profile.setFields(fields);
        return profile;
    }
}
