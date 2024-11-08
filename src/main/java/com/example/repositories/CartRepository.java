package com.example.repositories;

import com.example.models.dbModels.Cart;
import com.example.models.dbModels.Customers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Integer>
{
    Optional<Cart> findByCustCart(Customers customer);
}
