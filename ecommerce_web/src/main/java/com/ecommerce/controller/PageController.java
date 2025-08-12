package com.ecommerce.controller;

import com.ecommerce.model.Order;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/shop")
    public String shop(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("products", productService.getAll());
        return "shop";
    }

    @GetMapping("/shop/category/{id}")
    public String shopByCategory(@PathVariable("id") int id, Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("products", productService.getProByCatId(id));
        return "shop";
    }

    @GetMapping("/shop/viewproduct/{id}")
    public String viewProduct(@PathVariable("id") Long id, Model model) {
        return productService.fetchbyId(id).map(product -> {
            System.out.println("Fetched Product: ID=" + product.getId() + ", Name=" + product.getName());
            model.addAttribute("product", product);
            return "viewProduct";
        }).orElseGet(() -> {
            System.out.println("Product not found for ID: " + id);
            model.addAttribute("error", "Product not found.");
            return "error";
        });
    }

    @GetMapping("/userform")
    public String userForm(@RequestParam(value = "productId", required = false, defaultValue = "0") Long productId,
                           @RequestParam(value = "productName", required = false, defaultValue = "") String productName,
                           Model model) {
        Order order = new Order();
        order.setProductId(productId);
        order.setProductName(productName);
        order.setQuantity(1);
        model.addAttribute("order", order);
        if (productId == 0 || productName.isEmpty()) {
            model.addAttribute("error", "Invalid product details. Please select a valid product.");
        }
        System.out.println("UserForm: productId=" + productId + ", productName=" + productName);
        return "userform";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(@ModelAttribute("order") @Valid Order order, BindingResult result, Model model) {
        System.out.println("Received Order: " + order);
        if (result.hasErrors()) {
            System.out.println("Validation Errors: " + result.getAllErrors());
            return "userform";
        }
        orderService.saveOrder(order);
        model.addAttribute("order", order);
        return "OrderConfirmation";
    }

    @GetMapping("/OrderConfirmation")
    public String orderConfirmation() {
        return "OrderConfirmation";
    }
}