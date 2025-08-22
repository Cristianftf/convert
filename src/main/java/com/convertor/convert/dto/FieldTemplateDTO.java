package com.convertor.convert.dto;

import com.convertor.convert.model.ConstructionRule;

public class FieldTemplateDTO {
    private String name;
    private ConstructionRule constructionRule;

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConstructionRule getConstructionRule() {
        return constructionRule;
    }

    public void setConstructionRule(ConstructionRule constructionRule) {
        this.constructionRule = constructionRule;
    }
}