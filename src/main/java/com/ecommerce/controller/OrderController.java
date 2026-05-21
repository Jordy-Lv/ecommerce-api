package com.ecommerce.controller;

import com.ecommerce.config.CustomUserDetails;
import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.request.UpdateStatusRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Gestion de ordenes")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Crea una orden y descuenta el stock")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada"),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos invalidos"),
            @ApiResponse(responseCode = "404", description = "Producto o direccion no encontrada")
    })
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista las ordenes del usuario autenticado (ADMIN ve todas)")
    @ApiResponse(responseCode = "200", description = "Lista de ordenes")
    public ResponseEntity<List<OrderResponse>> findAll(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(orderService.getUserOrders(principal.getUser()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "403", description = "La orden no te pertenece"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrderResponse> findById(@AuthenticationPrincipal CustomUserDetails principal,
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(principal.getUser(), id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela una orden y revierte el stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden cancelada"),
            @ApiResponse(responseCode = "400", description = "La orden no se puede cancelar en su estado actual"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(principal.getUser(), id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambia el estado de una orden (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Transicion de estado no permitida"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }
}
