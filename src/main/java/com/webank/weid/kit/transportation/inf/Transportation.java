

package com.webank.weid.kit.transportation.inf;

import com.webank.weid.protocol.inf.JsonSerializer;
import com.webank.weid.kit.protocol.response.ResponseData;
import com.webank.weid.kit.transportation.entity.ProtocolProperty;
import com.webank.weid.protocol.base.WeIdAuthentication;

import java.util.List;

/**
 * Transportation base.
 * @author yanggang
 *
 */
public interface Transportation {
    
    Transportation specify(List<String> verifierWeIdList);

    /**
     * 协议传输序列化接口.
     * 
     * @param object 协议存储的实体数据对象
     * @param <T> 需要转换成元素的类型泛型定义
     * @param property 协议的配置对象
     * @return 返回协议字符串数据
     */
    <T extends JsonSerializer> ResponseData<String> serialize(
        T object,
        ProtocolProperty property
    );
    
    /**
     * 协议传输序列化接口.
     * 
     * @param weIdAuthentication 用户身份信息
     * @param object 协议存储的实体数据对象
     * @param <T> 需要转换成元素的类型泛型定义
     * @param property 协议的配置对象
     * @return 返回协议字符串数据
     */
    <T extends JsonSerializer> ResponseData<String> serialize(
        WeIdAuthentication weIdAuthentication,
        T object,
        ProtocolProperty property
    );

    /**
     * 协议反序列化接口.
     * 
     * @param transString JSON格式的协议数据字符串
     * @param clazz 需要转换成的Class类型
     * @param <T> 需要转换成元素的类型泛型定义
     * @return 返回PresentationE对象数据
     */
    @Deprecated
    <T extends JsonSerializer> ResponseData<T> deserialize(
        String transString,
        Class<T> clazz
    );

    /**
     * 协议反序列化接口，支持权限控制.
     * 
     * @param weIdAuthentication 验证方当前authentication信息
     * @param transString JSON格式的协议数据字符串
     * @param clazz 需要转换成的Class类型
     * @param <T> 需要转换成元素的类型泛型定义
     * @return 返回PresentationE对象数据
     */
    <T extends JsonSerializer> ResponseData<T> deserialize(
        WeIdAuthentication weIdAuthentication,
        String transString,
        Class<T> clazz
    );
}
