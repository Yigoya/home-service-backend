package com.home.service.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.Service.BusinessLocationService.BusinessLocationDTO;
import com.home.service.models.Address;
import com.home.service.models.Business;
import com.home.service.models.BusinessLocation;
import com.home.service.models.BusinessServices;
import com.home.service.models.Customer;
import com.home.service.models.Order;
import com.home.service.models.OrderItem;
import com.home.service.models.PaymentMethod;
import com.home.service.models.enums.OrderStatus;
import com.home.service.models.enums.PaymentStatus;
import com.home.service.repositories.AddressRepository;
import com.home.service.repositories.BusinessLocationRepository;
import com.home.service.repositories.BusinessRepository;
import com.home.service.repositories.BusinessServiceRepository;
import com.home.service.repositories.CustomerRepository;
import com.home.service.repositories.OrderRepository;
import com.home.service.repositories.PaymentMethodRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.Setter;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final BusinessServiceRepository businessServiceRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BusinessLocationRepository businessLocationRepository;

    public OrderService(OrderRepository orderRepository,
            CustomerRepository customerRepository,
            BusinessRepository businessRepository,
            BusinessServiceRepository businessServiceRepository,
            PaymentMethodRepository paymentMethodRepository,
            AddressRepository addressRepository,
            BusinessLocationRepository businessLocationRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.businessRepository = businessRepository;
        this.businessServiceRepository = businessServiceRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.businessLocationRepository = businessLocationRepository;
    }

    @Getter
    @Setter
    public static class OrderItemDTO {
        private Long id;
        private Long serviceId;
        private Integer quantity;
        private Map<String, String> selectedOptions;
        private String notes;

        public OrderItemDTO() {
        }

        public OrderItemDTO(OrderItem item) {
            this.id = item.getId();
            this.serviceId = item.getService().getId();
            this.quantity = item.getQuantity();
            this.selectedOptions = item.getSelectedOptions();
            this.notes = item.getNotes();
        }
    }

    @Getter
    @Setter
    public static class OrderDTO {
        private Long id;
        private Long businessId;
        private List<OrderItemDTO> items;
        private Long serviceLocationId;
        private Long paymentMethodId;
        private LocalDateTime scheduledDate;
        private String specialInstructions;

        public OrderDTO() {

        }

        public OrderDTO(Order order) {
            this.id = order.getId();
            this.businessId = order.getBusiness().getId();
            this.items = order.getItems().stream()
                    .map(OrderItemDTO::new)
                    .collect(Collectors.toList());
            this.serviceLocationId = order.getServiceLocation().getId();
            this.paymentMethodId = order.getPaymentMethod().getId();
            this.scheduledDate = order.getScheduledDate();
            this.specialInstructions = order.getSpecialInstructions();
        }

    }

    @Getter
    @Setter
    public static class DetailedOrderItemDTO {
        private Long id;
        private Long serviceId;
        private String serviceName;
        private String serviceDescription;
        private Double serviceBasePrice;
        private Integer quantity;
        private Double unitPrice;
        private Double subtotal;
        private Map<String, String> selectedOptions;
        private String notes;

        public DetailedOrderItemDTO(OrderItem item) {
            this.id = item.getId();
            this.serviceId = item.getService().getId();
            this.serviceName = item.getService().getName();
            this.serviceDescription = item.getService().getDescription();
            this.serviceBasePrice = item.getService().getPrice();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
            this.subtotal = item.getSubtotal();
            this.selectedOptions = item.getSelectedOptions();
            this.notes = item.getNotes();
        }
    }

    @Getter
    @Setter
    public static class DetailedOrderDTO {
        private Long id;
        private String orderNumber;
        private Long businessId;
        private String businessName;
        private String businessPhone;
        private String businessEmail;

        private Long customerId;
        private String customerName;
        private String customerEmail;
        private String customerPhone;

        private List<DetailedOrderItemDTO> items;
        private BusinessLocationDTO serviceLocation;
        private PaymentMethod paymentMethod;
        private LocalDateTime scheduledDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private String specialInstructions;
        private OrderStatus status;
        private PaymentStatus paymentStatus;

        private Double subtotal;
        private Double tax;
        private Double total;

        public DetailedOrderDTO(Order order) {
            this.id = order.getId();
            this.orderNumber = order.getOrderNumber();

            // Business details
            Business business = order.getBusiness();
            this.businessId = business.getId();
            this.businessName = business.getName();
            this.businessPhone = business.getPhoneNumber();
            this.businessEmail = business.getEmail();

            // Customer details
            Customer customer = order.getCustomer();
            this.customerId = customer.getId();
            this.customerName = customer.getUser().getName();
            this.customerEmail = customer.getUser().getEmail();
            this.customerPhone = customer.getUser().getPhoneNumber();

            // Order items
            this.items = order.getItems().stream()
                    .map(DetailedOrderItemDTO::new)
                    .collect(Collectors.toList());

            // Location and payment
            BusinessLocation location = order.getServiceLocation();
            this.serviceLocation = location != null ? new BusinessLocationDTO(location) : null;
            this.paymentMethod = order.getPaymentMethod();

            // Dates
            this.scheduledDate = order.getScheduledDate();
            this.createdAt = order.getCreatedAt();
            this.updatedAt = order.getUpdatedAt();

            // Order details
            this.specialInstructions = order.getSpecialInstructions();
            this.status = order.getStatus();
            this.paymentStatus = order.getPaymentStatus();

            // Financial details
            this.subtotal = order.getSubtotal();
            this.tax = order.getTax();
            this.total = order.getTotal();
        }
    }

    @Transactional
    public DetailedOrderDTO createOrder(OrderDTO dto, Long currentUserId) {
        Customer customer = customerRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setBusiness(business);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setScheduledDate(dto.getScheduledDate());
        order.setSpecialInstructions(dto.getSpecialInstructions());

        if (dto.getServiceLocationId() != null) {
            BusinessLocation serviceLocation = businessLocationRepository.findById(dto.getServiceLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Service location not found"));
            order.setServiceLocation(serviceLocation);
        }

        if (dto.getPaymentMethodId() != null) {
            PaymentMethod paymentMethod = paymentMethodRepository.findById(dto.getPaymentMethodId())
                    .orElseThrow(() -> new EntityNotFoundException("Payment method not found"));
            order.setPaymentMethod(paymentMethod);
        }

        List<OrderItem> orderItems = dto.getItems().stream()
                .map(itemDto -> createOrderItem(itemDto, order))
                .collect(Collectors.toList());

        order.setItems(orderItems);
        calculateTotals(order);

        Order savedOrder = orderRepository.save(order);
        return new DetailedOrderDTO(savedOrder);
    }

    private OrderItem createOrderItem(OrderItemDTO dto, Order order) {
        BusinessServices service = businessServiceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setService(service);
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(service.getPrice());
        item.setSelectedOptions(dto.getSelectedOptions());
        item.setNotes(dto.getNotes());

        // Calculate subtotal including options
        double optionsTotal = calculateOptionsTotal(service, dto.getSelectedOptions());
        item.setSubtotal((service.getPrice() + optionsTotal) * dto.getQuantity());

        return item;
    }

    private double calculateOptionsTotal(BusinessServices service, Map<String, String> selectedOptions) {
        if (selectedOptions == null || service.getOptions() == null) {
            return 0.0;
        }

        return service.getOptions().stream()
                .filter(option -> selectedOptions.containsKey(option.getName()))
                .mapToDouble(option -> {
                    String selectedChoice = selectedOptions.get(option.getName());
                    int choiceIndex = option.getChoices().indexOf(selectedChoice);
                    return choiceIndex >= 0 && choiceIndex < option.getPrices().size()
                            ? option.getPrices().get(choiceIndex)
                            : 0.0;
                })
                .sum();
    }

    private void calculateTotals(Order order) {
        double subtotal = order.getItems().stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();

        order.setSubtotal(subtotal);
        // Calculate tax (you can modify the tax rate as needed)
        order.setTax(subtotal * 0.15); // 15% tax
        order.setTotal(subtotal + order.getTax());
    }

    private String generateOrderNumber() {
        String orderNumber;
        do {
            orderNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    public DetailedOrderDTO getOrderById(Long id, Long currentUserId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getCustomer().getUser().getId().equals(currentUserId) &&
                !order.getBusiness().getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Unauthorized");
        }

        return new DetailedOrderDTO(order);
    }

    public Page<DetailedOrderDTO> getCustomerOrders(Long currentUserId, int page, int size) {
        Customer customer = customerRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByCustomer(customer, pageable);
        return orders.map(DetailedOrderDTO::new);
    }

    public Page<DetailedOrderDTO> getBusinessOrders(Long businessId, Long currentUserId, int page, int size) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        // if (!business.getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Unauthorized");
        // }

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByBusiness(business, pageable);
        return orders.map(DetailedOrderDTO::new);
    }

    @Transactional
    public DetailedOrderDTO updateOrderStatus(Long orderId, OrderStatus status, Long currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        // if (!order.getBusiness().getOwner().getId().equals(currentUserId)) {
        // throw new AccessDeniedException("Only business owners can update order
        // status");
        // }

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return new DetailedOrderDTO(savedOrder);
    }
}