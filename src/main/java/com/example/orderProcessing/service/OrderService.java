package com.example.orderProcessing.service;

import com.example.orderProcessing.utils.Common;
import com.example.orderProcessing.entity.Orders;
import com.example.orderProcessing.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.example.orderProcessing.utils.Common.CRON_JOB_TIME;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;
    private final Queue<Orders> orderQueue = new ConcurrentLinkedQueue<>();
    private final Map<Long, LocalDateTime> orderProcessingTimes = new ConcurrentHashMap<>();

    public Orders createOrder(Orders order) {
        order.setStatus(Common.PENDING);
        orderQueue.add(order);
        Orders orders = orderRepository.save(order);
        log.info("Order with id: {} added successfully", orders.getOrderId());
        return orders;
    }

    public Orders getOrderStatus(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public Map<String, Object> getMetrics() {
        long completed = countByStatus(Common.COMPLETED);
        long pending = countByStatus(Common.PENDING);
        long processing = countByStatus(Common.PROCESSING);
        double avgProcessingTime = orderProcessingTimes.values().stream()
                .mapToDouble(t -> Duration.between(t, LocalDateTime.now()).toSeconds())
                .average()
                .orElse(0.0);
        return metricsResponse(completed, pending, processing, avgProcessingTime);
    }

    @Scheduled(fixedRate = CRON_JOB_TIME)
    public void processOrders() {
        log.info("*****************************Scheduler invoked*****************************");
        Orders order = orderQueue.poll();
        if (order != null) {
            order.setStatus(Common.PROCESSING);
            log.info("Processing order id is {}",order.getOrderId());
            orderRepository.save(order);
            try {
                Thread.sleep(1000); // Simulate processing time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            order.setStatus(Common.COMPLETED);
            order.setProcessedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order with id: {} completed!",order.getOrderId());
            orderProcessingTimes.put(order.getOrderId(), order.getProcessedAt());
        }
    }

    public void enqueueOrder(Orders order) {
        orderQueue.add(order);
    }

    private long countByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    private Map<String, Object> metricsResponse(long completed, long pending, long processing, double avgProcessingTime) {
        Map<String, Object> response = new HashMap<>();
        response.put("totalProcessed", completed);
        response.put("pending", pending);
        response.put("processing", processing);
        response.put("avgProcessingTime", avgProcessingTime);
        return response;
    }
}
