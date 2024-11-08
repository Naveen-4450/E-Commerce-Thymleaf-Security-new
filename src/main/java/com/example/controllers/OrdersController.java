package com.example.controllers;

import com.example.enums.OrderStatus;
import com.example.models.dbModels.CustomerAddresses;
import com.example.models.dbModels.Customers;
import com.example.models.dbModels.Orders;
import com.example.models.dbModels.Products;
import com.example.models.dtoModels.FetchOrderDetailsDto;
import com.example.models.dtoModels.OneOrderDto;
import com.example.models.dtoModels.OrderItemsDto;
import com.example.models.dtoModels.OrdersDto;
import com.example.repositories.CustomerAddressRepository;
import com.example.repositories.CustomersRepository;
import com.example.repositories.ProductsRepository;
import com.example.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@Tag(name = "Orders Controller API's", description = "This API's are Managed to Customers Orders in Application")
@Slf4j
public class OrdersController
{
    @Autowired
    private OrderService orderSer;

    @Autowired
    private CustomersRepository custRepo;

    @Autowired
    private CustomerAddressRepository custAddRepo;

    @Autowired
    private ProductsRepository prodRepo;

    @GetMapping("/placeOneOrder")
    public String placeOneOrder(@RequestParam int custId,
                                @RequestParam int prodId,
                                Model model){
        Customers customer = custRepo.findByCustId(custId);
        Products product = prodRepo.findById(prodId).get();
        List<CustomerAddresses> addresses = customer.getCustomerAddresses();

        String status = product.getStockAvail().toString();
        if(status.equals("NOT_AVAILABLE")){
            String msg = "Product is Out-of-Stock, You cannot place Order";
            msg = URLEncoder.encode(msg,StandardCharsets.UTF_8);
            return "redirect:/displayProducts?custId="+custId+"&status=fail&msg="+msg;
        }else {
            OneOrderDto oDto = new OneOrderDto();
            oDto.setQuantity(1);
            model.addAttribute("oDto", oDto);
            model.addAttribute("customer", customer);
            model.addAttribute("product", product);
            model.addAttribute("addresses", addresses);
            return "placeOneOrder";
        }
    }

    @Operation(summary = "Placing the Order", description = "Customer Placing a single Order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order Placed Successfully"),
            @ApiResponse(responseCode = "404", description = "Customer Not Found/Address is not belond to Customer, Check the Customer Id")
    })
    @PostMapping("/placeOneOrder")
    public String placeOneOrder(@ModelAttribute OneOrderDto oDto, Model model)
    {
        log.info("Customer ID: {}", oDto.getCustomerId());
        log.info("Product ID: {}", oDto.getProductId());
        log.info("Quantity: {}", oDto.getQuantity());
        log.info("Address ID: {}", oDto.getCustAddId());
        log.info("Payment Method: {}", oDto.getPaymentMethod());

        orderSer.placeOneOrder(oDto);
        model.addAttribute("custId",oDto.getCustomerId());
        return "orderSuccess";
    }


    @Operation(summary = "Get one Order Details by orderId", description = "Get Order Details by using Order Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order Details Retrieved"),
            @ApiResponse(responseCode = "404", description = "Order Not Found with that Order Id")
    })
    @GetMapping("/viewOneOrder/{orderId}")//for Customer View Order Details
    public String getOrderById(@PathVariable int orderId, Model model)
    {
        FetchOrderDetailsDto orderDetails = orderSer.getOrderById(orderId);
        CustomerAddresses address = custAddRepo.findByAddId(orderDetails.getAddId());

        model.addAttribute("orderDetails",orderDetails);
        model.addAttribute("address", address);
        return "viewOneOrder";
    }

    @Operation(summary = "Get All Orders of a Customer by customer Id", description = "Get All Orders History of a Customer by customer Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ALl Order Details Retrieved"),
            @ApiResponse(responseCode = "404", description = "Customer Not Found/ Customer don't have any order History")
    })
    @GetMapping("/viewAllOrders/{custId}")// for customers
    public String getOrderByCustomerId(@PathVariable int custId,Model model)
    {
        List<FetchOrderDetailsDto> orders = orderSer.getOrderByCustomerId(custId);
        model.addAttribute("orders", orders);
        model.addAttribute("custId", custId);
        return "viewAllOrders";
    }



    @Operation(summary = "Update Order Status by orderId", description = "Updating an Order status by order Id & Pass the Status value like(SHIPPED,DELIVERED,CANCELLED,ORDERED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order Status Updated Successfully"),
            @ApiResponse(responseCode = "404", description = "Order Not Found to Update Status (check-->orderId)"),
            @ApiResponse(responseCode = "406", description = "Invalid Status Value")
    })
    @PutMapping("/orderStatus/{orderId}")// for admin
    public String updateOrderStatus(@PathVariable int orderId, @RequestParam String status,
                                    @RequestParam int custId)
    {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        String msg = orderSer.updateOrderStatus(orderId, orderStatus);
        if(msg != null) {
            msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
            return "redirect:/customerAllOrders?status=success&msg=" + msg + "&custId=" + custId;
        }else {
            msg = "Unable to Update Status to : "+ status;
            msg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
            return "redirect:/customerAllOrders?status=fail&msg=" + msg + "&custId=" + custId;
        }
    }




    @Operation(summary = "Creating/Placing Order", description = "Customer Placing a Order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order Placed Successfully"),
            @ApiResponse(responseCode = "404", description = "Customer Not Found/Address is not belond to Customer, Check the Customer Id")
    })
    @PostMapping("/placeMultiOrders")// for customers , but not implemented
    public String createOrders(@ModelAttribute OrdersDto oDto)
    {
        orderSer.createOrders(oDto);
        return "orderSuccess";
    }




}























/*    @PutMapping("/orderShipped/{orderId}")
    public ResponseEntity<String> orderStatusShipped(@PathVariable int orderId)
    {

            return orderSer.updateOrderStatus(orderId);
    }

    @PutMapping("/orderDelivered/{orderId}")
    public ResponseEntity<String> orderStatusDelivered(@PathVariable int orderId)
    {

        return orderSer.updateOrderStatus(orderId);
    }

    @PutMapping("/orderCancelled/{orderId}")
    public ResponseEntity<String> orderStatusCancelled(@PathVariable int orderId)
    {

        return orderSer.updateOrderStatus(orderId);
    }*/