package com.convertor.convert.model.old;

import java.util.List;

public class OldProfile {
    private String id;
    private String name;
    private String access;
    private List<OldValue> values;

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public List<OldValue> getValues() {
        return values;
    }

    public void setValues(List<OldValue> values) {
        this.values = values;
    }
}