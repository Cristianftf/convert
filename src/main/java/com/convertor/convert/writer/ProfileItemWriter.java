package com.convertor.convert.writer;

import com.convertor.convert.model.Profile;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class ProfileItemWriter extends FlatFileItemWriter<Profile> {

    public ProfileItemWriter() {
        // dónde se va a guardar el archivo
        this.setResource(new FileSystemResource("profiles-output.txt"));

        // cómo se convierte cada Profile en texto
        this.setLineAggregator(new LineAggregator<Profile>() {
            @Override
            public String aggregate(Profile profile) {
                StringBuilder sb = new StringBuilder();
                sb.append("Profile[")
                        .append("id=").append(profile.getId())
                        .append(", name=").append(profile.getName())
                        .append(", profileTemplateId=").append(profile.getProfileTemplateId())
                        // Opcional: imprimir el número de campos para verificar
                        .append(", fields_count=").append(profile.getFields() != null ? profile.getFields().size() : 0)
                        .append("]");
                return sb.toString();
            }
        });
    }
}