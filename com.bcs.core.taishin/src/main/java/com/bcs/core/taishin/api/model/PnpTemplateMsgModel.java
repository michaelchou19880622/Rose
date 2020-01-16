package com.bcs.core.taishin.api.model;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author ???
 */
@Setter
@Getter
public class PnpTemplateMsgModel extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    private boolean templateSwitch;
    private String altText;
    private String templateType;
    private String templateImageId;
    private String templateTitle;
    private String curfewStartTime;
    private String curfewEndTime;
    private String templateText;
    private String templateLevel;
    private String templateParentId;
    private String templateLetter;
    private List<TemplateActionModel> templateActions;
}
