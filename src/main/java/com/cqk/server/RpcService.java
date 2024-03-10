package com.cqk.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description
 * @author 曹庆魁
 * @since 1.0.0
 * @datetime 2023年7月26日 上午10:57:39
 */
@Target(ElementType.TYPE) //表示这个注解可以修饰的地方：类，接口，枚举
@Retention(RetentionPolicy.RUNTIME) // 什么时候起作用:运行时起作用
@Component
public @interface RpcService {
    /**
     * 服务接口类
     * @return
     */
    Class<?> value();
    /**
     * 服务版本号
     * @return
     */
    String version() default "";
}
