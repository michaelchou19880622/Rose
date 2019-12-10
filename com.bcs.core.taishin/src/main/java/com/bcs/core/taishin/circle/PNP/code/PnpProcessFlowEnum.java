package com.bcs.core.taishin.circle.PNP.code;

/**
 * Pnp Process Flow Enum
 *
 * @author Alan
 */
public enum PnpProcessFlowEnum {

    /**
     * Process Flow
     */
    BC(1, "1", "BC", "BC"),
    BC_SMS(2, "2", "BC_SMS", "BC->SMS"),
    BC_PNP_SMS(3, "3", "BC_PNP_SMS", "BC->PNP->SMS");

    /**
     * Id
     */
    public final int id;
    public final String code;
    public final String value;
    public final String uiText;

    /**
     * Default Constructor
     *
     * @param id id
     */
    PnpProcessFlowEnum(int id, String code, String value, String uiText) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.uiText = uiText;
    }

    public static PnpProcessFlowEnum findEnumByName(String value){
        for (PnpProcessFlowEnum e : PnpProcessFlowEnum.values()){
            if (e.value.equals(value)) {
                return e;
            }
        }
        return BC;
    }

    public static PnpProcessFlowEnum findEnumByCode(String code){
        for (PnpProcessFlowEnum e : PnpProcessFlowEnum.values()){
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
