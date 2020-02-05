package com.bcs.web.ui.service;

import com.bcs.core.taishin.circle.pnp.db.entity.PnpFlexTemplate;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpFlexTemplateRepository;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Pnp Flex Template Service
 *
 * @author Alan
 */
@Slf4j
@Service
public class PnpFlexTemplateService {

    private PnpFlexTemplateRepository pnpFlexTemplateRepository;

    @Autowired
    public PnpFlexTemplateService(PnpFlexTemplateRepository pnpFlexTemplateRepository) {
        this.pnpFlexTemplateRepository = pnpFlexTemplateRepository;

    }

    /**
     * Example:
     * {
     * "type": "flex",
     * "pathwayCode": "3",
     * "pathwayName": "BC->PNP->SMS",
     * "template": "Default Template",
     * "header": {
     * "headerBackground": "#FF0000",
     * "headerTextSize": "xl",
     * "headerTextColor": "#000000",
     * "headerTextWeight": "bold",
     * "headerTextStyle": "italic",
     * "headerTextDecoration": "none",
     * "headerText": "Title"
     * },
     * "hero": {
     * "heroBackground": "#9CFFE4",
     * "heroTextSize": "lg",
     * "heroTextColor": "#215CFF",
     * "heroTextWeight": "regular",
     * "heroTextStyle": "italic",
     * "heroTextDecoration": "underline",
     * "heroText": "445fff"
     * },
     * "body": {
     * "bodyDescTextSize": "xs",
     * "bodyDescTextColor": "#666666",
     * "bodyDescTextWeight": "regular",
     * "bodyDescTextStyle": "normal",
     * "bodyDescTextDecoration": "none",
     * "bodyDescText": "此通知在用戶簽約時所註冊之電話號碼和LINE上所註冊之電話號碼一致時發送。",
     * "bodyBackground": "#FFFFFF"
     * },
     * "footer": {
     * "footerLinkText": "收到此訊息的原因是？",
     * "footerLinkUrl": "https://linecorp.com"
     * },
     * "button": [
     * {
     * "id": "1",
     * "label": "Click!!",
     * "uri": "http://www.google.com",
     * "color": "#59E3FF"
     * },
     * {
     * "id": "2",
     * "label": "Click2!!",
     * "uri": "http://www.google.com",
     * "color": "#47FFA9"
     * }
     * ]
     * }
     *
     * @param templateMap templateMap
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public PnpFlexTemplate saveTemplate(Map<String, Object> templateMap) {
        PnpFlexTemplate pnpFlexTemplate;

        Long id;
        if (templateMap.containsKey("template")) {
            try {
                id = Long.parseLong((String) templateMap.get("template"));
                pnpFlexTemplate = pnpFlexTemplateRepository.findOne(id);
            } catch (NumberFormatException ne) {
                pnpFlexTemplate = new PnpFlexTemplate();
            }
        } else {
            pnpFlexTemplate = new PnpFlexTemplate();
        }

        if (templateMap.containsKey("header")) {
            Map<String, String> headerMap = (Map<String, String>) templateMap.get("header");
            pnpFlexTemplate.setHeaderText(headerMap.get("headerText"));
            pnpFlexTemplate.setHeaderBackground(headerMap.get("headerBackground"));
            pnpFlexTemplate.setHeaderTextColor(headerMap.get("headerTextColor"));
            pnpFlexTemplate.setHeaderTextDecoration(headerMap.get("headerTextDecoration"));
            pnpFlexTemplate.setHeaderTextSize(headerMap.get("headerTextSize"));
            pnpFlexTemplate.setHeaderTextStyle(headerMap.get("headerTextStyle"));
            pnpFlexTemplate.setHeaderTextWeight(headerMap.get("headerTextWeight"));
        }
        if (templateMap.containsKey("hero")) {
            Map<String, String> heroMap = (Map<String, String>) templateMap.get("hero");
            pnpFlexTemplate.setHeroText(heroMap.get("heroText"));
            pnpFlexTemplate.setHeroBackground(heroMap.get("heroBackground"));
            pnpFlexTemplate.setHeroTextColor(heroMap.get("heroTextColor"));
            pnpFlexTemplate.setHeroTextDecoration(heroMap.get("heroTextDecoration"));
            pnpFlexTemplate.setHeroTextSize(heroMap.get("heroTextSize"));
            pnpFlexTemplate.setHeroTextStyle(heroMap.get("heroTextStyle"));
            pnpFlexTemplate.setHeroTextWeight(heroMap.get("heroTextWeight"));
        }
        if (templateMap.containsKey("body")) {
            Map<String, String> bodyMap = (Map<String, String>) templateMap.get("body");
            pnpFlexTemplate.setBodyBackground(bodyMap.get("bodyBackground"));
            pnpFlexTemplate.setBodyDescText(bodyMap.get("bodyDescText"));
            pnpFlexTemplate.setBodyDescTextColor(bodyMap.get("bodyDescTextColor"));
            pnpFlexTemplate.setBodyDescTextDecoration(bodyMap.get("bodyDescTextDecoration"));
            pnpFlexTemplate.setBodyDescTextSize(bodyMap.get("bodyDescTextSize"));
            pnpFlexTemplate.setBodyDescTextStyle(bodyMap.get("bodyDescTextStyle"));
            pnpFlexTemplate.setBodyDescTextWeight(bodyMap.get("bodyDescTextWeight"));
        }
        if (templateMap.containsKey("footer")) {
            Map<String, String> footerMap = (Map<String, String>) templateMap.get("footer");
            pnpFlexTemplate.setFooterLinkText(footerMap.get("footerLinkText"));
            pnpFlexTemplate.setFooterLinkUrl(footerMap.get("footerLinkUrl"));
        }
        if (templateMap.containsKey("button")) {
            List<Map<String, String>> buttonMapList = (List<Map<String, String>>) templateMap.get("button");

            StringBuilder btnLabelList = new StringBuilder();
            StringBuilder btnColorList = new StringBuilder();
            StringBuilder btnUrlList = new StringBuilder();
            for (Map<String, String> btnMap : buttonMapList) {
                if (btnLabelList.length() != 0) {
                    btnLabelList.append(',');
                }
                if (btnColorList.length() != 0) {
                    btnColorList.append(',');
                }
                if (btnUrlList.length() != 0) {
                    btnUrlList.append(',');
                }
                btnLabelList.append(btnMap.get("label"));
                btnColorList.append(btnMap.get("color"));
                btnUrlList.append(btnMap.get("uri"));
            }
            pnpFlexTemplate.setButtonText(btnLabelList.toString());
            pnpFlexTemplate.setButtonColor(btnColorList.toString());
            pnpFlexTemplate.setButtonUrl(btnUrlList.toString());
        }

        log.info(DataUtils.toPrettyJsonUseJackson(pnpFlexTemplate));
        PnpFlexTemplate afterSavePnpFlexTemplate = pnpFlexTemplateRepository.save(pnpFlexTemplate);
        if (afterSavePnpFlexTemplate == null || afterSavePnpFlexTemplate.getId() == null) {
            return null;
        }
        return afterSavePnpFlexTemplate;
    }

    public PnpFlexTemplate getTemplate(Long templateId) {
        PnpFlexTemplate pnpFlexTemplate = pnpFlexTemplateRepository.findOne(templateId);
        log.info("pnpFlexTemplate: {}", DataUtils.toPrettyJsonUseJackson(pnpFlexTemplate));
        return pnpFlexTemplate;
    }
}
