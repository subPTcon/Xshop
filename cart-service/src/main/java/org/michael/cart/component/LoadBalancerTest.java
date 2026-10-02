package org.michael.cart.component;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadBalancerTest {

    private final LoadBalancerClient loadBalancerClient;

    public void printProductInstance() {
        ServiceInstance instance = loadBalancerClient.choose("product-service");

        if (instance == null) {
            System.out.println("没有找到 product-service 实例");
            return;
        }

        System.out.println("Feign 实际选择的实例："
                + instance.getHost()
                + ":"
                + instance.getPort());
    }
}
