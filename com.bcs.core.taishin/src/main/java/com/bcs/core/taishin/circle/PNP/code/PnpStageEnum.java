package com.bcs.core.taishin.circle.PNP.code;

import lombok.Getter;

/**
 * Pnp Stage Enum
 *
 * @author Alan
 */
public enum PnpStageEnum {
    /**
     * Stage
     */
    BC("BC"),
    PNP("PNP"),
    SMS("SMS");

    public final String value;

    PnpStageEnum(String value) {
        this.value = value;
    }

    public static PnpStageEnum findEnumByName(String value){
        for (PnpStageEnum e : PnpStageEnum.values()){
            if (e.value.equals(value)) {
                return e;
            }
        }
        return BC;
    }
}
