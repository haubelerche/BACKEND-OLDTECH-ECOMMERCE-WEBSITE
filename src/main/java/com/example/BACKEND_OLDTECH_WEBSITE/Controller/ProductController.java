package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

/*--DÀNH CHO ADMIN--*/

// ĐÁNH DẤU SẢN PHẨM ĐANG CHỜ DUYỆT
@PostMapping("/products/{productId}/mark-pending")
public ResponseEntity<?> markProductAsPending(@PathVariable Integer productId) {
    try {
        productService.markProductAsPending(productId);
        return ResponseEntity.ok("Sản phẩm " + productId + " đã được đánh dấu là đang chờ duyệt.");
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi đánh dấu sản phẩm đang chờ duyệt: " + e.getMessage());
    }
}



//XÁC THỰC SẢN PHẨM
@PostMapping("/products/{productId}/verify")
public ResponseEntity<?> verifyProduct(@PathVariable Integer productId) {
    try {
        productService.verifyProduct(productId);
        return ResponseEntity.ok("Sản phẩm " + productId + " đã được xác thực thành công.");
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xác thực sản phẩm: " + e.getMessage());
    }
}



//TỪ CHỐI SẢN PHẨM
@PostMapping("/products/{productId}/reject")
public ResponseEntity<?> rejectProduct(@PathVariable Integer productId) {
    try {
        productService.rejectProduct(productId);
        return ResponseEntity.ok("Sản phẩm " + productId + " đã bị từ chối.");
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi từ chối sản phẩm: " + e.getMessage());
    }
}



/*--DÀNH CHO NGƯỜI DÙNG NÓI CHUNG--*/

// TÌM KIẾM SẢN PHẨM BẰNG TỪ KHÓA
    @GetMapping("/searchProducts")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }



// LẤY THÔNG TIN SẢN PHẨM BẰNG ID
    @GetMapping("/getProductDetails/{productId}")
    public ResponseEntity<Product> getProductDetails(@PathVariable Integer productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }



// TÌM KIẾM DANH SÁCH TOÀN BỘ SẢN PHẨM
    @GetMapping("/getAllProducts")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }




//ẨN SẢN PHẨM
    @PostMapping("/products/{productId}/hide")
    public ResponseEntity<?> hideProduct(@PathVariable Integer productId) {
        try {
            productService.hideProduct(productId);
            return ResponseEntity.ok("Sản phẩm " + productId + " đã bị ẩn.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi ẩn sản phẩm: " + e.getMessage());
        }
    }


//CẬP NHẬT DANH MỤC SẢN PHẨM
    @PostMapping("/products/{productId}/category")
    public ResponseEntity<?> setProductCategory(@PathVariable Integer productId, @RequestParam Integer categoryId) {
        try {
            productService.setProductCategory(productId, categoryId);
            return ResponseEntity.ok("Danh mục của sản phẩm " + productId + " đã được cập nhật thành " + categoryId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật danh mục sản phẩm: " + e.getMessage());
        }
    }
}
