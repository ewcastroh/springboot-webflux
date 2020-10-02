package com.ewch.springboot.webflux.service;

import com.ewch.springboot.webflux.model.document.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

	Flux<Product> findAll();

	Flux<Product> findAllWithUpperCaseName();

	Flux<Product> findAllWithUpperCaseNameRepeat();

	Mono<Product> findById(String id);

	Mono<Product> save(Product product);

	Mono<Void> delete(Product product);
}
