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
import com.ecommerce.exception.EntityNotFoundException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidOrderStateException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        AddressRepository addressRepository,
                        OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.orderMapper = orderMapper;
    }

    // Crea una orden nueva a partir de los productos que el usuario eligio
    // Valida que haya stock suficiente, descuenta del inventario y calcula el total
    @Transactional
    public OrderResponse createOrder(User user, CreateOrderRequest request) {
        // Busco la direccion de envio y verifico que sea del usuario
        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new EntityNotFoundException("Direccion no encontrada con id: " + request.addressId()));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Esta direccion no te pertenece");
        }

        // Armo la orden con estado PENDING
        Order order = Order.builder()
                .user(user)
                .shippingAddress(address)
                .status(OrderStatus.PENDING)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // Recorro cada item del pedido, valido stock y voy calculando el total
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + itemRequest.productId()));

            // Verifico que haya suficiente stock antes de continuar
            if (product.getStock() < itemRequest.quantity()) {
                throw new InsufficientStockException("Stock insuficiente para: " + product.getName());
            }

            // Descuento el stock del producto
            product.setStock(product.getStock() - itemRequest.quantity());

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addItem(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotal(total);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // Devuelve las ordenes del usuario, si es admin ve todas
    public List<OrderResponse> getUserOrders(User user) {
        List<Order> orders = user.getRole() == Role.ADMIN
                ? orderRepository.findAll()
                : orderRepository.findByUserId(user.getId());

        return orders.stream().map(orderMapper::toResponse).toList();
    }

    // Busca una orden por id, solo el dueño o un admin pueden verla
    public OrderResponse getOrderById(User user, Long orderId) {
        Order order = getOrder(orderId);
        checkAccess(user, order);
        return orderMapper.toResponse(order);
    }

    // Cancela una orden, solo si esta en PENDING o CONFIRMED
    // Devuelve el stock al inventario cuando se cancela
    @Transactional
    public OrderResponse cancelOrder(User user, Long orderId) {
        Order order = getOrder(orderId);
        checkAccess(user, order);

        // Solo se puede cancelar si no se ha enviado todavia
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException("Solo se pueden cancelar ordenes en estado PENDING o CONFIRMED");
        }

        // Devuelvo los productos al stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // Cambia el estado de una orden (solo admins), valida que la transicion sea permitida
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrder(orderId);

        // Verifico que el cambio de estado sea valido segun el flujo definido
        if (!order.getStatus().canChangeTo(newStatus)) {
            throw new InvalidOrderStateException(
                    "Transicion no permitida de " + order.getStatus() + " a " + newStatus);
        }

        order.setStatus(newStatus);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // Metodo auxiliar para buscar ordenes sin repetir codigo
    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + orderId));
    }

    // Verifica que el usuario tenga permiso para ver/modificar la orden
    private void checkAccess(User user, Order order) {
        if (user.getRole() != Role.ADMIN && !order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("No puedes acceder a esta orden");
        }
    }
}
