


package com.webank.weid.kit.amop.request;

import lombok.Getter;
import lombok.Setter;

import com.webank.weid.kit.amop.base.AmopBaseMsgArgs;

/**
 * the request body for get EncryptKey.
 * 
 * @author tonychen 2019年5月7日.
 *
 */
@Getter
@Setter
public class GetEncryptKeyArgs extends AmopBaseMsgArgs {

    /**
     * the encrypKey Id.
     */
    private String keyId;
    
    /**
     * weId信息.
     */
    private String weId;
    
    /**
     * 签名信息.
     */
    private String signValue;
    
    /**
     * 扩展字符串字段.
     */
    private String extra;
}
