package com.ewch.springboot.webflux.controller;

import com.ewch.springboot.webflux.model.document.Category;
import com.ewch.springboot.webflux.model.document.Product;
import com.ewch.springboot.webflux.service.ProductService;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("product")
@Controller
public class ProductController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	@Value("${config.uploads.path}")
	private String pathUpload;

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
		model.addAttribute("button", "Create");
		return Mono.just("form");
	}

	@GetMapping("/form-v2/{id}")
	// public Mono<String> editProduct(@PathVariable(name = "id") String productId) {
	public Mono<String> editProductV2(@PathVariable String id, Model model) {
		return productService.findById(id)
			.doOnNext(product -> {
				product.toString();
				model.addAttribute("title", "Edit Product");
				model.addAttribute("product", product);
				model.addAttribute("button", "Save");
			})
			.defaultIfEmpty(new Product())
			.flatMap(product -> {
				if (product.getId() == null) {
					return Mono.error(new InterruptedException("Product doesn't exist!"));
				}
				return Mono.just(product);
			})
			.then(Mono.just("form"))
			.onErrorResume(throwable -> Mono.just("redirect:/products?error=product+doesnt+exist"));
	}

	@GetMapping("/form/{id}")
	// public Mono<String> editProduct(@PathVariable(name = "id") String productId) {
	public Mono<String> editProduct(@PathVariable String id, Model model) {
		Mono<Product> productMono = productService.findById(id)
			.doOnNext(product -> product.toString())
			.defaultIfEmpty(new Product());
		model.addAttribute("title", "Edit Product");
		model.addAttribute("product", productMono);
		model.addAttribute("button", "Save");
		return Mono.just("form");
	}

	@PostMapping("/form")
	public Mono<String> saveProduct(@Valid @ModelAttribute("product") Product product, BindingResult bindingResult, Model model,
									@RequestPart(name = "file") FilePart file, SessionStatus sessionStatus) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("title", "Errors in Product form");
			model.addAttribute("button", "Save");
			return Mono.just("form");
		} else {
			sessionStatus.setComplete();
			Mono<Category> categoryMono = productService.findCategoryById(product.getCategory().getId());
			return categoryMono.flatMap(category -> {
				if (product.getCreatedAt() == null) {
					product.setCreatedAt(new Date());
				}
				if (!file.filename().isEmpty()) {
					product.setPicture(UUID.randomUUID().toString().concat("-")
						.concat(file.filename()
							.replace(" ", "")
							.replace(":", "")
							.replace("\\", "")
						));
				}
				product.setCategory(category);
				return productService.save(product);
			})
			.doOnNext(product1 -> {
				LOGGER.info("Saved product: " + product.toString());
			})
			.flatMap(product1 -> {
				if (!file.filename().isEmpty()) {
					return file.transferTo(new File(pathUpload.concat(product1.getPicture())));
				}
				return Mono.empty();
			})
			.thenReturn("redirect:/products?success=product+saved+successfully");
			//.then(Mono.just("redirect:/productList"));
		}
	}

	@GetMapping("/delete/{id}")
	public Mono<String> deleteProduct(@PathVariable String id) {
		return productService.findById(id)
			.defaultIfEmpty(new Product())
			.flatMap(product -> {
				if (product.getId() == null) {
					return Mono.error(new InterruptedException("Product doesn't exist!"));
				}
				return Mono.just(product);
			})
			.flatMap(product -> {
				LOGGER.info("Product deleted: " + product.toString());
				return productService.delete(product);
			})
			.then(Mono.just("redirect:/products?success=product+deleted+successfully"))
			.onErrorResume(throwable -> Mono.just("redirect:/products?error=product+doesnt+exist+to+be+deleted"));
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

	@ModelAttribute("categories")
	public Flux<Category> getCategories() {
		return productService.findAllCategories();
	}

	@GetMapping("/productDetail/{id}")
	public Mono<String> showImage(Model model, @PathVariable("id") String id) {
		return productService.findById(id)
			.doOnNext(product -> {
				model.addAttribute("product", product);
				model.addAttribute("title", "Product Detaol");
			})
			.switchIfEmpty(Mono.just(new Product()))
			.flatMap(product -> {
				if (product.getId() == null) {
					return Mono.error(new InterruptedException("Product doesn't exist!"));
				}
				return Mono.just(product);
			})
			.then(Mono.just("productDetail"))
			.onErrorResume(throwable -> Mono.just("redirect:/products?error=product+doesnt+exist"));
	}

	@GetMapping("/uploads/pictures/{pictureName:.+}")
	public Mono<ResponseEntity<Resource>> showPicture(@PathVariable("pictureName") String pictureName) throws MalformedURLException {
		Path path = Paths.get(pathUpload).resolve(pictureName).toAbsolutePath();
		Resource resource = new UrlResource(path.toUri());
		return Mono.just(ResponseEntity.ok()
							.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename\"" + resource.getFilename() + "\"")
							.body(resource));
	}

}
