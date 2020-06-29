package com.bcs.core.taishin.circle.pnp.db.entity;

import lombok.*;

import java.util.Date;

/**
 * @ClassName PnpSendBlockParam
 * @Description TODO
 * @Author ean
 * @Date 2020/6/10 下午 12:50
 * @Version 1.0
 **/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PnpSendBlockParam {

    private Date startDate;
    private Date endDate;
    private String role;
    private Integer page;
    private Integer pageCount;
    private String mobile;
    private String insertUser;
    private String groupTag;
    private String insertDate;
    private String insertTime;
    private String modify_reason;
    private int blockEnable;
    private Integer inActive;
    
    
    
	@Override
	public String toString() {
		try {
	        return new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
	    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
}
