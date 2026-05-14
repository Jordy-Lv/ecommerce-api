package com.ecommerce.service;

import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.dto.response.AddressResponse;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.User;
import com.ecommerce.exception.EntityNotFoundException;
import com.ecommerce.repository.AddressRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<AddressResponse> findByUser(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public AddressResponse create(User user, AddressRequest request) {
        Address address = Address.builder()
                .user(user)
                .street(request.street())
                .city(request.city())
                .country(request.country())
                .zipCode(request.zipCode())
                .build();

        return toResponse(addressRepository.save(address));
    }

    public void delete(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Direccion no encontrada con id: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Esta direccion no te pertenece");
        }

        addressRepository.delete(address);
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getCountry(),
                address.getZipCode());
    }
}
