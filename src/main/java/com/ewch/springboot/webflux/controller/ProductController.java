package com.ewch.springboot.webflux.controller;

import com.ewch.springboot.webflux.dao.ProductDao;
import com.ewch.springboot.webflux.model.document.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

@Controller
public class ProductController {

	private final ProductDao productDao;

	public ProductController(ProductDao productDao) {
		this.productDao = productDao;
	}

	@GetMapping({"/products", "/"})
	public String getProducts(Model model) {
		Flux<Product> productFlux = productDao.findAll();
		model.addAttribute("products", productFlux);
		model.addAttribute("title", "products list");
		return "productList";
	}
}
