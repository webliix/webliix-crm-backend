package com.webliix.storage.repository;

import com.webliix.storage.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
    List<StoredFile> findByModuleAndReferenceIdOrderByCreatedAtDesc(String module, Long referenceId);
    List<StoredFile> findByModuleOrderByCreatedAtDesc(String module);
    List<StoredFile> findByReferenceIdOrderByCreatedAtDesc(Long referenceId);
    List<StoredFile> findAllByOrderByCreatedAtDesc();
}
