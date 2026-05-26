package com.ecommerce.service;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.request.OrderItemRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.Role;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidOrderStateException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private User customer() {
        return User.builder().id(1L).name("Juan").email("user@store.com").role(Role.CUSTOMER).build();
    }

    private Address address(User user) {
        return Address.builder().id(1L).user(user).street("Calle 1").city("Lima").country("Peru").zipCode("15001").build();
    }

    private Product product(int stock) {
        return Product.builder().id(1L).name("Laptop Dell").price(new BigDecimal("100.00")).stock(stock).build();
    }

    @Test
    void createOrder_success() {
        // given
        User user = customer();
        Product product = product(10);
        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(1L, 2)), 1L);

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address(user)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(new OrderResponse(1L, "PENDING", new BigDecimal("200.00"), List.of(), null));

        // when
        OrderResponse response = orderService.createOrder(user, request);

        // then
        assertNotNull(response);
        assertEquals(8, product.getStock());

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();
        assertEquals(OrderStatus.PENDING, saved.getStatus());
        assertEquals(new BigDecimal("200.00"), saved.getTotal());
        assertEquals(1, saved.getItems().size());
    }

    @Test
    void createOrder_insufficientStock() {
        // given
        User user = customer();
        CreateOrderRequest request = new CreateOrderRequest(List.of(new OrderItemRequest(1L, 5)), 1L);

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address(user)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1)));

        // when / then
        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(user, request));
    }

    @Test
    void cancelOrder_success() {
        // given
        User user = customer();
        Product product = product(5);
        OrderItem item = OrderItem.builder().product(product).quantity(2).unitPrice(new BigDecimal("100.00")).build();
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.PENDING)
                .items(List.of(item))
                .total(new BigDecimal("200.00"))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(new OrderResponse(1L, "CANCELLED", new BigDecimal("200.00"), List.of(), null));

        // when
        orderService.cancelOrder(user, 1L);

        // then
        assertEquals(7, product.getStock());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancelOrder_invalidState() {
        // given
        User user = customer();
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.SHIPPED)
                .total(new BigDecimal("200.00"))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when / then
        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(user, 1L));
    }

    @Test
    void updateStatus_validTransition() {
        // given
        Order order = Order.builder().id(1L).user(customer()).status(OrderStatus.PENDING).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class)))
                .thenReturn(new OrderResponse(1L, "CONFIRMED", null, List.of(), null));

        // when
        orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        // then
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void updateStatus_invalidTransition() {
        // given
        Order order = Order.builder().id(1L).user(customer()).status(OrderStatus.PENDING).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when / then
        assertThrows(InvalidOrderStateException.class, () -> orderService.updateStatus(1L, OrderStatus.SHIPPED));
    }
}
