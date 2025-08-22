package com.convertor.convert.model;

public class ConstructionRule {
    private TypeField typeField;
    private List<String> values;

    // Getters y setters
    public TypeField getTypeField() {
        return typeField;
    }

    public void setTypeField(TypeField typeField) {
        this.typeField = typeField;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}