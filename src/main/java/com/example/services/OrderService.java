package com.example.services;

import com.example.enums.OrderStatus;
import com.example.enums.PaymentStatus;
import com.example.exceptionhandling.CustomerNotFoundException;
import com.example.exceptionhandling.ResourceNotFoundException;
import com.example.models.dbModels.*;
import com.example.models.dtoModels.FetchOrderDetailsDto;
import com.example.models.dtoModels.OneOrderDto;
import com.example.models.dtoModels.OrderItemsDto;
import com.example.models.dtoModels.OrdersDto;
import com.example.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService
{
    @Autowired
    private OrdersRepository ordersRepo;

    @Autowired
    private CustomersRepository custRepo;

    @Autowired
    private ProductsRepository productsRepo;

    @Autowired
    private OrderItemsRepository orderItemsRepo;

    @Autowired
    private CustomerAddressRepository custAddRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private DeliveredAddressRepository deliveredAddRepo;


    private Double discountCalculation(Double price, Integer discount) {
        Double val = price - (price * discount/100); //if the discount is zero it returns the product original price
        return  val;
    }


    //single product Order
    public String placeOneOrder(OneOrderDto oDto)
    {
        log.info("Order Placing Process Started");
        CustomerAddresses address = custAddRepo.findByAddId(oDto.getCustAddId()); // for Address
        Optional<Customers> customer = custRepo.findById(oDto.getCustomerId());

        // Check if the address belongs to the customer
        if (address == null || !address.getCustomerId().equals(customer.get())) {
            String errorMsg = "Address not found for customer with ID : " + oDto.getCustomerId();
            log.error(errorMsg);
            return errorMsg;
        }

        if (customer.isPresent())
        {
            log.info("Customer also present with the ID: {}", oDto.getCustomerId());
            // Create and set up a new order
            Orders order = new Orders();
            order.setOrderedDate(new Date());
            order.setStatus(OrderStatus.ORDERED);
            order.setCustomerId(customer.get());
            order.setCustAddId(address);

            // Handling the single OrderItem
            log.info("Processing the Order Item...");
            Optional<Products> product = productsRepo.findById(oDto.getProductId());

            if (product.isPresent()) {
                log.info("Product found with ID: {}", oDto.getProductId());

                OrderItems orderItem = new OrderItems();
                orderItem.setQuantity(oDto.getQuantity());
                orderItem.setItemsPrice(product.get().getPrice() * oDto.getQuantity()); // setting the original costs of products

                // Calculate the price based on the product's price and discount
                Double finalPrice = discountCalculation(product.get().getPrice(), product.get().getDiscount());
                orderItem.setFinalAmount(finalPrice * oDto.getQuantity()); // calculate the final price based on the items quantity

                orderItem.setOrderId(order); // set the Order
                orderItem.setProductId(product.get()); // set the product

                List<OrderItems> orderItemsList = new ArrayList<>();
                orderItemsList.add(orderItem);
                order.setOrderItems(orderItemsList); // Setting the collected list to the order

                // Calculate total amount
                Double totalAmt = finalPrice * oDto.getQuantity();
                order.setTotalAmount(totalAmt);

                // Save the order in the database
                Orders savedOrder = ordersRepo.save(order);
                log.info("Order saved with ID: {}", savedOrder.getOrderId());

                // Handle payment details
                Payments payment = new Payments();
                payment.setAmount(totalAmt);
                payment.setPaymentDate(new Date());
                payment.setPaymentMethod(oDto.getPaymentMethod()); // from dto
                payment.setStatus(PaymentStatus.SUCCESSFUL);
                payment.setOrderId(savedOrder); // orderId Saved
                paymentRepo.save(payment);

                log.info("Order successfully placed with order ID : {}", savedOrder.getOrderId());
                return "Woohoo!!.....\nYour Order Placed Successfully. Order will be Delivered in Next 4-5 Working Days";
            } else {
                log.warn("Product not found with ID: {}", oDto.getProductId());
                return "Product Not Found.";
            }
        } else {
            String errorMsg = "Customer Not Found with ID---->" + oDto.getCustomerId();
            log.error(errorMsg);
            throw new CustomerNotFoundException(errorMsg);
        }
    }

    public FetchOrderDetailsDto getOrderById(int orderId)
    {
        log.info("Fetching order details for order ID: {}", orderId);
        Orders order = ordersRepo.findByOrderId(orderId);
        if(order != null)
        {
            FetchOrderDetailsDto orderDetails = convertToDto(order);
            log.info("Order details retrieved successfully for order ID: {}", orderId);
            return orderDetails;
        }else{
            String errorMsg = "Order not found with ID--->"+orderId;
            log.error(errorMsg);
            throw new ResourceNotFoundException(errorMsg);
        }
    }


    public List<FetchOrderDetailsDto> getOrderByCustomerId(int custId)
    {
        log.info("Fetching all orders for customer ID: {}", custId);
        Customers customer = custRepo.findByCustId(custId);
        if(customer != null) {
            List<Orders> orders = ordersRepo.findByCustomerIdCustId(custId);
            if (!orders.isEmpty()) {
                List<FetchOrderDetailsDto> allOrders = orders.stream().map(this::convertToDto).collect(Collectors.toList());
                log.info("Orders retrieved successfully for customer ID: {}", custId);
                return allOrders;
            } else {
                log.warn("Orders list is Empty for Customer ID: {}", custId);
                return null;
            }
        }else{
            String errorMsg = "Customer Not Found with ID: " + custId;
            log.error(errorMsg);
            throw new CustomerNotFoundException(errorMsg);
        }
    }



    public String updateOrderStatus(int orderId, OrderStatus newStatus) {
        log.info("Updating status for order ID: {} to {}", orderId, newStatus);
        Orders order = ordersRepo.findByOrderId(orderId);
        OrderStatus currentStatus = order.getStatus();

        if(currentStatus == OrderStatus.ORDERED){
            if(newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED){
                order.setStatus(newStatus);
                ordersRepo.save(order);
            }else {return null;}
        }else if (currentStatus == OrderStatus.SHIPPED)
        {
            if(newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED)
            {
                if(newStatus == OrderStatus.DELIVERED) {
                    if(order.getDeliveredAddress() == null) { //If the order is updated to "DELIVERED" again for any other reason, this check prevents the creation of another DeliveredAddress record.
                        order.setStatus(newStatus);
                        ordersRepo.save(order);
                        CustomerAddresses address = order.getCustAddId();
                        DeliveredAddress delAdd = new DeliveredAddress();
                        delAdd.setAddLine(address.getAddLine());
                        delAdd.setCity(address.getCity());
                        delAdd.setPincode(address.getPincode());
                        delAdd.setState(address.getState());
                        delAdd.setCountry(address.getCountry());
                        delAdd.setOrderId(order);
                        deliveredAddRepo.save(delAdd);
                        log.info("DeliveredAddress created and saved for order ID : {}", orderId);
                    }
                } else  {
                    order.setStatus(newStatus);
                    ordersRepo.save(order);
                }
            }else {
                return null;
            }
        } else {
            return null;
        }
        log.info("Order status updated successfully for order ID: {}", orderId);
        return "Order Status Updated to : " + newStatus;
    }







    public FetchOrderDetailsDto convertToDto(Orders order)
    {
        log.info("Converting order ID: {} to DTO", order.getOrderId());
        FetchOrderDetailsDto dto = new FetchOrderDetailsDto();
        dto.setOrderId(order.getOrderId());
        dto.setOrderedDate(order.getOrderedDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());

        dto.setCustomerId(order.getCustomerId().getCustId());
        dto.setAddId(order.getCustAddId().getAddId());

        //payment Details Returned
        dto.setPaymentMethod(order.getPayments().getPaymentMethod());
        dto.setPaymentStatus(order.getPayments().getStatus());

        List<FetchOrderDetailsDto.OrdItemDto> orderItemsDto = order.getOrderItems().stream().map(orderItem->{
            FetchOrderDetailsDto.OrdItemDto ordItemDto = new FetchOrderDetailsDto.OrdItemDto();
            ordItemDto.setId(orderItem.getId());
            ordItemDto.setQuantity(orderItem.getQuantity());
            ordItemDto.setItemsPrice(orderItem.getItemsPrice());
            ordItemDto.setFinalAmount(orderItem.getFinalAmount());

            Products product = orderItem.getProductId();
            FetchOrderDetailsDto.ProdDto prodDto = new FetchOrderDetailsDto.ProdDto();
            prodDto.setProductId(product.getProdId());
            prodDto.setProdName(product.getProdName());
            prodDto.setDescription(product.getDescription());
            prodDto.setPrice(product.getPrice());
            prodDto.setProdImages(product.getProdImages());
            ordItemDto.setProduct(prodDto); //setting product in OrdItemDto innerClass

            return ordItemDto;
                }).collect(Collectors.toList());

        dto.setOrderItems(orderItemsDto);
        return dto;
    }







    //Order Multiple Products At a time
    public String createOrders(OrdersDto oDto) {
        log.info("Placing an Order");
        CustomerAddresses address = custAddRepo.findByAddId(oDto.getCustAddId()); // for Address
        Optional<Customers> customer = custRepo.findById(oDto.getCustomerId());

        log.info("Address customer verified");

        // Check if the address belongs to the customer
        if (address == null || !address.getCustomerId().equals(customer.get())) {
            String errorMsg = "Address not found for customer with ID: " + oDto.getCustomerId();
            log.error(errorMsg);
            return errorMsg;
        }

        if (customer.isPresent()) {
            log.info("Customer also present with the ID");
            Orders order = new Orders();
            order.setOrderedDate(new Date());
            order.setStatus(OrderStatus.ORDERED);
            order.setCustomerId(customer.get());
            order.setCustAddId(address);
            log.info("Order details saved");

            // Handling the OrderItems
            log.info("Processing the Order Items....");
            List<OrderItems> orderItemsList = new ArrayList<>();

            for (OrderItemsDto itemDto : oDto.getOrderItems()) {
                Optional<Products> product = productsRepo.findById(itemDto.getProductId());
                log.info("Product Retrieving");

                if (product.isPresent()) {
                    log.info("Product also found");
                    OrderItems orderItem = new OrderItems();
                    log.info("Order Items Object Created");
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setItemsPrice(product.get().getPrice() * itemDto.getQuantity()); // setting the original costs of products

                    // Calculate the price based on the product's price and discount
                    Double finalPrice = discountCalculation(product.get().getPrice(), product.get().getDiscount());
                    orderItem.setFinalAmount(finalPrice * itemDto.getQuantity()); // calculate the final price based on the items Quantity

                    orderItem.setOrderId(order); // set the Order
                    orderItem.setProductId(product.get()); // set the product
                    orderItemsList.add(orderItem); // add to the list
                } else {
                    log.warn("Product is not found");
                }
            }
            log.info("Order items Handled------");
            order.setOrderItems(orderItemsList); // Setting the collected list to the order

            Double totalAmt = 0.0;
            for (OrderItems orderItem : orderItemsList) {
                totalAmt = totalAmt + orderItem.getFinalAmount();
            }
            order.setTotalAmount(totalAmt); // after selecting all products and quantity then setting the Costs
            Orders savedOrder = ordersRepo.save(order); // last, order saved in db

            Payments payment = new Payments();
            payment.setAmount(totalAmt);
            payment.setPaymentDate(new Date());
            payment.setPaymentMethod(oDto.getPayment().getPaymentMethod()); // from json
            payment.setStatus(PaymentStatus.SUCCESSFUL);
            payment.setOrderId(savedOrder); // orderId Saved
            paymentRepo.save(payment);

            log.info("Order successfully placed with order ID:{}", savedOrder.getOrderId());
            return "Woohoo!!.....\nYour Order Placed Successfully. Order will be Delivered in Next 4-5 Working Days";
        } else {
            String errorMsg = "Customer Not Found with ID--->" + oDto.getCustomerId();
            log.error(errorMsg);
            throw new CustomerNotFoundException(errorMsg);
        }
    }






}















  /*  public String createOrder(OrdersDto oDto)
    {
        log.info("Placing a Order");
        CustomerAddresses address =  custAddRepo.findByAddId(oDto.getCustAddId()); // for Address
        Optional<Customers> customer = custRepo.findById(oDto.getCustomerId());

        log.info("Address custoemer verified");

        // Check if the address belongs to the customer
        if(address == null || !address.getCustomerId().equals(customer.get())) {
            String errorMsg = "Address not found for customer with ID: " + oDto.getCustomerId();
            log.error(errorMsg);
            return errorMsg;
        }

        if(customer.isPresent())
        {
            log.info("customer also present with the ID");
            Orders order = new Orders();
            order.setOrderedDate(new Date());
            order.setStatus(OrderStatus.ORDERED);
            order.setCustomerId(customer.get());
            order.setCustAddId(address);
            log.info("Order details saved");

            //Handling the OrderItems
            log.info("Processing the Order Items....");
            List<OrderItems> orderItemsList = oDto.getOrderItems().stream().map(itemDto->{
                Optional<Products> product = productsRepo.findById(itemDto.getProductId());
                log.info("Product REtrieving");
                    if(product.isPresent()) {
                        log.info("Product also founded");
                        OrderItems orderItem = new OrderItems();
                        log.info("Order Items Object Created");
                        orderItem.setQuantity(itemDto.getQuantity());
                        orderItem.setItemsPrice(product.get().getPrice() * itemDto.getQuantity());          // setting the original costs of products

                        // Calculate the price based on the product's price and discount
                        Double finalPrice = discountCalculation(product.get().getPrice(), product.get().getDiscount());
                        orderItem.setFinalAmount(finalPrice * itemDto.getQuantity()); // calculate the final price based on the items Quantity

                        orderItem.setOrderId(order);           // set the Order
                        orderItem.setProductId(product.get());  // set the product
                        return orderItem;                       // it will Returning to the stream
                    }
                    log.warn("Product is not found");
                    return null; // if product is not there with the given prodId it return null to Stream
                    }).filter(orderItem -> orderItem != null).collect(Collectors.toList());
            log.info("Order items Handled------");
            order.setOrderItems(orderItemsList); // Setting the collected list to the order

            Double totalAmt = 0.0;
            for(OrderItems orderItem : orderItemsList) {
                totalAmt = totalAmt+ orderItem.getFinalAmount();
            }
            order.setTotalAmount(totalAmt);// after selecting all products and quantity then setting the Costs
            Orders savedOrder =ordersRepo.save(order); // last, order saved in db

            Payments payment = new Payments();
            payment.setAmount(totalAmt);
            payment.setPaymentDate(new Date());
            payment.setPaymentMethod(oDto.getPayment().getPaymentMethod()); //from json
            payment.setStatus(PaymentStatus.SUCCESSFUL);
            payment.setOrderId(savedOrder); //orderId Saved
            paymentRepo.save(payment);

            log.info("Order successfully placed with order ID:{}",savedOrder.getOrderId());
            return "Woohoo!!.....\nYour Order Placed Successfully. Order will be Delivered in Next 4-5 Working Days";
        }else {
            String errorMsg = "Customer Not Found with ID--->" + oDto.getCustomerId();
            log.error(errorMsg);
            throw new CustomerNotFoundException(errorMsg);
        }
    }*/










