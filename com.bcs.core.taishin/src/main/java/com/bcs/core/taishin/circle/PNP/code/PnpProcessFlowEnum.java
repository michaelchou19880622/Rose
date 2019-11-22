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
    BC(1, "BC"),
    BC_SMS(2, "BC_SMS"),
    BC_PNP_SMS(3, "BC_PNP_SMS");

    /**
     * Id
     */
    public final int id;
    public final String value;

    /**
     * Default Constructor
     *
     * @param id id
     */
    PnpProcessFlowEnum(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public static PnpProcessFlowEnum findEnumByName(String value){
        for (PnpProcessFlowEnum e : PnpProcessFlowEnum.values()){
            if (e.value.equals(value)) {
                return e;
            }
        }
        return BC;
    }
}
