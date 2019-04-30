package com.bcs.core.api.collection;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ApiServiceInterface {

	public ObjectNode callApi(Map<String, Object> content) throws Exception;
	
	public ObjectNode callApi(Date start, Map<String, Object> content, int retryCount) throws Exception;
}
