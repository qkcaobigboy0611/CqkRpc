/**
 * @author qkcao
 * @date 2023/7/26 11:09
 */
package com.cqk.test.server;

import com.cqk.test.server.bean.Person;

public interface IHelloRPC {
    String sayHello();
    String sayHello(Person person);
}
