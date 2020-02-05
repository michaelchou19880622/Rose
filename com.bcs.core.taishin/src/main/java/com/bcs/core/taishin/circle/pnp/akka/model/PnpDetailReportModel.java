package com.bcs.core.taishin.circle.pnp.akka.model;

import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author ???
 */
@Getter
@Setter
public class PnpDetailReportModel extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    private Long pnpDetailId;
    private Long pnpMainId;
    private String uid;
    private String phone;
    private String msg;
    private String source;
    private String scheduleTime;
    private Date sendTime;
}

