package com.webank.weid.kit.amop.inner;

import com.webank.weid.util.PropertyUtils;
import com.webank.weid.service.impl.WeIdServiceImpl;
import com.webank.weid.service.rpc.WeIdService;
import com.webank.weid.suite.persistence.Persistence;
import com.webank.weid.suite.persistence.PersistenceFactory;
import com.webank.weid.suite.persistence.PersistenceType;

public abstract class InnerService {
    
    private Persistence dataDriver;

    private PersistenceType persistenceType;

    private WeIdService weidService;
    
    protected WeIdService getWeIdService() {
        if (weidService == null) {
            weidService = new WeIdServiceImpl();
        }
        return weidService;
    }

    protected Persistence getDataDriver() {
        String type = PropertyUtils.getProperty("persistence_type");
        if (type.equals("mysql")) {
            persistenceType = PersistenceType.Mysql;
        } else if (type.equals("redis")) {
            persistenceType = PersistenceType.Redis;
        }
        if (dataDriver == null) {
            dataDriver = PersistenceFactory.build(persistenceType);
        }
        return dataDriver;
    }
}
