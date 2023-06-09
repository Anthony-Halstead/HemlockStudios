package com.HemlockStudiosWebsite.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.HemlockStudiosWebsite.entity.Photo;
import com.HemlockStudiosWebsite.entity.Product;
import com.HemlockStudiosWebsite.entity.User;
import com.HemlockStudiosWebsite.enums.ProductEnums;
import com.HemlockStudiosWebsite.events.ProductMadeEvent;
import com.HemlockStudiosWebsite.repo.ProductRepo;


@Service
public class ProductService {

    @Autowired
    ProductRepo productRepo;

    @Autowired
    PhotoService photoService;

    @Autowired
    UserService userService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    public List<Product> getAll() {
        return productRepo.findAll();
    }

    public void deleteProductById(Integer id) {
        removeProductFromFavorites(id);
        productRepo.deleteById(id);
        System.out.println("Deleted product by ID success!");
    }

    public void createProduct(String description, Double price, String name, String[] imgUrls, String category,
            String subcategory, String size) {
        try {
            System.out.println("In the backend create product service start");
            Product product = new Product();
            product.setDescription(description);
            product.setPrice(price);
            product.setName(name);
            ProductEnums.Category categoryEnum = ProductEnums.Category.valueOf(category);
            ProductEnums.Subcategory subcategoryEnum = ProductEnums.Subcategory.valueOf(subcategory);
            ProductEnums.Size sizeEnum = ProductEnums.Size.valueOf(size);

            product.setCategory(categoryEnum);
            product.setSubcategory(subcategoryEnum);
            product.setSize(sizeEnum);
            for (String imgUrl : imgUrls) {
                System.out.println("Creating photo for url: " + imgUrl);
                Photo newPhoto = photoService.createPhoto(imgUrl);
                product.getPhotoAlbum().add(newPhoto);
            }
            productRepo.save(product);
            eventPublisher.publishEvent(new ProductMadeEvent(product));
            //send product email event
            System.out.println("Product saved successfully");
        } catch (Exception e) {
            System.out.println("Error during product creation: " + e.getMessage());
            e.printStackTrace();
        }
    }


    
    public Product save(Product product) {
        return productRepo.save(product);
    }


public void updateProduct(Integer id, String description, Double price, String name, String[] imgUrls,
        String category, String subcategory, String size, Double discount) {
    try {
        System.out.println("In the backend update product service start");
        Optional<Product> optionalProduct = productRepo.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setDescription(description);
            product.setPrice(price);
            product.setName(name);
            product.setDiscount(discount);
            ProductEnums.Category categoryEnum = ProductEnums.Category.valueOf(category);
            ProductEnums.Subcategory subcategoryEnum = ProductEnums.Subcategory.valueOf(subcategory);
            ProductEnums.Size sizeEnum = ProductEnums.Size.valueOf(size);
            product.setCategory(categoryEnum);
            product.setSubcategory(subcategoryEnum);
            product.setSize(sizeEnum);

            if (imgUrls != null) {
                for (String imgUrl : imgUrls) {
                    if (imgUrl != null && !photoExistsInList(product.getPhotoAlbum(), imgUrl)) {
                        System.out.println("Creating photo for url: " + imgUrl);
                        Photo newPhoto = photoService.createPhoto(imgUrl);
                        product.getPhotoAlbum().add(newPhoto);
                    }
                }
            }

            // Save the updated product
            productRepo.save(product);
            System.out.println("Product updated successfully");
        }
    } catch (Exception e) {
        System.out.println("Error during product update: " + e.getMessage());
        e.printStackTrace();
    }
}

private boolean photoExistsInList(List<Photo> photoList, String imgUrl) {
    for (Photo photo : photoList) {
        if (photo.getPhotoUrl().equals(imgUrl)) {
            return true;
        }
    }
    return false;
}

    public Product getProductById(Integer id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

    }

    public void removeProductFromFavorites(Integer productId) {
        System.out.println("in the remove product from favorites service path "+productId);
    
      
       User currentUser = userService.findUserByEmail();
    
        Product product = productRepo.findReference(productId);
    
        if (product == null) {
            System.out.println("Product not found");
            return; // or throw an exception
        }
    
        System.out.println("product Object"+product);
        currentUser.getFavoriteProducts().remove(product);
        System.out.println("product was removed");
        userService.save(currentUser);
        System.out.println("user was Saved");
    }

    public void addProductToFavorites(Integer productId) {
        User currentUser = userService.findUserByEmail();
        Product product = productRepo.findReference(productId);    
      currentUser.getFavoriteProducts().add(product);
      userService.save(currentUser);
    }


}
