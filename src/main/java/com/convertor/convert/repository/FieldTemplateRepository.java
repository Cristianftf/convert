package com.convertor.convert.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.convertor.convert.model.FieldTemplate;

public interface FieldTemplateRepository extends JpaRepository<FieldTemplate, String> {

    List<FieldTemplate> findByProfileTemplateIdAndDeletedFalse(String profileTemplateId);

    List<FieldTemplate> findByAncestorsSetContainingAndDeletedFalse(String ancestorId);
}
