package com.ewch.springboot.webflux.controller;

import com.ewch.springboot.webflux.dao.ProductDao;
import com.ewch.springboot.webflux.model.document.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	private final ProductDao productDao;

	public ProductRestController(ProductDao productDao) {
		this.productDao = productDao;
	}

	@GetMapping
	public Flux<Product> getProducts() {
		Flux<Product> productFlux = productDao.findAll()
			.map(product -> {
				product.setName(product.getName().toUpperCase());
				return product;
			})
			.doOnNext(product -> LOGGER.info(product.getName()));
		return productFlux;
	}

	@GetMapping("/{id}")
	public Mono<Product> getProductById(@PathVariable String id) {
		// Mono<Product> productMono = productDao.findById(id);
		Flux<Product> productFlux = productDao.findAll();
		Mono<Product> productMono = productFlux.filter(product -> product.getId().equals(id))
			.next()
			.doOnNext(product -> LOGGER.info(product.getName()));
		return productMono;
	}

}
