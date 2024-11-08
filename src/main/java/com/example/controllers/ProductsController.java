package com.example.controllers;

import com.example.enums.Stock;
import com.example.models.dbModels.Categories;
import com.example.models.dbModels.Products;
import com.example.models.dtoModels.CartDto;
import com.example.models.dtoModels.ProductsDto;
import com.example.repositories.CategoryRepository;
import com.example.repositories.ProductsRepository;
import com.example.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/")
@Tag(name = "Products Controller API's", description = "This API's are Managed to Products in Application")
public class ProductsController {
    @Autowired
    private ProductService productSer;

    @Autowired
    private CategoryRepository cateRepo;

    @Autowired
    private ProductsRepository productRepo;


    @GetMapping("/addProduct")
    public String addingProduct(Model model)
    {
        List<Categories> categories = cateRepo.findAll();
        model.addAttribute("categories",categories);
        return "addProduct";
    }
    @Operation(summary = "Add a new Product in Application", description = "Adds a new Product Details in to Application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",description = "Product Details are Entered Successfully")
    })
    @PostMapping(value = "/addProduct")
    public String addingProduct(@RequestParam String prodName,
                                                  @RequestParam String description,
                                                  @RequestParam Double price,
                                                  @RequestParam Integer discount,
                                                  @RequestParam MultipartFile[] prodImages,
                                                  @RequestParam int cateId) throws IOException  {
        ProductsDto pDto = new ProductsDto();
        pDto.setProdName(prodName);
        pDto.setDescription(description);
        pDto.setPrice(price);
        pDto.setDiscount(discount);
        pDto.setProdImages(prodImages);
        Products product = productSer.addingProduct(pDto, cateId);

        String msg = "Product Details Added Successfully and ProductId is==> "+product.getProdId();
        msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
        return "redirect:/addProduct?status=success&msg="+msg;
    }



    @Operation(summary = "Fetch one Product by Id", description = "Get One Product Details by it's Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Product Details Retrieved"),
            @ApiResponse(responseCode = "404", description = "Product Not Found to Retrieve")
    })
    @GetMapping("/viewOneProduct/{prodId}")
    public String getProduct(@PathVariable int prodId, Model model) {
        Optional<Products> productOpt = productSer.getProduct(prodId);

        if (productOpt.isPresent()) {
            Products product = productOpt.get();
            model.addAttribute("product", product);
            model.addAttribute("status", "success");
        } else {
            model.addAttribute("msg", "Product not Found. Please pass Valid Id....");
            model.addAttribute("status", "fail");
        }
        return "viewOneProduct";
    }


    @Operation(summary = "Fetch All Product Details", description = "Get All Product Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Products Details are Retrieved")
    })
    @GetMapping("/viewProducts")
    public String getAllProducts(Model model)
    {
        List<Products> products = productSer.getAllProducts();
        model.addAttribute("products",products);
        return "viewProducts";
    }


    @Operation(summary = "Delete Product Details by Id", description = "Deleting the Product Details by Using Product Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product Deleted Successfully"),
            @ApiResponse(responseCode = "404", description = "Product Not Found to Delete")
    })
    @DeleteMapping("/deleteProduct/{prodId}")
    public String deletingProduct(@PathVariable int prodId)
    {
        String msg = productSer.deletingProduct(prodId);
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/viewProducts?status=fail&msg=" + msg; //passing fail bcz of red
    }



    @GetMapping("/updateProduct/{prodId}")
    public String updateProduct(@PathVariable int prodId, Model model)
    {
        Products product = productRepo.findById(prodId).get();
        model.addAttribute("product",product);
        return "updateProduct";
    }

    @PostMapping("/updateProduct/{prodId}")
    public String updateProduct(@PathVariable int prodId,
                                @ModelAttribute ProductsDto pDto,
                                @RequestParam String stockAvail) throws IOException {
        Stock status = Stock.valueOf(stockAvail);
        Products product = productSer.updateProduct(prodId,pDto,status);

        String msg = "Product Details Updated Successfully";
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/viewProducts?status=success&msg="+msg;  // after update it returning all products page
    }


    @GetMapping("/updateOneProduct/{prodId}")
    public String updateOneProduct(@PathVariable int prodId, Model model)
    {
        Products product = productRepo.findById(prodId).get();
        model.addAttribute("product",product);
        return "updateOneProduct";
    }
    @PostMapping("/updateOneProduct/{prodId}")
    public String updateOneProduct(@PathVariable int prodId,
                                      @ModelAttribute ProductsDto pDto,
                                      @RequestParam String stockAvail) throws IOException {
        Stock status = Stock.valueOf(stockAvail);
        Products product = productSer.updateProduct(prodId,pDto,status);

        String msg = "Product Details Updated Successfully";
        msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
        return "redirect:/viewOneProduct/"+prodId+"?status=success&msg="+msg;  // after update it returning the single product page
    }




    // Below API is for Delete Product Images


    @GetMapping("/removeProductImage")
    public String removeProductImage(@RequestParam("prodId") int prodId,
                                     @RequestParam("imageUrl") String imageUrl)
    {
        boolean isRemoved = productSer.removeProductImage(prodId, imageUrl);
        if(isRemoved){
            return "redirect:/updateProduct/"+prodId;           //it return to all products
        }else {
            return "redirect:/updateProduct/"+prodId;
        }
    }


    @GetMapping("/removeOneProductImage")
    public String removeOneProductImage(@RequestParam("prodId") int prodId,
                                        @RequestParam("imageUrl") String imageUrl)
    {
        boolean isRemoved = productSer.removeProductImage(prodId, imageUrl);
        if(isRemoved){
            return "redirect:/updateOneProduct/"+prodId;        //at last it returns to that particular products
        }else {
            return "redirect:/updateProduct/"+prodId;
        }
    }

}













  /*
  // 4 api's for update statuses 2 and discount and price
  @Operation(summary = "Status Update to ***Available*** ", description = "Status Updating of a product is Available or not")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "roduct Status Updated Successfully"),
            @ApiResponse(responseCode = "404", description = "Product is Not Found to update Status")
    })
    @PutMapping("/productAvail/{prodId}")
    public ResponseEntity<String> updateProdAvail(@PathVariable int prodId) {
        return productSer.updateProdAvail(prodId);
    }



    @Operation(summary = "Status Update to ***Not_Available***", description = "Status Updating of a product is Available or not")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product Status Updated Successfully"),
            @ApiResponse(responseCode = "404", description = "Product is Not Found to update Status")
    })
    @PutMapping("/productNotAvail/{prodId}")
    public ResponseEntity<String> updateProdNotAvail(@PathVariable int prodId) {
        return productSer.updateProdNotAvail(prodId);
    }


    @Operation(summary = "Update Product Discount by Id", description = "Update the Existing Product Discount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product Discount Updated Successfully"),
            @ApiResponse(responseCode = "404", description = "Product Not Found to update Discount")
    })
    @PutMapping("/productDiscount/{prodId}")
    public ResponseEntity<String> updateProductDiscount(@PathVariable int prodId, @RequestParam int discount)
    {
        return productSer.updateProductDiscount(prodId,discount);
    }



    @Operation(summary = "Update Product Price By Id", description = "Update the Existing Product Price")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product Price Updated Successfully"),
            @ApiResponse(responseCode = "404", description = "Product Not Found to update Price")
    })
    @PutMapping("/productPrice/{prodId}")
    public ResponseEntity<String> updateProductPrice(@PathVariable int prodId, @RequestParam  Double price)
    {
        return productSer.updateProductPrice(prodId, price);
    }
*/
 /*
  @Operation(summary = "Add Multiple Products at a time", description = "Adds Multiple Product Details at a time in to Application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All Product Details are Entered Successfully")
    })
    @PostMapping("/addmultiproducts/{cateId}")
    public ResponseEntity<List<Products>> addingMultipleProducts(@RequestParam List<String> prodName,
                                                                 @RequestParam List<String> description,
                                                                 @RequestParam List<Double> price,
                                                                 @RequestParam List<Integer> discount,
                                                                 @RequestParam List<MultipartFile[]> prodImages,
                                                                 @PathVariable int cateId) throws IOException {
        List<ProductsDto> productsDtos = new ArrayList<>();
        for (int i = 0; i < prodName.size(); i++) {
            ProductsDto pDto = new ProductsDto();

            pDto.setProdName(prodName.get(i));
            pDto.setDescription(description.get(i));
            pDto.setPrice(price.get(i));
            pDto.setDiscount(discount.get(i));
            pDto.setProdImages(prodImages.get(i)); // Multiple images not adding for each product
            productsDtos.add(pDto);
        }
        return productSer.addingMultipleProducts(productsDtos, cateId);
    }

*/