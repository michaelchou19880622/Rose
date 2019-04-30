package com.bcs.core.utils;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SystemCheckUtil {

    private static Runtime runtime = Runtime.getRuntime();

    public static ObjectNode Info() {

		ObjectNode result = (new ObjectMapper()).createObjectNode();

		result.putPOJO("OsInfo", OsInfo());
		result.putPOJO("MemInfo", MemInfo());
		result.putPOJO("DiskInfo", DiskInfo());
		
		return result;
    }

    public static String OSname() {
        return System.getProperty("os.name");
    }

    public static String OSversion() {
        return System.getProperty("os.version");
    }

    public static String OsArch() {
        return System.getProperty("os.arch");
    }

    public static int availableProcessors() {
        return runtime.availableProcessors();
    }

    public static long totalMemory() {
        return runtime.totalMemory() /1024;
    }

    public static long usedMemory() {
        return runtime.totalMemory() /1024 - runtime.freeMemory() /1024;
    }

    public static long freeMemory() {
        return runtime.freeMemory() /1024;
    }

    public static long maxMemory() {
        return runtime.maxMemory() /1024;
    }

    public static ObjectNode MemInfo() {

		ObjectNode result = (new ObjectMapper()).createObjectNode();
		
		result.put("MaxMemory", maxMemory());
		result.put("TotalMem", totalMemory());
		result.put("UsedMem", usedMemory());
		result.put("FreeMemory", freeMemory());
		
		return result;
    }

    public static ObjectNode OsInfo() {

		ObjectNode result = (new ObjectMapper()).createObjectNode();

		result.put("Os", OSname());
		result.put("OsVersion", OSversion());
		result.put("OsArch", OsArch());
		result.put("AvailableProcessors", availableProcessors());
		
		return result;
    }

    public static ObjectNode DiskInfo() {
        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();

		ObjectNode result = (new ObjectMapper()).createObjectNode();
		ArrayNode rootArray = (new ObjectMapper()).createArrayNode();
		
		result.putPOJO("DiskInfo", rootArray);
		
        for (File root : roots) {
    		ObjectNode info = (new ObjectMapper()).createObjectNode();
    		
    		rootArray.addPOJO(info);

    		info.put("FileRoot", root.getAbsolutePath());
    		info.put("TotalSpace", root.getTotalSpace());
    		info.put("FreeSpace", root.getFreeSpace());
    		info.put("UsableSpace", root.getUsableSpace());
        }
        
        return result;
    }
}
