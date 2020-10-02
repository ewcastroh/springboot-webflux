package com.ewch.springboot.webflux.controller;

import com.ewch.springboot.webflux.dao.ProductDao;
import com.ewch.springboot.webflux.model.document.Product;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

@Controller
public class ProductController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	private final ProductDao productDao;

	public ProductController(ProductDao productDao) {
		this.productDao = productDao;
	}

	@GetMapping({"/products", "/"})
	public String getProducts(Model model) {
		Flux<Product> productFlux = productDao.findAll()
			.map(product -> {
				product.setName(product.getName().toUpperCase());
				return product;
			});
		productFlux.subscribe(product -> LOGGER.info(product.getName()));

		model.addAttribute("products", productFlux);
		model.addAttribute("title", "products list");
		return "productList";
	}

	@GetMapping("/products-datadriver")
	public String getProductsDataDriver(Model model) {
		Flux<Product> productFlux = productDao.findAll()
			.map(product -> {
				product.setName(product.getName().toUpperCase());
				return product;
			})
			.delayElements(Duration.ofSeconds(1));
		productFlux.subscribe(product -> LOGGER.info(product.getName()));

		model.addAttribute("products", new ReactiveDataDriverContextVariable(productFlux, 1));
		model.addAttribute("title", "products list");
		return "productList";
	}
}
