package com.example.models.dtoModels;

import com.example.enums.Stock;
import com.example.models.dbModels.Customers;
import com.example.models.dbModels.Products;
import lombok.Data;


import java.util.Date;
import java.util.List;

@Data
public class WishlistDto  //Dto created for getting the data based on CustId. without this lazyLoading Exceptions coming
{
    private Integer wishlistId;
    private Date addedDate;

    private String prodName;
    private String description;
    private Double price;
    private Stock stockAvail;
    private List<String> prodImages;

    private Integer custId;
    private Integer prodId;

}
