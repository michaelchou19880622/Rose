package com.bcs.core.taishin.circle.PNP.db.entity;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.utils.DataUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PNP Flex Template Config Json Object
 *
 * @author Alan
 */
@Slf4j(topic = "PnpRecorder")
@Getter
@Setter
@Entity
@Table(name = "BCS_PNP_FLEX_TEMPLATE",
        indexes = {
                @Index(name = "IDX_CREATE_TIME", columnList = "CREATE_TIME"),
                @Index(name = "IDX_MODIFY_TIME", columnList = "MODIFY_TIME")
        })
public class PnpFlexTemplate extends AbstractBcsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;


    @Column(name = "HEADER_BACKGROUND", columnDefinition = "varchar(7) default ''")
    private String headerBackground;
    @Column(name = "HEADER_TEXT_SIZE", columnDefinition = "varchar(5) default ''")
    private String headerTextSize;
    @Column(name = "HEADER_TEXT_COLOR", columnDefinition = "varchar(7) default ''")
    private String headerTextColor;
    @Column(name = "HEADER_TEXT_WEIGHT", columnDefinition = "varchar(10) default ''")
    private String headerTextWeight;
    @Column(name = "HEADER_TEXT_STYLE", columnDefinition = "varchar(10) default ''")
    private String headerTextStyle;
    @Column(name = "HEADER_TEXT_DECORATION", columnDefinition = "varchar(15) default ''")
    private String headerTextDecoration;
    @Column(name = "HEADER_TEXT", columnDefinition = "nvarchar(50) default ''")
    private String headerText;


    @Column(name = "HERO_BACKGROUND", columnDefinition = "varchar(7) default ''")
    private String heroBackground;
    @Column(name = "HERO_TEXT_SIZE", columnDefinition = "varchar(5) default ''")
    private String heroTextSize;
    @Column(name = "HERO_TEXT_COLOR", columnDefinition = "varchar(7) default ''")
    private String heroTextColor;
    @Column(name = "HERO_TEXT_WEIGHT", columnDefinition = "varchar(10) default ''")
    private String heroTextWeight;
    @Column(name = "HERO_TEXT_STYLE", columnDefinition = "varchar(10) default ''")
    private String heroTextStyle;
    @Column(name = "HERO_TEXT_DECORATION", columnDefinition = "varchar(15) default ''")
    private String heroTextDecoration;
    @Column(name = "HERO_TEXT", columnDefinition = "nvarchar(200) default ''")
    private String heroText;

    @Column(name = "BODY_BACKGROUND", columnDefinition = "varchar(7) default ''")
    private String bodyBackground;
    @Column(name = "BODY_DESC_TEXT_SIZE", columnDefinition = "varchar(5) default ''")
    private String bodyDescTextSize;
    @Column(name = "BODY_DESC_TEXT_COLOR", columnDefinition = "varchar(7) default ''")
    private String bodyDescTextColor;
    @Column(name = "BODY_DESC_TEXT_WEIGHT", columnDefinition = "varchar(10) default ''")
    private String bodyDescTextWeight;
    @Column(name = "BODY_DESC_TEXT_STYLE", columnDefinition = "varchar(10) default ''")
    private String bodyDescTextStyle;
    @Column(name = "BODY_DESC_TEXT_DECORATION", columnDefinition = "varchar(15) default ''")
    private String bodyDescTextDecoration;
    @Column(name = "BODY_DESC_TEXT", columnDefinition = "nvarchar(200) default ''")
    private String bodyDescText;


    @Column(name = "BUTTON_TEXT", columnDefinition = "nvarchar(600) default ''")
    private String buttonText;
    @Column(name = "BUTTON_COLOR", columnDefinition = "varchar(600) default ''")
    private String buttonColor;
    @Column(name = "BUTTON_URL", columnDefinition = "varchar(600) default ''")
    private String buttonUrl;

    @Column(name = "FOOTER_LINK_TEXT", columnDefinition = "nvarchar(20) default ''")
    private String footerLinkText;
    @Column(name = "FOOTER_LINK_URL", columnDefinition = "nvarchar(200) default ''")
    private String footerLinkUrl;

    /**
     * 更新時間
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;

    /**
     * 建立時間
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "CREATE_TIME")
    private Date createTime;

    @PrePersist
    public void prePersist() {
        this.createTime = new Date();
        this.modifyTime = createTime;
        log.info("Create Time is Update to : {}", DataUtils.formatDateToString(createTime, "yyyy-MM-dd HH:mm:ss"));
        log.info("Modify Time is Update to : {}", DataUtils.formatDateToString(modifyTime, "yyyy-MM-dd HH:mm:ss"));
    }

    @PreUpdate
    public void preUpdate() {
        this.modifyTime = new Date();
        log.info("Modify Time is Update to : {}", DataUtils.formatDateToString(modifyTime, "yyyy-MM-dd HH:mm:ss"));
    }

    public Map<String, String> mergeMap(Map<String, String>[] mapArrays) {
        Map<String, String> finalMap = new HashMap<>();
        for (Map<String, String> map : mapArrays) {
            finalMap.putAll(map);
        }
        return finalMap;
    }

    /**
     * TODO 暫時放置於此的Template
     * 未來有一個以上的Template可以搬移至Resource/template/flex/xxx.json
     * 提供上傳刪除xxx.json檔案的功能
     *
     * @return Default Template
     */
    public static String fetchDefaultTemplateJson() {
        return "{" +
                "    \"type\": \"flex\"," +
                "    \"altText\": \"This is a Flex Message\"," +
                "    \"contents\": {" +
                "        \"type\": \"bubble\"," +
                "        \"header\": {" +
                "            \"type\": \"box\"," +
                "            \"layout\": \"vertical\"," +
                "            \"position\": \"relative\"," +
                "            \"spacing\": \"none\"," +
                "            \"margin\": \"none\"," +
                "            \"height\": \"50px\"," +
                "            \"paddingAll\": \"12px\"," +
                "            \"backgroundColor\": \"headerBackground\"," +
                "            \"contents\": [" +
                "                {" +
                "                    \"type\": \"text\"," +
                "                    \"align\": \"center\"," +
                "                    \"position\": \"relative\"," +
                "                    \"margin\": \"none\"," +
                "                    \"gravity\": \"center\"," +
                "                    \"wrap\": true," +
                "                    \"size\": \"headerTextSize\"," +
                "                    \"color\": \"headerTextColor\"," +
                "                    \"text\": \"headerText\"," +
                "                    \"weight\": \"headerTextWeight\"," +
                "                    \"style\": \"headerTextStyle\"," +
                "                    \"decoration\": \"headerTextDecoration\"" +
                "                }" +
                "            ]" +
                "        }," +
                "        \"hero\": {" +
                "            \"type\": \"box\"," +
                "            \"layout\": \"vertical\"," +
                "            \"backgroundColor\": \"heroBackground\"," +
                "            \"contents\": [" +
                "                {" +
                "                    \"type\": \"box\"," +
                "                    \"layout\": \"vertical\"," +
                "                    \"margin\": \"lg\"," +
                "                    \"spacing\": \"sm\"," +
                "                    \"paddingBottom\": \"15px\"," +
                "                    \"paddingTop\": \"15px\"," +
                "                    \"paddingStart\": \"15px\"," +
                "                    \"paddingEnd\": \"15px\"," +
                "                    \"height\": \"150px\"," +
                "                    \"contents\": [" +
                "                        {" +
                "                            \"type\": \"text\"," +
                "                            \"wrap\": true," +
                "                            \"margin\": \"md\"," +
                "                            \"gravity\": \"center\"," +
                "                            \"text\": \"heroText\"," +
                "                            \"size\": \"heroTextSize\"," +
                "                            \"color\": \"heroTextColor\"," +
                "                            \"weight\": \"heroTextWeight\"," +
                "                            \"style\": \"heroTextStyle\"," +
                "                            \"decoration\": \"heroTextDecoration\"," +
                "                            \"offsetBottom\": \"8px\"" +
                "                        }" +
                "                    ]" +
                "                }" +
                "            ]" +
                "        }," +
                "        \"body\": {" +
                "            \"type\": \"box\"," +
                "            \"layout\": \"vertical\"," +
                "            \"margin\": \"none\"," +
                "            \"paddingTop\": \"10px\"," +
                "            \"paddingBottom\": \"10px\"," +
                "            \"backgroundColor\": \"bodyBackground\"," +
                "            \"contents\": [" +
                "                {" +
                "                    \"type\": \"box\"," +
                "                    \"layout\": \"vertical\"," +
                "                    \"contents\": [" +
                "                        {" +
                "                            \"type\": \"text\"," +
                "                            \"wrap\": true," +
                "                            \"size\": \"bodyDescTextSize\"," +
                "                            \"color\": \"bodyDescTextColor\"," +
                "                            \"text\": \"bodyDescText\"," +
                "                            \"weight\": \"bodyDescTextWeight\"," +
                "                            \"style\": \"bodyDescTextStyle\"," +
                "                            \"decoration\": \"bodyDescTextDecoration\"," +
                "                            \"gravity\": \"center\"," +
                "                            \"position\": \"relative\"," +
                "                            \"align\": \"start\"" +
                "                        }" +
                "                    ]" +
                "                }" +
                //ButtonArea
                " buttonJsonArea " +
                "            ]" +
                "        }," +
                "        \"footer\": {" +
                "            \"type\": \"box\"," +
                "            \"layout\": \"vertical\"," +
                "            \"spacing\": \"none\"," +
                "            \"contents\": [" +
                "                {" +
                "                    \"type\": \"button\"," +
                "                    \"style\": \"link\"," +
                "                    \"height\": \"sm\"," +
                "                    \"margin\": \"none\"," +
                "                    \"position\": \"relative\"," +
                "                    \"action\": {" +
                "                        \"type\": \"uri\"," +
                "                        \"label\": \"footerLinkText\"," +
                "                        \"uri\": \"footerLinkUrl\"" +
                "                    }" +
                "                }," +
                "                {" +
                "                    \"type\": \"spacer\"," +
                "                    \"size\": \"sm\"" +
                "                }" +
                "            ]," +
                "            \"action\": {" +
                "                \"type\": \"uri\"," +
                "                \"label\": \"action\"," +
                "                \"uri\": \"footerLinkUrl\"" +
                "            }" +
                "        }," +
                "        \"styles\": {" +
                "            \"hero\": {" +
                "                \"separator\": true" +
                "            }," +
                "            \"body\": {" +
                "                \"separator\": false" +
                "            }," +
                "            \"footer\": {" +
                "                \"separator\": true" +
                "            }" +
                "        }" +
                "    }" +
                "}";
    }

    /**
     * 取得ButtonTemplateJson
     * 供取代DefaultTemplateJson中buttonJsonArea的部分
     *
     * @return Default Button Template Json
     */
    public static String fetchDefaultButtonTemplateJson() {
        return ",{" +
                "    \"type\": \"box\"," +
                "    \"layout\": \"vertical\"," +
                "    \"contents\": [" +
                "        {" +
                "            \"type\": \"button\"," +
                "            \"action\": {" +
                "                \"type\": \"uri\"," +
                "                \"label\": \"bodyButtonText\"," +
                "                \"uri\": \"bodyLinkUrl\"" +
                "            }," +
                "            \"style\": \"primary\"," +
                "            \"color\": \"bodyButtonColor\"" +
                "        }" +
                "    ]," +
                "    \"margin\": \"none\"," +
                "    \"paddingBottom\": \"10px\"" +
                "}";
    }
}
