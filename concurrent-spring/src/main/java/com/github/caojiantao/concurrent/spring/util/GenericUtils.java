package com.github.caojiantao.concurrent.spring.util;

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

public class GenericUtils {

    /**
     * 获取实现指定接口的，第一个泛型
     *
     * @param obj 对象实例
     * @return 泛型
     */
    public static Class<?> getInterfaceGeneric(Object obj, Class<?> interfaceClass) {
        ResolvableType[] interfaces = ResolvableType.forInstance(obj).getInterfaces();
        ResolvableType handleType = null;
        for (ResolvableType resolvableType : interfaces) {
            if (Objects.equals(resolvableType.toClass(), interfaceClass)) {
                handleType = resolvableType;
                break;
            }
        }
        if (Objects.isNull(handleType) || ObjectUtils.isEmpty(handleType.getGeneric())) {
            return null;
        }
        return handleType.getGeneric(0).toClass();
    }
}
