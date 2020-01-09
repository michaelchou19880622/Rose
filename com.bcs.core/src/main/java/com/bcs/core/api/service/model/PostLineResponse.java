package com.bcs.core.api.service.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ???
 */
@Getter
@Setter
public class PostLineResponse {

    private int status;
    private String responseStr;

    public PostLineResponse(int status, String responseStr) {
        this.setStatus(status);
        this.setResponseStr(responseStr);
    }
}
