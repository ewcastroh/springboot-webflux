package com.ewch.springboot.webflux.controller;

import com.ewch.springboot.webflux.model.document.Product;
import com.ewch.springboot.webflux.service.ProductService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping({"/products", "/"})
	public Mono<String> getProducts(Model model) {
		Flux<Product> productFlux = productService.findAllWithUpperCaseName();
		productFlux.subscribe(product -> LOGGER.info(product.getName()));
		model.addAttribute("products", productFlux);
		model.addAttribute("title", "Products list");
		return Mono.just("productList");
	}

	@GetMapping("/form")
	public Mono<String> createProduct(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("title", "Product Form");
		return Mono.just("form");
	}

	@PostMapping("/form")
	public Mono<String> saveProduct(Product product) {
		return productService.save(product)
			.doOnNext(product1 -> {
				LOGGER.info("Saved product: " + product.toString());
			})
			.thenReturn("redirect:/productList");
			//.then(Mono.just("redirect:/productList"));
	}

	@GetMapping("/products-datadriver")
	public Mono<String> getProductsDataDriver(Model model) {
		Flux<Product> productFlux = productService.findAllWithUpperCaseName()
			.delayElements(Duration.ofSeconds(1));
		productFlux.subscribe(product -> LOGGER.info(product.getName()));
		model.addAttribute("products", new ReactiveDataDriverContextVariable(productFlux, 1));
		model.addAttribute("title", "Products list");
		return Mono.just("productList");
	}

	@GetMapping("/products-full")
	public Mono<String> getProductsFull(Model model) {
		Flux<Product> productFlux = productService.findAllWithUpperCaseNameRepeat();
		model.addAttribute("products", productFlux);
		model.addAttribute("title", "Products list");
		return Mono.just("productList");
	}

	@GetMapping("/products-chunked")
	public Mono<String> getProductsChunked(Model model) {
		Flux<Product> productFlux = productService.findAllWithUpperCaseNameRepeat();
		model.addAttribute("products", productFlux);
		model.addAttribute("title", "Products list");
		return Mono.just("productList-chunked");
	}

}
