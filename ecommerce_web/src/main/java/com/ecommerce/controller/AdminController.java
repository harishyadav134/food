package com.ecommerce.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.dto.ProductDt;
import com.ecommerce.entity.Admin;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.service.AdminService;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;

import jakarta.validation.Valid;

@Controller
public class AdminController {

    @Autowired
    private CategoryService cservice;

    @Autowired
    private ProductService pservice;

    @Autowired
    private AdminService aservice;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/admin")
    public String admin() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String postRegister(@RequestParam String email, @RequestParam String password) {
        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            Admin admin = new Admin();
            admin.setEmail(email);
            admin.setPassword(password);
            aservice.save(admin);
        }
        return "redirect:/admin";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password, Model model) {
        List<Admin> list = aservice.fetchAll();
        for (Admin a : list) {
            if (a.getEmail().equals(email) && a.getPassword().equals(a.getPassword())) {
                model.addAttribute("userobject", a);
                return "admin";
            }
        }
        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    @GetMapping("/admin/categories")
    public String categoryPage(Model model) {
        List<Category> list = cservice.getAll();
        model.addAttribute("categories", list);
        return "categories";
    }

    @GetMapping("/admin/categories/add")
    public String addCategory(Model model) {
        Category category = new Category();
        model.addAttribute("category", category);
        return "categoriesAdd";
    }

    @PostMapping("/admin/categories/add")
    public String postAddCategory(@ModelAttribute("category") Category category) {
        cservice.saveCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id) {
        cservice.deletebyId(id);
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/update/{id}")
    public String updateCategory(@PathVariable("id") int id, Model model) {
        Optional<Category> category = cservice.fetchbyId(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "categoriesAdd";
        } else {
            return "error";
        }
    }

    @GetMapping("/admin/products")
    public String productPage(Model model) {
        List<Product> list = pservice.getAll();
        model.addAttribute("products", list);
        return "products";
    }

    @GetMapping("/admin/products/add")
    public String addProduct(Model model) {
        ProductDt productDt = new ProductDt();
        model.addAttribute("productDTO", productDt);
        model.addAttribute("categories", cservice.getAll());
        return "productsAdd";
    }

    @PostMapping("/admin/products/add")
    public String postAddProduct(@Valid @ModelAttribute("productDTO") ProductDt productDt,
                                 BindingResult result,
                                 @RequestParam("productImage") MultipartFile file,
                                 @RequestParam("imgName") String imgName,
                                 Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", cservice.getAll());
            return "productsAdd";
        }

        Product product = new Product();
        product.setId(productDt.getId());
        product.setName(productDt.getName());
        product.setPrice(productDt.getPrice());
        product.setDescription(productDt.getDescription());
        product.setWeight(productDt.getWeight());
        product.setCategory(cservice.fetchbyId(productDt.getCategoryId()).orElse(null));

        String imageUUID;
        if (!file.isEmpty()) {
            imageUUID = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            Path path = Paths.get(uploadPath, imageUUID);
            Files.write(path, file.getBytes());
        } else {
            imageUUID = imgName;
        }

        product.setImageName(imageUUID);
        pservice.saveProduct(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") long id) {
        pservice.deletebyId(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/product/update/{id}")
    public String updateProduct(@PathVariable("id") long id, Model model) {
        Optional<Product> productOpt = pservice.fetchbyId(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            ProductDt productDt = new ProductDt();
            productDt.setId(product.getId());
            productDt.setName(product.getName());
            productDt.setPrice(product.getPrice());
            productDt.setWeight(product.getWeight());
            productDt.setDescription(product.getDescription());
            productDt.setCategoryId(product.getCategory().getId());
            productDt.setImageName(product.getImageName());
            model.addAttribute("productDTO", productDt);
            model.addAttribute("categories", cservice.getAll());
            return "productsAdd";
        } else {
            return "error";
        }
    }

    @PostMapping("/admin/product/update/{id}")
    public String postUpdateProduct(@PathVariable("id") long id,
                                   @Valid @ModelAttribute("productDTO") ProductDt productDt,
                                   BindingResult result,
                                   @RequestParam("productImage") MultipartFile file,
                                   @RequestParam("imgName") String imgName,
                                   Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("categories", cservice.getAll());
            return "productsAdd";
        }

        Optional<Product> productOpt = pservice.fetchbyId(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(productDt.getName());
            product.setPrice(productDt.getPrice());
            product.setDescription(productDt.getDescription());
            product.setWeight(productDt.getWeight());
            product.setCategory(cservice.fetchbyId(productDt.getCategoryId()).orElse(null));

            String imageUUID;
            if (!file.isEmpty()) {
                imageUUID = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                Path path = Paths.get(uploadPath, imageUUID);
                Files.write(path, file.getBytes());
            } else {
                imageUUID = imgName;
            }

            product.setImageName(imageUUID);
            pservice.saveProduct(product);
            return "redirect:/admin/products";
        } else {
            return "error";
        }
    }
}