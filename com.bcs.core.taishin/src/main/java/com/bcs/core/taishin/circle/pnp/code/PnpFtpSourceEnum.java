package com.bcs.core.taishin.circle.pnp.code;


import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMainUnica;

/**
 * Pnp Ftp Source Enum
 *
 * @author Alan
 */
public enum PnpFtpSourceEnum {
    /**
     * 三竹來源
     */
    MITAKE(1, "1", "mitake", "三竹", "BCS_PNP_MAIN_MITAKE", "BCS_PNP_DETAIL_MITAKE", PnpDetailMitake.class, PnpMainMitake.class),
    /**
     * 互動來源
     */
    EVERY8D(2, "2", "every8d", "互動", "BCS_PNP_MAIN_EVERY8D", "BCS_PNP_DETAIL_EVERY8D", PnpDetailEvery8d.class, PnpMainEvery8d.class),
    /**
     * 明宣來源
     */
    MING(3, "3", "ming", "明軒", "BCS_PNP_MAIN_MING", "BCS_PNP_DETAIL_MING", PnpDetailMing.class, PnpMainMing.class),

    /**
     * UNICA來源
     */
    UNICA(4, "4", "unica", "Unica", "BCS_PNP_MAIN_UNICA", "BCS_PNP_DETAIL_UNICA", PnpDetailUnica.class, PnpMainUnica.class);

    /**
     * Id
     */
    public final int id;
    public final String code;
    public final String english;
    public final String chinese;
    public final String mainTable;
    public final String detailTable;
    public final Class detailClass;
    public final Class mainClass;

    /**
     * Default Constructor
     * @param id id
     */
    PnpFtpSourceEnum(int id, String code, String english, String chinese, String mainTable, String detailTable, Class detailClass, Class mainClass) {
        this.id = id;
        this.code = code;
        this.english = english;
        this.chinese = chinese;
        this.mainTable = mainTable;
        this.detailTable = detailTable;
        this.detailClass = detailClass;
        this.mainClass = mainClass;
    }

    public static PnpFtpSourceEnum findEnumByCode(String code){
        for (PnpFtpSourceEnum e : PnpFtpSourceEnum.values()){
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static PnpFtpSourceEnum findEnumByChinese(String chinese){
        for (PnpFtpSourceEnum e : PnpFtpSourceEnum.values()){
            if (e.chinese.equals(chinese)) {
                return e;
            }
        }
        return null;
    }
}
