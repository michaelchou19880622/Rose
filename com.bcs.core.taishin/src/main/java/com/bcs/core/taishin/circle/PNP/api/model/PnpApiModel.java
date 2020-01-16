package com.bcs.core.taishin.circle.PNP.api.model;

import com.bcs.core.json.AbstractBcsEntity;

public class PnpApiModel extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    private String phone;
    private String message;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}