package com.ewch.springboot.webflux.service.impl;

import com.ewch.springboot.webflux.controller.ProductController;
import com.ewch.springboot.webflux.dao.ProductDao;
import com.ewch.springboot.webflux.model.document.Product;
import com.ewch.springboot.webflux.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	private final ProductDao productDao;

	public ProductServiceImpl(ProductDao productDao) {
		this.productDao = productDao;
	}

	@Override
	public Flux<Product> findAll() {
		return productDao.findAll();
	}

	@Override
	public Flux<Product> findAllWithUpperCaseName() {
		return productDao.findAll()
			.map(product -> {
				product.setName(product.getName().toUpperCase());
				return product;
		});
	}

	@Override
	public Flux<Product> findAllWithUpperCaseNameRepeat() {
		return findAllWithUpperCaseName().repeat(5000);
	}

	@Override
	public Mono<Product> findById(String id) {
		return productDao.findById(id);
	}

	@Override
	public Mono<Product> save(Product product) {
		return productDao.save(product);
	}

	@Override
	public Mono<Void> delete(Product product) {
		return productDao.delete(product);
	}
}
