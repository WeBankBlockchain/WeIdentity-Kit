package com.webank.weid.kit.amop.request;

import com.webank.weid.protocol.base.Challenge;
import lombok.Getter;
import lombok.Setter;

import com.webank.weid.kit.amop.base.AmopBaseMsgArgs;

/**
 * args for getting weIdAuth object.
 * @author tonychen 2020年3月10日
 */
@Getter
@Setter
public class GetWeIdAuthArgs extends AmopBaseMsgArgs {

    /**
     * self weId.
     */
    private String weId;

    /**
     * the challenge.
     */
    private Challenge challenge;

    /**
     * 0:single, 1:mutual.
     */
    private Integer type;
}
