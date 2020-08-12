package com.bcs.core.enums;

public enum EnumClickReportSortType {
    ByTracingID(0, "追蹤ID"), ByLinkURL(1, "連結URL");
    // 成員
    private int value;
    private String description;

    // 建構式
    private EnumClickReportSortType(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static String getDescription(int index) {
        for (EnumClickReportSortType data : EnumClickReportSortType.values()) {
            if (data.getValue() == index) {
                return data.getDescription();
            }
        }
        return "";
    }
}
