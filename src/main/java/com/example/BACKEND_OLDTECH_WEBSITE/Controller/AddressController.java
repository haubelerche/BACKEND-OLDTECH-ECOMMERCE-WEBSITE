package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address.AddressRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address.AddressResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address.AddressSelectionResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Address;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.AddressService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/addresses")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('Customer')")  // Only customers can access address management
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);    @Autowired
    private AddressService addressService;

    @Autowired
    private UserService userService;

    /**
     * Get all addresses for the current user
     */

    //ok
    @GetMapping("/list")
    public ResponseEntity<?> getAllAddresses() {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Người dùng chưa đăng nhập hoặc token không hợp lệ");
            }
            Integer userId = currentUser.getUserId();
            logger.info("Getting all addresses for user ID: {}", userId);
            List<Address> addresses = addressService.getAllAddressesByUserId(userId);
            List<AddressResponse> response = addresses.stream()
                    .map(this::convertToAddressResponse)
                    .collect(Collectors.toList());
            logger.info("Retrieved {} addresses for user ID: {}", addresses.size(), userId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving addresses: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Get a specific address by addressId for the current user
     */
    @GetMapping("/get/{addressId}")
    public ResponseEntity<?> getAddressById(@PathVariable Integer addressId) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Người dùng chưa đăng nhập hoặc token không hợp lệ");
            }
            Integer userId = currentUser.getUserId();
            logger.info("Getting address ID: {} for user ID: {}", addressId, userId);
            Address address = addressService.getAddressById(addressId, userId);
            AddressResponse response = convertToAddressResponse(address);
            logger.info("Successfully retrieved address ID: {} for user ID: {}", addressId, userId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Address not found with ID: {}: {}", addressId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.error("Unauthorized access to address ID: {}: {}", addressId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving address ID: {}: {}", addressId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy thông tin địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Create a new address
     */
    //ok
    @PostMapping("/create")
    public ResponseEntity<?> createAddress(@Valid @RequestBody AddressRequest addressRequest) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Người dùng chưa đăng nhập hoặc token không hợp lệ");
            }
            Integer userId = currentUser.getUserId();
            logger.info("Creating address for userId: {} with request: {}", userId, addressRequest);
            Address savedAddress = addressService.createAddress(addressRequest, userId);
            AddressResponse response = convertToAddressResponse(savedAddress);
            logger.info("Address created successfully for user ID: {}, address ID: {}", userId, savedAddress.getAddressId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating address: {}", e.getMessage(), e);
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
                                          @Valid @RequestBody AddressRequest addressRequest) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Người dùng chưa đăng nhập hoặc token không hợp lệ");
            }
            Integer userId = currentUser.getUserId();
            Address updatedAddress = addressService.updateAddress(addressId, addressRequest, userId);
            AddressResponse response = convertToAddressResponse(updatedAddress);
            logger.info("Address updated successfully for user ID: {}, address ID: {}", userId, addressId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Address not found with ID: {}: {}", addressId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.error("Unauthorized access to update address ID: {}: {}", addressId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating address ID: {}: {}", addressId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Delete an address
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer addressId) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Người dùng chưa đăng nhập hoặc token không hợp lệ");
            }
            Integer userId = currentUser.getUserId();
            logger.info("Deleting address ID: {} for user ID: {}", addressId, userId);
            addressService.deleteAddress(addressId, userId);
            logger.info("Address deleted successfully for user ID: {}, address ID: {}", userId, addressId);
            return ResponseEntity.ok(
                    java.util.Map.of("message", "Địa chỉ đã được xóa thành công")
            );
        } catch (EntityNotFoundException e) {
            logger.error("Address not found with ID: {}: {}", addressId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.error("Unauthorized access to delete address ID: {}: {}", addressId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting address ID: {}: {}", addressId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa địa chỉ: " + e.getMessage());
        }
    }

    /**
     * Get all addresses for the current authenticated user (for address selection)
     * This endpoint automatically detects the current user from JWT token
     */
    @GetMapping("/my-addresses")
    public ResponseEntity<?> getMyAddresses() {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Người dùng chưa đăng nhập hoặc token không hợp lệ");
            }

            logger.info("Getting addresses for current user: {} (ID: {})", currentUser.getEmail(), currentUser.getUserId());
            List<Address> addresses = addressService.getAllAddressesByUserId(currentUser.getUserId());
            
            List<AddressResponse> response = addresses.stream()
                    .map(this::convertToAddressResponse)
                    .collect(Collectors.toList());
                    
            logger.info("Retrieved {} addresses for user: {}", addresses.size(), currentUser.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving addresses for current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách địa chỉ: " + e.getMessage());
        }
    }


    private AddressResponse convertToAddressResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setAddressId(address.getAddressId());
        response.setUserId(address.getUserId());
        response.setCity(address.getCity());
        response.setDistrict(address.getDistrict());        response.setWard(address.getWard());
        response.setStreet(address.getStreet());
        response.setDetailedAddress(address.getDetailedAddress());
        response.setAddressType(address.getAddressType());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        response.setUpdatedAt(address.getUpdatedAt());
        return response;
    }

    /**
     * Helper method to get the current authenticated user
     */
    private User getCurrentAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("No authentication found in security context");
                return null;
            }
            
            String email = authentication.getName();
            if (email == null || email.trim().isEmpty()) {
                logger.warn("No email found in authentication principal");
                return null;
            }
            
            User user = userService.findUserByEmail(email);
            if (user == null) {
                logger.warn("No user found with email: {}", email);
            }
            
            return user;
        } catch (Exception e) {
            logger.error("Error getting authenticated user: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert Address to AddressSelectionResponse for order selection
     */
    private AddressSelectionResponse convertToAddressSelectionResponse(Address address) {
        AddressSelectionResponse response = new AddressSelectionResponse();
        response.setAddressId(address.getAddressId());
        response.setIsDefault(address.getIsDefault());
        response.setAddressType(address.getAddressType());
        response.setCity(address.getCity());
        response.setDistrict(address.getDistrict());
        response.setWard(address.getWard());
        response.setStreet(address.getStreet());
        response.setDetailedAddress(address.getDetailedAddress());
        
        // Create formatted address for display
        String fullAddress = String.format("%s, %s, %s, %s, %s",
                address.getDetailedAddress() != null ? address.getDetailedAddress() : "",
                address.getStreet() != null ? address.getStreet() : "",
                address.getWard() != null ? address.getWard() : "",
                address.getDistrict() != null ? address.getDistrict() : "",
                address.getCity() != null ? address.getCity() : ""
        ).replaceAll(", ,", ",").replaceAll("^,|,$", "");
        
        response.setFullAddress(fullAddress);
        
        // Create user-friendly selection label
        String selectionLabel = "";
        if (address.getAddressType() != null) {
            selectionLabel = address.getAddressType() + " - ";
        }
        selectionLabel += fullAddress;
        
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            selectionLabel += " (Mặc định)";
        }
        
        response.setSelectionLabel(selectionLabel);
        response.setDisplayName(selectionLabel);
        response.setCanBeUsedForOrders(true); // All addresses can be used for orders
        
        return response;
    }
}
