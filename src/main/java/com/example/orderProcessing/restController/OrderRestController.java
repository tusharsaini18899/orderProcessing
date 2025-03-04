package com.example.orderProcessing.restController;


import com.example.orderProcessing.service.OrderService;
import com.example.orderProcessing.entity.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderRestController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public Orders placeOrder(@RequestBody Orders order) {
        return orderService.createOrder(order);
    }

    @GetMapping("/{orderId}")
    public Orders checkOrderStatus(@PathVariable Long orderId) {
        return orderService.getOrderStatus(orderId);
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        return orderService.getMetrics();
    }
}
