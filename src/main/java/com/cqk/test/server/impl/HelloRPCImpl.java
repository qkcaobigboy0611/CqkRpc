/**
 * @author qkcao
 * @date 2023/7/26 15:35
 */
package com.cqk.test.server.impl;

import com.cqk.server.RpcService;
import com.cqk.test.server.IHelloRPC;
import com.cqk.test.server.bean.Person;

@RpcService(value = IHelloRPC.class, version = "sample.hello.person")
public class HelloRPCImpl implements IHelloRPC {
    @Override
    public String sayHello() {
        return "woowowo.success.sayHello.";
    }

    @Override
    public String sayHello(Person person) {
        return person.getName()+","+person.getAge();
    }
}
