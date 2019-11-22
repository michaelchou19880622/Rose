package com.bcs.core.db.service;

import com.bcs.core.db.entity.ContentResourceFile;
import com.bcs.core.db.repository.ContentResourceFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContentResourceFileService {

    @Autowired
    private ContentResourceFileRepository contentResourceFileRepository;

    public ContentResourceFileService() {
    }

    public void save(ContentResourceFile contentResourceFile) {
        contentResourceFileRepository.save(contentResourceFile);
    }

    public ContentResourceFile findOne(String resourceId) {
        return contentResourceFileRepository.findOne(resourceId);
    }
}
