package com.bcs.core.taishin.api.model;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ???
 */
@Getter
@Setter
public class UpdateStatusFieldModel extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private String key;
    private String value;
    private String type;
}
