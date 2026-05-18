package com.ecommerce.controller;

import com.ecommerce.config.CustomUserDetails;
import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.dto.response.AddressResponse;
import com.ecommerce.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Addresses", description = "Direcciones de envio del usuario")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    @Operation(summary = "Lista las direcciones del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de direcciones")
    public ResponseEntity<List<AddressResponse>> findAll(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(addressService.findByUser(principal.getId()));
    }

    @PostMapping
    @Operation(summary = "Agrega una direccion al usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Direccion creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos")
    })
    public ResponseEntity<AddressResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                   @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.create(principal.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una direccion propia")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Direccion eliminada"),
            @ApiResponse(responseCode = "403", description = "La direccion no te pertenece"),
            @ApiResponse(responseCode = "404", description = "Direccion no encontrada")
    })
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                       @PathVariable Long id) {
        addressService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
