package com.home.service.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.home.service.Service.OrderService;
import com.home.service.Service.OrderService.OrderDTO;
import com.home.service.Service.OrderService.DetailedOrderDTO;
import com.home.service.models.Order;
import com.home.service.models.enums.OrderStatus;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<DetailedOrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        DetailedOrderDTO order = orderService.createOrder(orderDTO, currentUserId);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailedOrderDTO> getOrderById(@PathVariable Long id) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        DetailedOrderDTO order = orderService.getOrderById(id, currentUserId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer")
    public ResponseEntity<Page<DetailedOrderDTO>> getCustomerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        Page<DetailedOrderDTO> orders = orderService.getCustomerOrders(currentUserId, page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<Page<DetailedOrderDTO>> getBusinessOrders(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        Page<DetailedOrderDTO> orders = orderService.getBusinessOrders(businessId, currentUserId, page, size);
        return ResponseEntity.ok(orders);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{id}/status")
    public ResponseEntity<DetailedOrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        // String currentUserId =
        // SecurityContextHolder.getContext().getAuthentication().getName();
        Long currentUserId = 5L;
        DetailedOrderDTO order = orderService.updateOrderStatus(id, status, currentUserId);
        return ResponseEntity.ok(order);
    }
}