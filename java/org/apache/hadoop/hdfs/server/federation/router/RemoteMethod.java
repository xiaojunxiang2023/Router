package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.CacheDirectiveInfo;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

// 对于 RPC调用中"方法"的描述
public class RemoteMethod {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteMethod.class);


    // 参数值
    private final Object[] params;
    // 参数类型
    private final Class<?>[] types;

    // RPC协议
    private final Class<?> protocol;
    private final String methodName;

    public RemoteMethod(String method) {
        this(ClientProtocol.class, method);
    }

    public RemoteMethod(Class<?> proto, String method) {
        this.params = null;
        this.types = null;
        this.methodName = method;
        this.protocol = proto;
    }

    public RemoteMethod(String method, Class<?>[] pTypes, Object... pParams)
            throws IOException {
        this(ClientProtocol.class, method, pTypes, pParams);
    }

    public RemoteMethod(Class<?> proto, String method, Class<?>[] pTypes, Object... pParams) throws IOException {

        if (pParams.length != pTypes.length) {
            throw new IOException("Invalid parameters for method " + method);
        }

        this.protocol = proto;
        this.params = pParams;
        this.types = Arrays.copyOf(pTypes, pTypes.length);
        this.methodName = method;
    }

    public Class<?> getProtocol() {
        return this.protocol;
    }

    public Method getMethod() throws IOException {
        try {
            if (types != null) {
                return protocol.getDeclaredMethod(methodName, types);
            } else {
                return protocol.getDeclaredMethod(methodName);
            }
        } catch (NoSuchMethodException e) {
            LOG.error("Cannot get method {} with types {} from {}",
                    methodName, Arrays.toString(types), protocol.getSimpleName(), e);
            throw new IOException(e);   // 技巧：包装 Exception的写法
        } catch (SecurityException e) {
            LOG.error("Cannot access method {} with types {} from {}",
                    methodName, Arrays.toString(types), protocol.getSimpleName(), e);
            throw new IOException(e);
        }
    }

    public Class<?>[] getTypes() {
        return Arrays.copyOf(this.types, this.types.length);
    }

    public Object[] getParams() {
        return this.getParams(null);
    }

    public String getMethodName() {
        return this.methodName;
    }

    // ?何意
    public Object[] getParams(RemoteLocationContext context) {
        if (this.params == null) {
            return new Object[]{};
        }
        Object[] objList = new Object[this.params.length];
        for (int i = 0; i < this.params.length; i++) {
            Object currentObj = this.params[i];
            if (currentObj instanceof RemoteParam) {
                RemoteParam paramGetter = (RemoteParam) currentObj;
                // Map the parameter using the context
                if (this.types[i] == CacheDirectiveInfo.class) {
                    CacheDirectiveInfo path = (CacheDirectiveInfo) paramGetter.getParameterForContext(context);
                    objList[i] = new CacheDirectiveInfo.Builder(path).setPath(new Path(context.getDest())).build();
                } else {
                    objList[i] = paramGetter.getParameterForContext(context);
                }
            } else {
                objList[i] = currentObj;
            }
        }
        return objList;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(this.protocol.getSimpleName())
                .append("#")
                .append(this.methodName)
                .append("(")
                .append(Arrays.deepToString(this.params))
                .append(")")
                .toString();
    }
}
