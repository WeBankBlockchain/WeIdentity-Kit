

package com.webank.weid.kit.transportation.impl;

import java.lang.reflect.Method;
import java.util.List;

import com.webank.weid.constant.DataDriverConstant;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.protocol.inf.JsonSerializer;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.transportation.entity.ProtocolProperty;
import com.webank.weid.kit.transportation.entity.TransMode;
import com.webank.weid.protocol.base.WeIdAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.weid.kit.transportation.inf.JsonTransportation;
import com.webank.weid.util.DataToolUtils;

/**
 * 二维码传输协议抽象类定义.
 * @author v_wbgyang
 *
 */
public abstract class AbstractJsonTransportation 
    extends AbstractTransportation
    implements JsonTransportation {
    
    private static final Logger logger =
        LoggerFactory.getLogger(AbstractJsonTransportation.class);
    
    @Override
    public JsonTransportation specify(List<String> verifierWeIdList) {
        this.setVerifier(verifierWeIdList);
        return this;
    }
    
    @Override
    public <T extends JsonSerializer> ResponseData<String> serialize(
        WeIdAuthentication weIdAuthentication,
        T object,
        ProtocolProperty property
    ) {
        ResponseData<String> response = serializeInner(object, property);
        if (response.getErrorCode().intValue() == KitErrorCode.SUCCESS.getCode()
            && property.getTransMode() == TransMode.DOWNLOAD_MODE) {
            super.registerWeIdAuthentication(weIdAuthentication);
        }
        return response;
    }
    
    protected abstract <T extends JsonSerializer> ResponseData<String> serializeInner(
        T object, 
        ProtocolProperty property
    );
    
    protected void saveTransData(String id, Object data) {
        com.webank.weid.blockchain.protocol.response.ResponseData<Integer> save = getDataDriver().add(
            DataDriverConstant.DOMAIN_RESOURCE_INFO,
            id,
                DataToolUtils.serialize(data)
        );
        if (save.getErrorCode().intValue() != KitErrorCode.SUCCESS.getCode()) {
            throw new WeIdBaseException(com.webank.weid.blockchain.constant.ErrorCode.getTypeByErrorCode(save.getErrorCode()));
        }
    }
    
    protected <T extends JsonSerializer> ResponseData<T> buildObject(
        String jsonString,
        Class<T> clazz
    ) throws Exception {
        
        String jsonData = DataToolUtils.convertUtcToTimestamp(jsonString);
        String jsonDataNew = jsonData;
        if (DataToolUtils.isValidFromToJson(jsonData)) {
            jsonDataNew = DataToolUtils.removeTagFromToJson(jsonData);
        }
        T object = null;
        Method method = getFromJsonMethod(clazz);
        if (method == null) {
            //调用工具的反序列化 
            object = (T) DataToolUtils.deserialize(jsonDataNew, clazz);
        } else  {
            object = (T) method.invoke(null, jsonDataNew);
        }
        logger.info("[deserialize] Transportation deserialize finished.");
        return new ResponseData<T>(object, KitErrorCode.SUCCESS);
    }
}
