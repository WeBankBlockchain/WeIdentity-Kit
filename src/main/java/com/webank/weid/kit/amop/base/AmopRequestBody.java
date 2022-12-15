

package com.webank.weid.kit.amop.base;

import lombok.Data;

import com.webank.weid.kit.amop.entity.AmopMsgType;
import com.webank.weid.protocol.inf.IArgs;

@Data
public class AmopRequestBody implements IArgs {

    protected AmopMsgType msgType;

    protected String msgBody;
}
