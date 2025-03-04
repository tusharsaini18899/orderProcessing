package com.example.orderProcessing.repository;

import com.example.orderProcessing.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    long countByStatus(String status);
}
