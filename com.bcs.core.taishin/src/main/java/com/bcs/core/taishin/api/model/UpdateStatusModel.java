package com.bcs.core.taishin.api.model;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ???
 */
@Setter
@Getter
public class UpdateStatusModel extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;
    private String uid;
    private Long time;
    private String status;
    private List<UpdateStatusFieldModel> field = new ArrayList<>();
}
