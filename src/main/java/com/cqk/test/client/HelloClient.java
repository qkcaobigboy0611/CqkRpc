/**
 * @author qkcao
 * @date 2023/7/29 20:35
 */
package com.cqk.test.client;

import com.cqk.client.RpcProxy;
import com.cqk.test.server.IHelloRPC;
import com.cqk.test.server.bean.Person;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloClient {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-client.xml");
        RpcProxy proxy = applicationContext.getBean(RpcProxy.class);

        IHelloRPC helloRPC = proxy.create(IHelloRPC.class, "sample.hello.person");
        Person person = new Person(26, "qkcao");
        String result = helloRPC.sayHello(person);
        System.out.println("result print:"+result);
    }
}
