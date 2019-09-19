package com.bcs.web.api.controller;

import java.io.BufferedReader;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.api.service.LineUserStatusService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@RequestMapping("/api")
public class TestController {
    
    @Autowired
    private LineUserStatusService lineUserStatusService;
    
    @RequestMapping(method=RequestMethod.GET,value="/test/test11")
    @ResponseBody
    public void testPage(HttpServletRequest request) {
        String uid = "U1234567890abcdef7890123456789012";
        String status = "BLOCK";
        long time = new Date().getTime();
        
        lineUserStatusService.callLineUserStatusAPI(uid, status, time);
    }
    
    @RequestMapping(method=RequestMethod.POST,value="/test/call11")
    @ResponseBody
    public String testCall(HttpServletRequest request) throws Exception {
        
        BufferedReader bf =  request.getReader();
        StringBuffer sb = new StringBuffer();
        String str = "";
        ObjectMapper mapper = new ObjectMapper();
        
        while((str = bf.readLine()) != null) {
            sb.append(str);
        }
        
        JsonNode result = mapper.readTree(sb.toString());
        
        String uid = result.get("uid").toString();
        String status = result.get("status").toString();
        String time = result.get("time").toString();
        
        System.out.println("uid=" + uid + "; status=" + status + "; time=" + time);
        ObjectNode  node = mapper.createObjectNode();
        node.put("uid", uid);
        node.put("result", "1");
        node.put("message", "Success");
        
        return node.toString(); 
    }
}
