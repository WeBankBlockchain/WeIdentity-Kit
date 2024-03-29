

package com.webank.weid.kit.transportation.impl;

import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.kit.amop.request.GetTransDataArgs;
import com.webank.weid.kit.constant.ServiceType;
import com.webank.weid.kit.exception.ProtocolSuiteException;
import com.webank.weid.kit.transmission.TransmissionRequest;
import com.webank.weid.kit.transmission.entity.TransType;
import com.webank.weid.kit.transportation.entity.TransBaseData;
import com.webank.weid.kit.transportation.entity.TransCodeBaseData;
import com.webank.weid.kit.transportation.entity.TransMode;
import com.webank.weid.util.DataToolUtils;
import com.webank.weid.protocol.base.WeIdAuthentication;
import org.apache.commons.lang3.StringUtils;


/**
 * 二维码传输协议抽象类定义.
 * @author v_wbgyang
 *
 */
public abstract class AbstractCodeTransportation extends AbstractJsonTransportation {

    protected TransmissionRequest<GetTransDataArgs> buildRequest(
        TransType type, 
        TransCodeBaseData codeData,
        WeIdAuthentication weIdAuthentication
    ) {
        TransmissionRequest<GetTransDataArgs> request = new TransmissionRequest<>();
        request.setAmopId(codeData.getAmopId());
        request.setServiceType(ServiceType.SYS_GET_TRANS_DATA.name());
        request.setWeIdAuthentication(weIdAuthentication);
        request.setArgs(getCodeDataArgs(codeData, weIdAuthentication));
        request.setTransType(type);
        return request;
    }
    
    protected GetTransDataArgs getCodeDataArgs(
        TransCodeBaseData codeData, 
        WeIdAuthentication weIdAuthentication
    ) {
        GetTransDataArgs args = new GetTransDataArgs();
        args.setResourceId(codeData.getId());
        args.setTopic(codeData.getAmopId());
        args.setFromAmopId(com.webank.weid.blockchain.service.fisco.BaseServiceFisco.fiscoConfig.getAmopId());
        args.setWeId(weIdAuthentication.getWeId());
        args.setClassName(codeData.getClass().getName());
        /*String signValue = DataToolUtils.secp256k1Sign(
            codeData.getId(),
            new BigInteger(weIdAuthentication.getWeIdPrivateKey().getPrivateKey())
        );*/
        String signature = DataToolUtils.SigBase64Serialization(
                DataToolUtils.signToRsvSignature(codeData.getId(), weIdAuthentication.getWeIdPrivateKey().getPrivateKey())
        );
        args.setSignValue(signature);
        return args;
    }
    
    /**
     * 根据协议字符串判断协议为下载模式协议还是纯数据模式协议.
     * 
     * @param transString 协议字符串
     * @return 返回TransMode
     */
    protected TransMode getTransMode(String transString) {
        if (StringUtils.isBlank(transString)) {
            throw new ProtocolSuiteException(ErrorCode.TRANSPORTATION_PROTOCOL_STRING_INVALID);
        }
        String[] trans = transString.split(TransBaseData.PARTITION_FOR_SPLIT);
        if (trans.length == 3) {
            return TransMode.DOWNLOAD_MODE;
        }
        return TransMode.DATA_MODE;
    } 
}
