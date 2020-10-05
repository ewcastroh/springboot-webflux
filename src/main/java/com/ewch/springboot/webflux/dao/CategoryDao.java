package com.ewch.springboot.webflux.dao;

import com.ewch.springboot.webflux.model.document.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryDao extends ReactiveMongoRepository<Category, String> {

}
