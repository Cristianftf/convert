package com.convertor.convert.dto;

import com.convertor.convert.model.ConstructionRule;

public class ProfileTemplateDTO {
    private String name;
    private FieldTemplateDTO fieldTemplate;

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldTemplateDTO getFieldTemplate() {
        return fieldTemplate;
    }

    public void setFieldTemplate(FieldTemplateDTO fieldTemplate) {
        this.fieldTemplate = fieldTemplate;
    }
}