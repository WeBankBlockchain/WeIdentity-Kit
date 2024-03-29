

package com.webank.weid.kit.transportation.entity;

import com.webank.weid.blockchain.constant.ErrorCode;
import com.webank.weid.kit.constant.KitErrorCode;
import com.webank.weid.kit.exception.ProtocolSuiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 协议版本V1.
 * @author v_wbgyang
 *
 */
public class QrCodeVersion2 extends TransCodeBaseData {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeVersion2.class);
    
    private static final QrCodeVersion version = QrCodeVersion.V2;
    
    // 协议数据位置定义
    private static final int VERSION_INDEX = 0;
    private static final int CODE_INDEX = 1;
    private static final int RESOURCEID_INDEX = 2;
    
    public QrCodeVersion2() {
        this.setVersion(version.getCode());
    }
    
    /**
     * 获取协议数据.
     * 协议模板: version|encode+uritype+backup+orgId|resourceId
     * @return 返回协议字符串
     */
    @Override
    public String buildCodeString() {
        StringBuffer buffer = new StringBuffer();
        // 第一段 控制协议版本
        buffer.append(version.getCode()).append(PROTOCOL_PARTITION);
        // 第二段 控制协议编解码相关
        buffer.append(super.getEncodeType()).append(super.getUriTypeCode()).append("00")
            .append(super.getAmopId()).append(PROTOCOL_PARTITION);
        // 第三段 控制资源Id
        buffer.append(super.getId());
        return buffer.toString();
    }
    
    /**
     * 通过协议数据解析基本的协议对象.
     * 
     * @param codeString 协议字符串
     */
    public void buildCodeData(String codeString) {
        String[] codeStrings = codeString.split(PARTITION_FOR_SPLIT);
        if (codeStrings.length != 3) {
            logger.error("[buildCodeData] the field of protocol invalid.");
            throw new ProtocolSuiteException(ErrorCode.TRANSPORTATION_PROTOCOL_FIELD_INVALID);
        }
        try {
            // 解析第一段版本号
            String version = codeStrings[VERSION_INDEX];
            this.setVersion(Integer.parseInt(version));
            
            // 解析第二段相关
            String enCodeString = codeStrings[CODE_INDEX];
            
            // 第一个字符为encode
            String enCodeTypeStr = enCodeString.substring(0, 1);
            this.setEncodeType(Integer.parseInt(enCodeTypeStr));
            
            // 第二个字符为uriCode
            String uriCodeStr = enCodeString.substring(1, 2);
            this.setUriTypeCode(Integer.parseInt(uriCodeStr));
            
            // 第三/四个字符为备份字段 不解析
            
            // 第五个字符开始为orgId(后续有uriCode决定)
            String amopId = enCodeString.substring(4);
            this.setAmopId(amopId);
            // 解析第三段 资源id
            this.setId(codeStrings[RESOURCEID_INDEX]);
        } catch (Exception e) {
            logger.error("[buildCodeData] the protocol string invalid.", e);
            throw new ProtocolSuiteException(ErrorCode.TRANSPORTATION_PROTOCOL_STRING_INVALID);
        }
    }
}
