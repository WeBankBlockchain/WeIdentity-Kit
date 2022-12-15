

package com.webank.weid.kit.amop.base;

import com.webank.weid.protocol.base.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.webank.weid.protocol.inf.IArgs;

/**
 * Created by junqizhang on 01/06/2017.
 */
@Data
@BlockChainDto(bindType = BlockChainDto.BindTypeEnum.Object)
@EqualsAndHashCode(callSuper = false)
public class AmopBaseMsgArgs implements IArgs {

    /**
     * sdk functions version.
     */
    protected Version version;

    /**
     * 消息id，用于链上链下消息去重.
     */
    protected String messageId;

    /**
     * 来源AmopId.
     */
    protected String fromAmopId;

    /**
     * topic.
     */
    protected String topic;
    
    /**
     * 业务类型.
     */
    protected String serviceType;
    
    /**
     * 通道编号.
     */
    protected String channelId;


}
