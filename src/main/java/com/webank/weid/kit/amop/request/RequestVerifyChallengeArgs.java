package com.webank.weid.kit.amop.request;

import com.webank.weid.protocol.base.Challenge;
import lombok.Getter;
import lombok.Setter;

import com.webank.weid.kit.amop.base.AmopBaseMsgArgs;

/**
 * args for verify challenge.
 * @author tonychen 2020年3月12日
 */
@Getter
@Setter
public class RequestVerifyChallengeArgs extends AmopBaseMsgArgs {

    private String channelId;
    private String selfWeId;
    private Challenge challenge;
    private String signData;
}
