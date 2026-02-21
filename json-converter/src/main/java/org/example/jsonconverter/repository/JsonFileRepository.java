package org.example.jsonconverter.repository;

import org.example.jsonconverter.entity.JsonFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JsonFileRepository extends JpaRepository<JsonFile,Long> {
    JsonFile findByFileName(String filename);
}
