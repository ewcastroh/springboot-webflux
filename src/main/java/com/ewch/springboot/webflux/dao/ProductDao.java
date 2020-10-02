package com.ewch.springboot.webflux.dao;

import com.ewch.springboot.webflux.model.document.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDao extends ReactiveMongoRepository<Product, String> {

}
