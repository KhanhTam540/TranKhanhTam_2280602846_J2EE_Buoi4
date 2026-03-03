package org.example;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private static List<Product> products = new ArrayList<>();

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", products);
        return "product-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        return "product-create";
    }

    @PostMapping("/create")
    public String save(@Valid @ModelAttribute("product") Product product,
                       BindingResult result,
                       @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        if (result.hasErrors()) {
            return "product-create";
        }

        // Xử lý Upload Hình ảnh vào thư mục vật lý
        if (!imageFile.isEmpty()) {
            String fileName = imageFile.getOriginalFilename();
            Path uploadPath = Paths.get("uploads");

            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Copy file vào thư mục uploads
            try (var inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            // Lưu tên file để hiển thị
            product.setImage(fileName);
        }

        product.setId(products.size() + 1);
        products.add(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        Product product = products.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
        model.addAttribute("product", product);
        return "product-edit";
    }

    @PostMapping("/edit")
    public String update(@Valid @ModelAttribute("product") Product product,
                         BindingResult result,
                         @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        if (result.hasErrors()) {
            return "product-edit";
        }

        if (!imageFile.isEmpty()) {
            String fileName = imageFile.getOriginalFilename();
            Path uploadPath = Paths.get("uploads");
            Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            product.setImage(fileName);
        }

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == product.getId()) {
                // Giữ lại ảnh cũ nếu người dùng không upload ảnh mới khi sửa
                if (imageFile.isEmpty()) {
                    product.setImage(products.get(i).getImage());
                }
                products.set(i, product);
                break;
            }
        }
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        products.removeIf(p -> p.getId() == id);
        return "redirect:/products";
    }
}