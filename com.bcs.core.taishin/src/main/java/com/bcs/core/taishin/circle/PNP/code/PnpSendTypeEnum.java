package com.bcs.core.taishin.circle.PNP.code;

/**
 * Pnp Send Type Enum
 *
 * @author Alan
 */
public enum PnpSendTypeEnum {
    /**
     * Send Type
     */
    IMMEDIATE("IMMEDIATE"),
    SCHEDULED("SCHEDULED"),
    SCHEDULE_TIME_EXPIRED("SCH_EXPRED"),
    DELAY("DELAY");

    /**
     * Value
     */
    public final String value;

    /**
     * Default Constructor
     *
     * @param value Value
     */
    PnpSendTypeEnum(String value) {
        this.value = value;
    }

    public static PnpSendTypeEnum findEnumByName(String value){
        for (PnpSendTypeEnum e : PnpSendTypeEnum.values()){
            if (e.value.equals(value)) {
                return e;
            }
        }
        return IMMEDIATE;
    }
}
