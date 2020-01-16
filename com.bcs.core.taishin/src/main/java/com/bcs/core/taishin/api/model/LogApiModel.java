package com.bcs.core.taishin.api.model;

import com.bcs.core.json.AbstractBcsEntity;

/**
 * @author ???
 */
public class LogApiModel extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    public static String CREATE = "A";
    public static String DELETE = "D";
    public static String UPDATE = "E";
    public static String READ = "Q";
    public static String REPORT = "R";
    public static String EXPORT = "O";
    public static String PRINT = "P";
    public static String LOGIN = "L";
    public static String LOGOUT = "X";

    public static String SUCCESS = "Y";

    public static String SYSTEM_CODE = "LINE-BC";

    private String systemCode = SYSTEM_CODE;
    private String userId = "";
    private String sensitiveData = "";

    private String functionType;
    private String functionName;
    private String functionStatus;
    private String clientIp;

    private String queryString;
    private Object data;

    private LogApiModel(LogApiModelBuilder logApiModelBuilder) {
        this.userId = logApiModelBuilder.userId;
        this.sensitiveData = logApiModelBuilder.sensitiveData;
        this.functionType = logApiModelBuilder.functionType;
        this.functionName = logApiModelBuilder.functionName;
        this.functionStatus = logApiModelBuilder.functionStatus;
        this.clientIp = logApiModelBuilder.clientIp;
        this.queryString = logApiModelBuilder.queryString;
        this.data = logApiModelBuilder.data;
    }

    public static class LogApiModelBuilder {
        private String userId = "";
        private String sensitiveData = "";

        private String functionType;
        private String functionName;
        private String functionStatus;
        private String clientIp;

        private String queryString;
        private Object data;


        public LogApiModelBuilder() {
        }

        public LogApiModelBuilder functionName(String functionName) {
            this.functionName = functionName;
            return this;
        }

        public LogApiModelBuilder functionType(String functionType) {
            this.functionType = functionType;
            return this;
        }

        public LogApiModelBuilder functionStatus(String functionStatus) {
            this.functionStatus = functionStatus;
            return this;
        }

        public LogApiModelBuilder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public LogApiModelBuilder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public LogApiModelBuilder data(Object data) {
            this.data = data;
            return this;
        }

        public LogApiModelBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public LogApiModelBuilder sensitiveData(String sensitiveData) {
            this.sensitiveData = sensitiveData;
            return this;
        }

        public LogApiModel build() {
            return new LogApiModel(this);
        }
    }

    public String getSystemCode() {
        return systemCode;
    }

    public String getUserId() {
        return userId;
    }

    public String getSensitiveData() {
        return sensitiveData;
    }

    public String getFunctionType() {
        return functionType;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getFunctionStatus() {
        return functionStatus;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getQueryString() {
        return queryString;
    }

    public Object getData() {
        return data;
    }
}
