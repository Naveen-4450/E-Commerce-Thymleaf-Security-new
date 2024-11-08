package com.example.services;

import com.example.enums.Stock;
import com.example.models.dbModels.Cart;
import com.example.models.dbModels.Categories;
import com.example.models.dbModels.Products;
import com.example.models.dtoModels.ProductsDto;
import com.example.repositories.CartRepository;
import com.example.repositories.CategoryRepository;
import com.example.repositories.ProductsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class ProductService
{
    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private ProductsRepository productsRepo;

    @Autowired
    private CartRepository cartRepo;



    public Products addingProduct(ProductsDto pDto,  int cateId) throws IOException
    {
        log.info("Adding Product with category ID: {}",cateId);
        Products pd = new Products();
        pd.setProdName(pDto.getProdName());
        pd.setDescription(pDto.getDescription());
        pd.setPrice(pDto.getPrice());
        pd.setDiscount(pDto.getDiscount());
        pd.setCreatedAt(new Date());
        pd.setUpdatedAt(new Date());
        pd.setStockAvail(Stock.AVAILABLE);

        File uploadDir = new File(uploadPath);
        if(!uploadDir.exists()){
            log.info("Directory Created for Storing Image Url's at path :{}",uploadPath);
            uploadDir.mkdirs();
        }
        List<String> imagePaths = new ArrayList<>();
        MultipartFile[] images = pDto.getProdImages();
        for(MultipartFile pic:images)
        {
            String imageFileName = UUID.randomUUID().toString()+"_"+pic.getOriginalFilename();
            Path imagePath = Paths.get(uploadPath,imageFileName);
            Files.write(imagePath, pic.getBytes());
            String imageUrl = "/images/"+imageFileName;
            imagePaths.add(imageUrl);
            log.info("Image {} Converted to URL {}",pic.getOriginalFilename(),imageUrl);
        }

        pd.setProdImages(imagePaths);
        Categories category = categoryRepo.findById(cateId).get();//total Categories object will added in Product table but id only visable
        pd.setProductCategories(category);

        Products savedProduct = productsRepo.save(pd);
        log.info("Product {} Successfully Stored under category {}",savedProduct.getProdName(),category.getCategoryName());
        return savedProduct;
    }


    public Products updateProduct(int prodId, ProductsDto pDto, Stock status) throws IOException {
        log.info("Updating Product with ID: {}", prodId);
        Products product = productsRepo.findById(prodId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + prodId));

        product.setProdName(pDto.getProdName());
        product.setDescription(pDto.getDescription());
        product.setPrice(pDto.getPrice());
        product.setDiscount(pDto.getDiscount());
        product.setStockAvail(status);

        MultipartFile[] images = pDto.getProdImages();

        if (images != null && images.length > 0) { // if new images or not passed then it will retain the existing images
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile pic : images) {
                if (!pic.isEmpty()) {
                    String imageFileName = UUID.randomUUID().toString() + "_" + pic.getOriginalFilename();
                    Path imagePath = Paths.get(uploadPath, imageFileName);
                    Files.write(imagePath, pic.getBytes());
                    String imageUrl = "/images/" + imageFileName;
                    imagePaths.add(imageUrl);
                    log.info("Image {} converted to URL {}", pic.getOriginalFilename(), imageUrl);
                }
            }
            if (!imagePaths.isEmpty()) {
                product.setProdImages(imagePaths); // Only update if new images were actually uploaded
            }
        } else {
            log.info("No new Images Received, Retaining Existing Images");
        }

        product.setUpdatedAt(new Date());
        productsRepo.save(product);
        log.info("Product Updated Successfully with ID: {}", prodId);
        return product;
    }



    public String deletingProduct(int prodId)
    {
        Optional<Products> opt = productsRepo.findById(prodId);

        if(opt.isPresent()){
            log.info("Product is Found with Id: {}", prodId);
            Products product = opt.get();
            List<Cart> carts = cartRepo.findAll();
                for(Cart cart: carts){
                    if(cart.getProductQuantities().containsKey(product)){
                        cart.getProductQuantities().remove(product);
                        cartRepo.save(cart);
                    }
                }
                productsRepo.deleteById(prodId);
                log.info("Product is Found & deleted successfully");
                return prodId+" Product Deleted Successfully";
        }else {
            log.warn("Product Not Found with ID: {}", prodId);
            return "Record Not Found with your Id "+prodId;
        }
    }



    public Optional<Products> getProduct(int prodId) {

        log.info("fetching product by Product Id");
        Optional<Products> product = productsRepo.findById(prodId);
        if(product != null)
        {
            log.info("Product found with ID: {}", prodId);
            return product;
        }else{
            log.warn("Product not found with ID: {}", prodId);
            return null;
        }
    }


    public List<Products> getAllProducts() {
        List<Products> list = productsRepo.findAll();
        log.info("All product details retrieved successfully");
        return list;
    }



    public boolean removeProductImage(int prodId,String imageUrl)
    {
        Optional<Products> opt = productsRepo.findById(prodId);
        if(opt.isPresent()){
            Products product = opt.get();
            List<String> images = product.getProdImages();
            if (images.remove(imageUrl)){
                product.setProdImages(images);
                productsRepo.save(product);
                return true;
            }
        }
        return false;

    }

}
















