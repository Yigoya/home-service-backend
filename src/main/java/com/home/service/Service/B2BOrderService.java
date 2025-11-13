package com.home.service.Service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.home.service.dto.B2BOrderDTO;
import com.home.service.models.B2BOrder;
import com.home.service.models.Business;
import com.home.service.models.Product;
import com.home.service.models.enums.B2BOrderStatus;
import com.home.service.repositories.B2BOrderRepository;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;

@Service
public class B2BOrderService {

    private static final Logger logger = LoggerFactory.getLogger(B2BOrderService.class);

    @Autowired
    private B2BOrderRepository b2bOrderRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public B2BOrderDTO createOrder(B2BOrderDTO orderDTO) {
        logger.info("Creating B2B order for product ID: {}", orderDTO.getProductId());
        Business buyer = businessRepository.findById(orderDTO.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Buyer business not found with ID: " + orderDTO.getBuyerId()));
        Business seller = businessRepository.findById(orderDTO.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Seller business not found with ID: " + orderDTO.getSellerId()));
        Product product = productRepository.findById(orderDTO.getProductId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Product not found with ID: " + orderDTO.getProductId()));

        if (orderDTO.getQuantity() < product.getMinOrderQuantity()) {
            throw new ValidationException("Quantity is below minimum order quantity: " + product.getMinOrderQuantity());
        }

        if (orderDTO.getQuantity() > product.getStockQuantity()) {
            throw new ValidationException("Quantity exceeds available stock: " + product.getStockQuantity());
        }

        B2BOrder order = new B2BOrder();
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setProduct(product);
        order.setQuantity(orderDTO.getQuantity());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setStatus(B2BOrderStatus.PENDING);
        order.setShippingDetails(orderDTO.getShippingDetails());
        order.setOrderDate(LocalDateTime.now());

        // Update stock quantity
        product.setStockQuantity(product.getStockQuantity() - orderDTO.getQuantity());
        productRepository.save(product);

        B2BOrder savedOrder = b2bOrderRepository.save(order);
        logger.info("B2B order created with ID: {}", savedOrder.getId());
        return mapOrderEntityToDTO(savedOrder);
    }

    public Page<B2BOrderDTO> getOrdersByBusiness(Long businessId, B2BOrderStatus status, Pageable pageable) {
        logger.info("Fetching B2B orders for business ID: {}, status: {}", businessId, status);
        businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with ID: " + businessId));

        Page<B2BOrder> orders;
        if (status != null) {
            orders = b2bOrderRepository.findBySellerIdAndStatus(businessId, status, pageable);
        } else {
            orders = b2bOrderRepository.findBySellerIdOrBuyerId(businessId, businessId, pageable);
        }
        return orders.map(this::mapOrderEntityToDTO);
    }

    @Transactional
    public B2BOrderDTO updateOrderStatus(Long id, B2BOrderStatus status) {
        logger.info("Updating B2B order status for ID: {} to {}", id, status);
        B2BOrder order = b2bOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("B2B order not found with ID: " + id));

        order.setStatus(status);
        B2BOrder updatedOrder = b2bOrderRepository.save(order);
        logger.info("B2B order status updated for ID: {}", updatedOrder.getId());
        return mapOrderEntityToDTO(updatedOrder);
    }

    private B2BOrderDTO mapOrderEntityToDTO(B2BOrder entity) {
        B2BOrderDTO dto = new B2BOrderDTO();
        dto.setId(entity.getId());
        dto.setBuyerId(entity.getBuyer().getId());
        dto.setSellerId(entity.getSeller().getId());
        dto.setProductId(entity.getProduct().getId());
        dto.setQuantity(entity.getQuantity());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setStatus(entity.getStatus());
        dto.setShippingDetails(entity.getShippingDetails());
        dto.setOrderDate(entity.getOrderDate());
        return dto;
    }
}
