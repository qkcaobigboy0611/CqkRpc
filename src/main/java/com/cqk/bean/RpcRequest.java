/**
 * @author qkcao
 * @date 2023/7/28 17:39
 */
package com.cqk.bean;

import java.util.Arrays;

/**
 * RPC请求
 */
public class RpcRequest {
    /**
     * @param requestId 请求唯一标识符
     * @param interfaceName 接口名字
     * @param serviceVersion
     * @param methodName 方法名
     * @param parameterTypes 参数类型
     * @param parameters 参数
     */
    private RpcRequest(String requestId, String interfaceName,
                       String serviceVersion, String methodName,
                       Class<?>[] parameterTypes, Object[] parameters) {
        super();
        this.requestId = requestId;
        this.interfaceName = interfaceName;
        this.serviceVersion = serviceVersion;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
    }
    private String requestId;
    private String interfaceName;
    private String serviceVersion;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }
    /**
     * @return the interfaceName
     */
    public String getInterfaceName() {
        return interfaceName;
    }
    /**
     * @return the serviceVersion
     */
    public String getServiceVersion() {
        return serviceVersion;
    }
    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }
    /**
     * @return the parameterTypes
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
    /**
     * @return the parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "requestId["+this.requestId+"] "
                +"interfaceName["+this.interfaceName+"] "
                +"serviceVersion["+this.serviceVersion+"] "
                +"methodName["+this.methodName+"] "
                +"parameterTypes["+ Arrays.toString(this.parameterTypes)+"] "
                +"parameters["+Arrays.toString(this.parameters)+"].";

    }

    public static class RequestBuilder{
        private final String requestId;
        private final String interfaceName;
        private final String methodName;
        private String serviceVersion;
        private Class<?>[] parameterTypes;
        private Object[] parameters;

        public RequestBuilder(String requestId, String interfaceName, String methodName,
                              String serviceVersion, Class<?>[] parameterTypes, Object[] parameters) {
            this.requestId = requestId;
            this.interfaceName = interfaceName;
            this.methodName = methodName;
            this.serviceVersion = serviceVersion;
            this.parameterTypes = parameterTypes;
            this.parameters = parameters;
        }

        public RpcRequest create(){
            return new RpcRequest(requestId, interfaceName, serviceVersion, methodName, parameterTypes, parameters);
        }
    }
}