/*    public ResponseEntity<String> updateProdAvail(int prodId) {

        Optional<Products> opt  = productsRepo.findById(prodId);
        if (opt.isPresent())
        {
            Products product = opt.get();
            product.setStockAvail(Stock.AVAILABLE);
            product.setUpdatedAt(new Date());
            productsRepo.save(product);
            log.info("Product with ID: {} found, status updated to 'AVAILABLE'", prodId);
            return new ResponseEntity<>("Product Status Updates Successfully", HttpStatus.OK);
        }else {
            log.warn("Product Not Found with ID : {}", prodId);
            return new ResponseEntity<>("Product Not Found with your Id---> "+prodId, HttpStatus.NOT_FOUND);
        }

    }


    public ResponseEntity<String> updateProdNotAvail(int prodId) {
        Optional<Products> opt  = productsRepo.findById(prodId);
        if (opt.isPresent())
        {
            Products product = opt.get();
            product.setStockAvail(Stock.NOT_AVAILABLE);
            product.setUpdatedAt(new Date());
            productsRepo.save(product);

            log.info("Product with ID: {} found, status updated to 'NOT_AVAILABLE'", prodId);
            return new ResponseEntity<>("Product Status Updates Successfully", HttpStatus.OK);
        }else {
            log.warn("Product is Not Found with ID: {}", prodId);
            return new ResponseEntity<>("Product Not Found with your Id---> "+prodId, HttpStatus.NOT_FOUND);
        }
    }



    public ResponseEntity<String> updateProductDiscount(int prodId, int discount) {
        Optional<Products> opt  = productsRepo.findById(prodId);
        if (opt.isPresent())
        {
            Products product = opt.get();
            product.setDiscount(discount);
            product.setUpdatedAt(new Date());
            productsRepo.save(product);

            log.info("Product discount updated successfully for product ID: {}", prodId);
            return new ResponseEntity<>("Product Discount Updated Successfully", HttpStatus.OK);
        }else {
            log.warn("Product Not Found with ID: {} to Update discount",prodId);
            return new ResponseEntity<>("Product Not Found with your Id---> "+prodId, HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<String> updateProductPrice(int prodId, Double price) {
        Optional<Products> opt  = productsRepo.findById(prodId);
        if (opt.isPresent())
        {
            Products product = opt.get();
            product.setPrice(price);
            product.setUpdatedAt(new Date());
            productsRepo.save(product);

            log.info("Product price updated successfully for product ID: {}", prodId);
            return new ResponseEntity<>("Product Price Updated Successfully", HttpStatus.OK);
        }else {
            log.warn("Product not Found with ID:{} to update price",prodId);
            return new ResponseEntity<>("Product Not Found with your Id---> "+prodId, HttpStatus.NOT_FOUND);
        }
    }*/
/* //adding Multiple Products at a time
   @Override
    public ResponseEntity<List<Products>> addingMultipleProducts(List<ProductsDto> productsDtos,int cateId) throws IOException
    {
        List<Products> listOfProducts = new ArrayList();

        for(ProductsDto pDto : productsDtos)
        {
            Products pd = new Products();
            pd.setProdName(pDto.getProdName());
            pd.setDescription(pDto.getDescription());
            pd.setPrice(pDto.getPrice());
            pd.setDiscount(pDto.getDiscount());
            pd.setCreatedAt(new Date());
            pd.setUpdatedAt(new Date());
            pd.setStockAvail(Stock.AVAILABLE);

            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()) {
                uploadDir.mkdirs();}

            List<String> imagePaths = new ArrayList<>();
            MultipartFile[] images = pDto.getProdImages();
            for(MultipartFile pic : images){

                String imageFileName = UUID.randomUUID().toString()+"_"+pic.getOriginalFilename();
                Path imagePath = Paths.get(uploadPath,imageFileName);
                Files.write(imagePath, pic.getBytes());
                String imageUrl = "/images/"+imageFileName;

                imagePaths.add(imageUrl);
            }

            pd.setProdImages(imagePaths);

            Categories category = categoryRepo.findById(cateId).get(); // for category object storing in Product table as a Id
            pd.setProductCategories(category);
           Products savedProduct = productsRepo.save(pd);
           listOfProducts.add(savedProduct);

        }
        return new ResponseEntity<>(listOfProducts,HttpStatus.CREATED); // returning the list of products
    }

*/
/*

@Override
    public ResponseEntity<List<Products>> addingMultipleProducts(List<ProductsDto> productsDtos,int cateId) throws IOException
    {
        List<Products> listOfProducts = new ArrayList();

        for(ProductsDto pDto : productsDtos)
        {
            Products pd = new Products();
            pd.setProdName(pDto.getProdName());
            pd.setDescription(pDto.getDescription());
            pd.setPrice(pDto.getPrice());
            pd.setDiscount(pDto.getDiscount());
            pd.setCreatedAt(new Date());
            pd.setUpdatedAt(new Date());
            pd.setStockAvail(Stock.AVAILABLE);

            File uploadDir = new File(uploadPath);
            if(!uploadDir.exists()) {
                uploadDir.mkdirs();}

            List<String> imagePaths = new ArrayList<>();
            MultipartFile[] images = pDto.getProdImages();
            for(MultipartFile pic : images){

                String imageFileName = UUID.randomUUID().toString()+"_"+pic.getOriginalFilename();
                Path imagePath = Paths.get(uploadPath,imageFileName);
                Files.write(imagePath, pic.getBytes());
                String imageUrl = "/images/"+imageFileName;

                imagePaths.add(imageUrl);
            }

            pd.setProdImages(imagePaths);

            Categories category = categoryRepo.findById(cateId).get(); // for category object storing in Product table as a Id
            pd.setProductCategories(category);

           Products savedProduct = productsRepo.save(pd);

           listOfProducts.add(savedProduct);

        }

        return new ResponseEntity<>(listOfProducts,HttpStatus.OK); // returning the list of products
    }
 */