/*
// while order placing ,product is available or not checking below code implement this code in ur method
CustomerAddresses address = custAddRepo.findByAddId(oDto.getCustAddId());
Optional<Customers> customer = custRepo.findById(oDto.getCustomerId());

if (customer.isPresent()) {
        // Check if the address belongs to the customer
        if (address == null || !address.getCustomerId().equals(customer.get())) {
String errorMsg = "Address not found for customer with ID: " + oDto.getCustomerId();
        log.error(errorMsg);
        return new ResponseEntity<>(errorMsg, HttpStatus.NOT_FOUND);
        }

        // Verify that all products exist before proceeding
        for (OrderItemsDto itemDto : oDto.getOrderItems()) {
Optional<Products> product = productsRepo.findById(itemDto.getProductId());
        if (!product.isPresent()) {
String errorMsg = "Product not found with ID: " + itemDto.getProductId();
            log.error(errorMsg);
            return new ResponseEntity<>(errorMsg, HttpStatus.NOT_FOUND);
        }
        }

Orders order = new Orders();
    order.setOrderedDate(new Date());
        order.setStatus(OrderStatus.ORDERED);
    order.setCustomerId(customer.get());
        order.setCustAddId(address);

// Handling the OrderItems
    log.info("Processing the Order Items....");
List<OrderItems> orderItemsList = oDto.getOrderItems().stream().map(itemDto -> {
    Products product = productsRepo.findById(itemDto.getProductId()).get();
    OrderItems orderItem = new OrderItems();
    orderItem.setQuantity(itemDto.getQuantity());
    orderItem.setItemsPrice(product.getPrice() * itemDto.getQuantity()); // setting the original costs of products

    // Calculate the price based on the product's price and discount
    Double finalPrice = discountCalculation(product.getPrice(), product.getDiscount());
    orderItem.setFinalAmount(finalPrice * itemDto.getQuantity()); // calculate the final price based on the items Quantity

    orderItem.setOrderId(order); // set the Order
    orderItem.setProductId(product); // set the product
    return orderItem; // Returning to the stream
}).collect(Collectors.toList());

    order.setOrderItems(orderItemsList); // Setting the collected list to the order

Double totalAmt = 0.0;
    for (OrderItems orderItem : orderItemsList) {
totalAmt += orderItem.getFinalAmount();
    }
            order.setTotalAmount(totalAmt); // after selecting all products and quantity then setting the Costs
Orders savedOrder = ordersRepo.save(order); // last, order saved in db

Payments payment = new Payments();
    payment.setAmount(totalAmt);
    payment.setPaymentDate(new Date());
        payment.setPaymentMethod(oDto.getPayment().getPaymentMethod()); // from JSON
        payment.setStatus(PaymentStatus.SUCCESSFUL);
    payment.setOrderId(savedOrder); // orderId Saved
    paymentRepo.save(payment);

    log.info("Order successfully placed with order ID: {}", savedOrder.getOrderId());
        return new ResponseEntity<>("Woohoo!!.....\nYour Order Placed Successfully. Order will be delivered in the next 4-5 working days", HttpStatus.CREATED);
        } else {
String errorMsg = "Customer not found with ID: " + oDto.getCustomerId();
    log.error(errorMsg);
    throw new CustomerNotFoundException(errorMsg);
}
*/
/*@Override
    public ResponseEntity<String> orderStatusShipped(int orderId)
    {
        Orders order = ordersRepo.findByOrderId(orderId);
        order.setStatus(OrderStatus.SHIPPED);
        ordersRepo.save(order);
        return new ResponseEntity<>("Order Status is Updated", HttpStatus.OK);
    }
    @Override
    public ResponseEntity<String> orderStatusDelivered(int orderId)
    {
        Orders order = ordersRepo.findByOrderId(orderId);
            order.setStatus(OrderStatus.DELIVERED);

            //after delivered we need to store address in deliveredAddress table

            ordersRepo.save(order);
            return new ResponseEntity<>("Order Status is Updated", HttpStatus.OK);
    }
    @Override
    public ResponseEntity<String> orderStatusCancelled(int orderId) {
        Orders order = ordersRepo.findByOrderId(orderId);
        order.setStatus(OrderStatus.CANCELLED);
        ordersRepo.save(order);
        return new ResponseEntity<>("Order Status is Updated", HttpStatus.OK);
    }
*/
