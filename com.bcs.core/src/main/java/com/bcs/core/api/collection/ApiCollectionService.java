package com.bcs.core.api.collection;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ApiCollectionService {

	private Map<String, ApiServiceInterface> serviceCollection = new HashMap<String, ApiServiceInterface>();
	
	public void settingService(String serviceKey, ApiServiceInterface service){
		serviceCollection.put(serviceKey, service);	
	}
	
	public ApiServiceInterface getService(String serviceKey){
		return serviceCollection.get(serviceKey);
	}
	
	public void removeService(String serviceKey){
		serviceCollection.remove(serviceKey);
	}
}
