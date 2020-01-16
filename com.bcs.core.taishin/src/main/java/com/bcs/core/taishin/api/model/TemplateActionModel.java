package com.bcs.core.taishin.api.model;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ???
 */
@Setter
@Getter
public class TemplateActionModel extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    private String actionLetter;
    private String actionType;
    private String actionLabel;
    private String actionData;
    private String actionText;
}
