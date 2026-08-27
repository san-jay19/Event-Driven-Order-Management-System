package com.example.order_service.service;


import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Order> redisTemplate;

    private static final String CACHE_PREFIX = "order:";

    public OrderService(
            OrderRepository orderRepository,
            RedisTemplate<String, Order> redisTemplate) {
        this.orderRepository = orderRepository;
        this.redisTemplate = redisTemplate;
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public Optional<Order> getOrder(Long id) {

        String key = CACHE_PREFIX + id;

        Order cachedOrder = redisTemplate.opsForValue().get(key);

        if (cachedOrder != null) {
            System.out.println("Redis Cache HIT: " + id);
            return Optional.of(cachedOrder);
        }

        System.out.println("Redis Cache MISS: " + id);

        Optional<Order> order = orderRepository.findById(id);

        order.ifPresent(value ->
                redisTemplate.opsForValue().set(
                        key,
                        value,
                        10,
                        TimeUnit.MINUTES
                )
        );

        return order;
    }
}