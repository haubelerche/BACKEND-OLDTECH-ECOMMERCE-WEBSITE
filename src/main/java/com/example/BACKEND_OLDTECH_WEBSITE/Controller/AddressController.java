package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address.AddressRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address.AddressResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Address;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/addresses")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('Customer')")  // Only customers can access address management
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private AddressService addressService;

    /**
     * Get all addresses for the current user
     */

    //ok
    @GetMapping("/list")
    public ResponseEntity<?> getAllAddresses(@RequestParam Integer userId) {
        try {
            logger.info("Getting all addresses for user ID: {}", userId);
            List<Address> addresses = addressService.getAllAddressesByUserId(userId);
            List<AddressResponse> response = addresses.stream()
                    .map(this::convertToAddressResponse)
                    .collect(Collectors.toList());
            logger.info("Retrieved {} addresses for user ID: {}", addresses.size(), userId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("User not found with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving addresses for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Get a specific address by ID
     */
    @GetMapping("/get/{addressId}")
    public ResponseEntity<?> getAddressById(@PathVariable Integer addressId, @RequestParam Integer userId) {
        try {
            logger.info("Getting address ID: {} for user ID: {}", addressId, userId);
            Address address = addressService.getAddressById(addressId, userId);
            AddressResponse response = convertToAddressResponse(address);
            logger.info("Successfully retrieved address ID: {} for user ID: {}", addressId, userId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Address not found with ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.error("Unauthorized access to address ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving address ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy thông tin địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Create a new address
     */
    //ok
    @PostMapping("/create")
    public ResponseEntity<?> createAddress(@Valid @RequestBody AddressRequest addressRequest, @RequestParam Integer userId) {
        try {
            logger.info("Creating address for userId: {} with request: {}", userId, addressRequest);
            Address savedAddress = addressService.createAddress(addressRequest, userId);
            AddressResponse response = convertToAddressResponse(savedAddress);
            logger.info("Address created successfully for user ID: {}, address ID: {}", userId, savedAddress.getAddressId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found for user ID: {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for user ID: {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating address for user ID: {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo địa chỉ mới: " + e.getMessage());
        }
    }

    /**
     * Update an existing address
     */
    //ok
    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable Integer addressId,
                                          @Valid @RequestBody AddressRequest addressRequest,
                                          @RequestParam Integer userId) {
        try {
            logger.info("Updating address ID: {} for user ID: {} with request: {}", addressId, userId, addressRequest);
            Address updatedAddress = addressService.updateAddress(addressId, addressRequest, userId);
            AddressResponse response = convertToAddressResponse(updatedAddress);
            logger.info("Address updated successfully for user ID: {}, address ID: {}", userId, addressId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Address not found with ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.error("Unauthorized access to update address ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating address ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Delete an address
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer addressId, @RequestParam Integer userId) {
        try {
            logger.info("Deleting address ID: {} for user ID: {}", addressId, userId);
            addressService.deleteAddress(addressId, userId);
            logger.info("Address deleted successfully for user ID: {}, address ID: {}", userId, addressId);
            return ResponseEntity.ok(
                    java.util.Map.of("message", "Địa chỉ đã được xóa thành công")
            );
        } catch (EntityNotFoundException e) {
            logger.error("Address not found with ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.error("Unauthorized access to delete address ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting address ID: {} for user ID: {}", addressId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa địa chỉ: " + e.getMessage());
        }
    }




    private AddressResponse convertToAddressResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setAddressId(address.getAddressId());
        response.setUserId(address.getUserId());
        response.setCity(address.getCity());
        response.setDistrict(address.getDistrict());
        response.setWard(address.getWard());
        response.setStreet(address.getStreet());
        response.setDetailedAddress(address.getDetailedAddress());
        response.setAddressType(address.getAddressType());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());
        return response;
    }
}
