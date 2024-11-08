package com.example.services;

import com.example.models.dbModels.Customers;
import com.example.models.dbModels.Products;
import com.example.models.dbModels.Wishlist;
import com.example.models.dtoModels.WishlistDto;
import com.example.repositories.CustomersRepository;
import com.example.repositories.ProductsRepository;
import com.example.repositories.WishlistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WishlistService
{
    @Autowired
    private CustomersRepository custRepo;

    @Autowired
    private ProductsRepository productsRepo;

    @Autowired
    private WishlistRepository wishlistRepo;


    public Wishlist addingProduct(int custId, int prodId)
    {
        log.info("Adding product ID:{} to customer wishlist",prodId);
        Customers customer = custRepo.findById(custId).orElse(null);
        Products product = productsRepo.findById(prodId).orElse(null);

        List<Wishlist> wishlists = wishlistRepo.findAll();
        boolean isAvail = false;
        for(Wishlist w : wishlists) {
            if(w.getCustomerId().equals(customer) && w.getProductId().equals(product)) {
                isAvail = true;
                break;
            }
        }
        if(isAvail == false)
        {
            Wishlist wishlist = new Wishlist();
            wishlist.setCustomerId(customer);
            wishlist.setProductId(product);
            wishlist.setAddedDate(new Date());
            return wishlistRepo.save(wishlist);
        }else{
            return null;
        }

        /*
        // below code is for unique contraints annotation
        if (customer == null || product == null) {
            log.error("Customer ID: {} or Product ID: {} not found", custId, prodId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Wishlist wishlist = new Wishlist();
        wishlist.setCustomerId(customer);
        wishlist.setProductId(product);
        wishlist.setAddedDate(new Date());

        log.info("Product ID: {} added to wishlist for customer ID: {}", prodId, custId);
        return new ResponseEntity<>(wishlistRepo.save(wishlist), HttpStatus.OK);

*/
    }

    public List<WishlistDto> getWishlistByCustomerId(int custId)
    {
        List<Wishlist> wishlists = wishlistRepo.findByCustomerIdCustId(custId);
        log.info("Retrieved wishlist for customer with ID: {}", custId);
        return wishlists.stream().map(this::convertToDto).collect(Collectors.toList());
    }


    public String removeProdFromWishlist(int custId, int prodId)
    {
        List<Wishlist> list = wishlistRepo.findByCustomerIdCustId(custId);
        boolean productFound = false;

        for(Wishlist wishlist : list) {
            if(wishlist.getProductId().getProdId().equals(prodId)) {
                wishlistRepo.delete(wishlist);
                log.info("Removed product with ID: {} from wishlist for customer ID:{}",prodId,custId);
               productFound = true;
               break;
            }
        }
       if(productFound){
           return "Product Removed from Wishlist";
       }else {
           log.warn("Product with id {} not found in wishlist",prodId);
           return "Product Not_Found in Wishlist";
       }
    }


    public ResponseEntity<String> clearWishlist(int custId)
    {
        List<Wishlist> list = wishlistRepo.findByCustomerIdCustId(custId);
        if (list.isEmpty()){
            log.info("No products available in wishlist for customer with ID:{}",custId);
            return new ResponseEntity<>("No products are available in "+custId+" customer wishlist",HttpStatus.OK);
        }else {
            try {
                wishlistRepo.deleteAll(list);
                log.info("Cleared all products from wishlist for customer ID: {}", custId);
                return new ResponseEntity<>("Total wishlist is cleared", HttpStatus.OK);
            } catch (Exception e) {
                log.error("Error clearing wishlist for customer ID: {}", custId, e);
                return new ResponseEntity<>("Failed to clear the wishlist", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }



    private WishlistDto convertToDto(Wishlist wishlist)
    {
        WishlistDto dto = new WishlistDto();
        dto.setWishlistId(wishlist.getWishlistId());
        dto.setAddedDate(wishlist.getAddedDate());

        //Products Fields
        dto.setProdName(wishlist.getProductId().getProdName());
        dto.setDescription(wishlist.getProductId().getDescription());
        dto.setPrice(wishlist.getProductId().getPrice());
        dto.setStockAvail(wishlist.getProductId().getStockAvail());
        dto.setProdImages(wishlist.getProductId().getProdImages());

        //Customer fields
        dto.setCustId(wishlist.getCustomerId().getCustId());
        dto.setProdId(wishlist.getProductId().getProdId());
        /*// these details are not required to fetch along with wishlists
        dto.setFirstName(wishlist.getCustomerId().getFirstName());
        dto.setLastName(wishlist.getCustomerId().getLastName());
        dto.setUserName(wishlist.getCustomerId().getUserName());
        dto.setPassword(wishlist.getCustomerId().getPassword());
        dto.setMobile(wishlist.getCustomerId().getMobile());
        dto.setEmail(wishlist.getCustomerId().getEmail());  */
        return dto;
    }

}


