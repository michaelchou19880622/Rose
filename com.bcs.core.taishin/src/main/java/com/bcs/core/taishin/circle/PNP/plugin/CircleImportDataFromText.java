package com.bcs.core.taishin.circle.PNP.plugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CircleImportDataFromText {

    public static final String UTF8_BOM = "\uFEFF";
    public static final String ENCODE_UTF8 = "UTF-8";
    
	public Set<String> importData(InputStream inp) throws Exception {
		
		Set<String> values = new HashSet<String>();

		LineIterator lineIterator = IOUtils.lineIterator(inp, ENCODE_UTF8);

	    while (lineIterator.hasNext()) {
	        String value = lineIterator.nextLine();
	        value = StringUtils.trimToEmpty(value);
	        if(value.startsWith(UTF8_BOM)) {
	            value = value.substring(1);
	        }
	        
	        if (StringUtils.isBlank(value)) {
	        	continue;
	        }
//			logger.debug("value:" + value);
	        
	        values.add(value);
	    }
		
		return values;
	}

	public List<String> importDataList(InputStream inp) throws Exception {
		
		List<String> values = new ArrayList<String>();

		LineIterator lineIterator = IOUtils.lineIterator(inp, "UTF-8");
	    while (lineIterator.hasNext()) {
	        String value = lineIterator.nextLine();
	        value = StringUtils.trimToEmpty(value);
	        if(value.startsWith(UTF8_BOM)) {
                value = value.substring(1);
            }
	        
	        if (StringUtils.isBlank(value)) {
	        	continue;
	        }
//			logger.debug("value:" + value);
	        
	        values.add(value);
	    }
		
		return values;
	}

	public Map<String, String> importDataKeyValue(InputStream inp) throws Exception {
		
		Map<String, String> values = new HashMap<String, String>();

		LineIterator lineIterator = IOUtils.lineIterator(inp, "UTF-8");
	    while (lineIterator.hasNext()) {
	        String value = lineIterator.nextLine();
	        value = StringUtils.trimToEmpty(value);
	        if(value.startsWith(UTF8_BOM)) {
                value = value.substring(1);
            }
	        
	        if (StringUtils.isBlank(value)) {
	        	continue;
	        }
//			logger.debug("value:" + value);
			
			String[] data = value.split(",");
	        
			if(data != null && data.length == 2){
				values.put(data[0], data[1]);
			}
	    }
		
		return values;
	}

	public Map<String, List<String>> importDataKeyValueList(InputStream inp) throws Exception {
		
		Map<String, List<String>> values = new HashMap<String, List<String>>();

		LineIterator lineIterator = IOUtils.lineIterator(inp, "UTF-8");
	    while (lineIterator.hasNext()) {
	        String value = lineIterator.nextLine();
	        value = StringUtils.trimToEmpty(value);
	        if(value.startsWith(UTF8_BOM)) {
                value = value.substring(1);
            }
	        
	        if (StringUtils.isBlank(value)) {
	        	continue;
	        }
//			logger.debug("value:" + value);
			
			String[] data = value.split(",");
	        
			if(data != null && data.length > 1){
				List<String> list = new ArrayList<String>(Arrays.asList(data));
				list.remove(0);
				values.put(data[0], list);
			}
	    }
		
		return values;
	}
	
	public List<Map<String, String>> importCSVDataKeyValueList(InputStream inp) throws Exception {
		List<Map<String, String>> dataListMap = new ArrayList<>();
		LineIterator lineIterator = IOUtils.lineIterator(inp, "UTF-8");
		String columnValue = lineIterator.nextLine().replaceAll("(\"|\')","");
		String[] columnData = columnValue.split(",");
		List<String> columnList = new ArrayList<String>();
		
		for(String key : columnData) {
		    if(key.startsWith(UTF8_BOM)) {
		        columnList.add(key.substring(1).toUpperCase());
            }
		    else {
		        columnList.add(key.toUpperCase());
		    }
		}
		
		while (lineIterator.hasNext()) {
			String value = lineIterator.nextLine();
			value = StringUtils.trimToEmpty(value).replaceAll("(\"|\')","");
			if (StringUtils.isBlank(value)) {
				continue;
			}
			String[] data = value.split(",");
			if(data != null && data.length > 1){
				List<String> list = new ArrayList<String>(Arrays.asList(data));
				Map<String, String> map = new HashMap<String, String>();
				for(int i = 0; i < list.size(); i++){
					map.put(columnList.get(i), list.get(i));
				}
				dataListMap.add(map);
			}
		}
		return dataListMap;
	}
	
	public List<String> importDataList(InputStream inp, String encoding, boolean trimToEmpty) throws Exception {
        
        List<String> values = new ArrayList<String>();

        LineIterator lineIterator = IOUtils.lineIterator(inp, encoding);
        while (lineIterator.hasNext()) {
            String value = lineIterator.nextLine();
            if(trimToEmpty) {
                value = StringUtils.trimToEmpty(value);                
            }
            
            if(ENCODE_UTF8.equals(encoding) && value.startsWith(UTF8_BOM)) {
                value = value.substring(1);
            }
            
            if (StringUtils.isBlank(value)) {
                continue;
            }
            
            values.add(value);
        }
        
        return values;
    }
}
