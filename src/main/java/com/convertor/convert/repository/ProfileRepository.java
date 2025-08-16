package com.convertor.convert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.convertor.convert.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    
}
