package com.bcs.core.db.service;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentResourceFile;
import com.bcs.core.db.repository.ContentResourceFileRepository;

@Service
public class ContentResourceFileService {
	
	@Autowired
	private ContentResourceFileRepository contentResourceFileRepository;

	public ContentResourceFileService(){
	}
	
	@PreDestroy
	public void cleanUp() {
		System.gc();
	}
	
	public void save(ContentResourceFile contentResourceFile){
		contentResourceFileRepository.save(contentResourceFile);
	}
	
	public ContentResourceFile findOne(String resourceId){
		return contentResourceFileRepository.findOne(resourceId);
	}
}
