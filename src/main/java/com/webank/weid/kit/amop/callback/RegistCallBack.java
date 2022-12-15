

package com.webank.weid.kit.amop.callback;

public interface RegistCallBack {
    
    public void registAmopCallback(Integer msgType, WeIdAmopCallback routeCallBack);
    
    public WeIdAmopCallback getAmopCallback(Integer msgType);
}
