package com.convertor.convert.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.convertor.convert.model.Field;

public interface FieldRepository extends JpaRepository<Field, String> {

    List<Field> findByProfileId(String profileId);
}
