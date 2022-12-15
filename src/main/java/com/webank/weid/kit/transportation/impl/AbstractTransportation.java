

package com.webank.weid.kit.transportation.impl;

import com.webank.weid.kit.auth.WeIdAuth;
import com.webank.weid.kit.auth.WeIdAuthCallback;
import com.webank.weid.kit.auth.WeIdAuthImpl;
import com.webank.weid.kit.auth.WeIdAuthObj;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.exception.WeIdBaseException;
import com.webank.weid.kit.transportation.entity.ProtocolProperty;
import com.webank.weid.protocol.base.WeIdAuthentication;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.rpc.WeIdService;
import com.webank.weid.suite.persistence.Persistence;
import com.webank.weid.suite.persistence.mysql.driver.MysqlDriver;
import com.webank.weid.util.WeIdUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTransportation {

    private static final Logger logger =
        LoggerFactory.getLogger(AbstractTransportation.class);
    private static WeIdService weidService = new WeIdServiceImpl();
    private List<String> verifierWeIdList;
    private static long lasttime = System.currentTimeMillis();
    private static AtomicInteger atomicInt = new AtomicInteger(0);
    private static final int maxSize = 1000;
    private static WeIdAuth weIdAuthService;
    private static Persistence dataDriver;

    protected WeIdAuth getWeIdAuthService() {
        if (weIdAuthService == null) {
            weIdAuthService = new WeIdAuthImpl();
        }
        return weIdAuthService;
    }

    protected Persistence getDataDriver() {
        if (dataDriver == null) {
            dataDriver = new MysqlDriver();
        }
        return dataDriver;
    }
    
    /**
     * WeIdAuth回调拿到WeIdAuthentication.
     * 
     * @param weIdAuthentication 用户身份
     */
    protected void registerWeIdAuthentication(WeIdAuthentication weIdAuthentication) {
        this.getWeIdAuthService().registerCallBack(new WeIdAuthCallback() {
            
            private WeIdAuthentication authentication;
            {
                authentication = new WeIdAuthentication(
                    weIdAuthentication.getWeId(), 
                    weIdAuthentication.getWeIdPrivateKey().getPrivateKey()
                );
            }
            
            @Override
            public WeIdAuthentication onChannelConnecting(String counterpartyWeId) {
                return authentication;
            }
            
            @Override
            public Integer onChannelConnected(WeIdAuthObj arg) {
                return null;
            }
        });
    }
    
    /**
     * 验证协议配置.
     *
     * @param property 协议配置实体
     * @return Error Code and Message
     */
    protected KitErrorCode checkEncodeProperty(ProtocolProperty property) {
        if (property == null) {
            return KitErrorCode.TRANSPORTATION_PROTOCOL_PROPERTY_ERROR;
        }
        if (property.getEncodeType() == null) {
            return KitErrorCode.TRANSPORTATION_PROTOCOL_ENCODE_ERROR;
        }
        if (property.getTransType() == null) {
            return  KitErrorCode.TRANSPORTATION_TRANSMISSION_TYPE_INVALID;
        }
        if (property.getUriType() == null) {
            return  KitErrorCode.TRANSPORTATION_URI_TYPE_INVALID;
        }
        return KitErrorCode.SUCCESS;
    }
    
    /**
     * 验证WeIdAuthentication有效性.
     *
     * @param weIdAuthentication 身份信息
     * @return Error Code and Message
     */
    protected KitErrorCode checkWeIdAuthentication(WeIdAuthentication weIdAuthentication) {
        if (weIdAuthentication == null
                || weIdAuthentication.getWeIdPrivateKey() == null
                || weIdAuthentication.getWeId() == null) {
            return KitErrorCode.WEID_AUTHORITY_INVALID;
        }
        if (!WeIdUtils.validatePrivateKeyWeIdMatches(
                weIdAuthentication.getWeIdPrivateKey(),
                weIdAuthentication.getWeId())) {
            return KitErrorCode.WEID_PRIVATEKEY_DOES_NOT_MATCH;
        }
        com.webank.weid.protocol.response.ResponseData<Boolean> isExists = weidService.isWeIdExist(weIdAuthentication.getWeId());
        if (!isExists.getResult()) {
            return KitErrorCode.WEID_DOES_NOT_EXIST;
        }
        return KitErrorCode.SUCCESS;
    }

    /**
     * 验证wrapper数据.
     *
     * @param obj wrapper数据,作为协议的rawData部分
     * @return Error Code and Message
     */
    protected KitErrorCode checkProtocolData(Object obj) {
        if (obj == null) {
            return KitErrorCode.TRANSPORTATION_PROTOCOL_DATA_INVALID;
        }
        return KitErrorCode.SUCCESS;
    }

    protected List<String> getVerifiers() {
        if (verifierWeIdList == null) {
            throw new WeIdBaseException(ErrorCode.TRANSPORTATION_NO_SPECIFYER_TO_SET);
        }
        return verifierWeIdList;
    }

    protected void setVerifier(List<String> verifierWeIdList) {
        if (this.verifierWeIdList != null) {
            String errorMessage = ErrorCode.THIS_IS_REPEATED_CALL.getCode() + " - "
                    + ErrorCode.THIS_IS_REPEATED_CALL.getCodeDesc();
            logger.error("[specify] {}.", errorMessage);
            throw new WeIdBaseException(errorMessage);
        }
        if (CollectionUtils.isEmpty(verifierWeIdList)) {
            String errorMessage = ErrorCode.ILLEGAL_INPUT.getCode() + " - "
                    + ErrorCode.ILLEGAL_INPUT.getCodeDesc();
            logger.error("[specify] {}, the verifiers is null.", errorMessage);
            throw new WeIdBaseException(errorMessage);
        }
        for (String weid : verifierWeIdList) {
            ResponseData<Boolean> isExists = weidService.isWeIdExist(weid);
            if (!isExists.getResult()) {
                String errorMessage = ErrorCode.WEID_DOES_NOT_EXIST.getCode() + " - "
                        + ErrorCode.WEID_DOES_NOT_EXIST.getCodeDesc();
                logger.error("[specify] {} , weid = {} .", errorMessage, weid);
                throw new WeIdBaseException(errorMessage);
            }
        }
        this.verifierWeIdList = verifierWeIdList;
    }

    protected Method getFromJsonMethod(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Method targetMethod = null;
        for (Method method : methods) {
            if (method.getName().equals("fromJson")
                    && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0] == String.class) {
                targetMethod = method;
            }
        }
        return targetMethod;
    }
    
    /**
     * 产生资源Id.
     * @return 返回资源Id
     */
    public static synchronized long nextId() {
        long time = System.currentTimeMillis();
        if (time == lasttime && atomicInt.get() == maxSize) {
            atomicInt = new AtomicInteger(0);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                logger.error("[nextId] sleep error.");
            }
            lasttime = System.currentTimeMillis();
            return nextId();
        }
        return time * maxSize + atomicInt.getAndIncrement();
    }
}
