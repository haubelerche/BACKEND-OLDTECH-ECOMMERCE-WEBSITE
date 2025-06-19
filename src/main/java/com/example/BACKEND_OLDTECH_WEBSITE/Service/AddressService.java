package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address.AddressRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Address;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.AddressRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all addresses for a user
     */
    public List<Address> getAllAddressesByUserId(Integer userId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        return addressRepository.findByUserId(userId);
    }

    /**
     * Get a specific address by ID
     */
    public Address getAddressById(Integer addressId, Integer userId) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + addressId));

        // Verify the address belongs to the user
        if (!address.getUserId().equals(userId)) {
            throw new SecurityException("You do not have permission to access this address");
        }

        return address;
    }

    /**
     * Create a new address for a user
     */
    @Transactional
    public Address createAddress(AddressRequest addressRequest, Integer userId) {
        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Create new address
        Address address = new Address();
        address.setUserId(userId);
        address.setCity(addressRequest.getCity());
        address.setDistrict(addressRequest.getDistrict());
        address.setWard(addressRequest.getWard());
        address.setStreet(addressRequest.getStreet());
        address.setDetailedAddress(addressRequest.getDetailedAddress());
        address.setAddressType(addressRequest.getAddressType());
        address.setIsDefault(addressRequest.isIsDefault());

        if (Boolean.TRUE.equals(addressRequest.isIsDefault())) {
            List<Address> userAddresses = addressRepository.findByUserId(userId);
            userAddresses.forEach(existingAddress -> {
                if (Boolean.TRUE.equals(existingAddress.getIsDefault())) {
                    existingAddress.setIsDefault(false);
                    addressRepository.save(existingAddress);
                }
            });
        }

        address.setCreatedAt(Timestamp.from(Instant.now()).toLocalDateTime());
        address.setUpdatedAt(Timestamp.from(Instant.now()).toLocalDateTime());

        return addressRepository.save(address);
    }

    /**
     * Update an existing address
     */
    @Transactional
    public Address updateAddress(Integer addressId, AddressRequest addressRequest, Integer userId) {
        // Get existing address
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + addressId));

        // Verify the address belongs to the user
        if (!address.getUserId().equals(userId)) {
            throw new SecurityException("You do not have permission to update this address");
        }

        // Update address fields
        address.setCity(addressRequest.getCity());
        address.setDistrict(addressRequest.getDistrict());
        address.setWard(addressRequest.getWard());
        address.setStreet(addressRequest.getStreet());
        address.setDetailedAddress(addressRequest.getDetailedAddress());
        address.setAddressType(addressRequest.getAddressType());
        address.setUpdatedAt(Timestamp.from(Instant.now()).toLocalDateTime());

        // Handle default address setting
        if (Boolean.TRUE.equals(addressRequest.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            List<Address> userAddresses = addressRepository.findByUserId(userId);
            userAddresses.forEach(existingAddress -> {
                if (!existingAddress.getAddressId().equals(addressId) &&
                    Boolean.TRUE.equals(existingAddress.getIsDefault())) {
                    existingAddress.setIsDefault(false);
                    addressRepository.save(existingAddress);
                }
            });
            address.setIsDefault(true);
        }

        return addressRepository.save(address);
    }

    /**
     * Delete an address
     */
    @Transactional
    public void deleteAddress(Integer addressId, Integer userId) {
        // Get existing address
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + addressId));

        // Verify the address belongs to the user
        if (!address.getUserId().equals(userId)) {
            throw new SecurityException("You do not have permission to delete this address");
        }

        // Delete the address
        addressRepository.delete(address);
    }

    /**
     * Set an address as default
     */
    @Transactional
    public Address setDefaultAddress(Integer addressId, Integer userId) {
        // Get existing address
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new EntityNotFoundException("Address not found with ID: " + addressId));

        // Verify the address belongs to the user
        if (!address.getUserId().equals(userId)) {
            throw new SecurityException("You do not have permission to update this address");
        }

        // Remove default flag from all other addresses
        List<Address> userAddresses = addressRepository.findByUserId(userId);
        userAddresses.forEach(existingAddress -> {
            if (Boolean.TRUE.equals(existingAddress.getIsDefault())) {
                existingAddress.setIsDefault(false);
                addressRepository.save(existingAddress);
            }
        });

        // Set this address as default
        address.setIsDefault(true);
        address.setUpdatedAt(Timestamp.from (Instant.now()).toLocalDateTime());

        return addressRepository.save(address);
    }
}
