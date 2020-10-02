package com.ewch.springboot.webflux;

import com.ewch.springboot.webflux.dao.ProductDao;
import com.ewch.springboot.webflux.model.document.Product;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringbootWebfluxApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringbootWebfluxApplication.class);

	private final ProductDao productDao;

	private final ReactiveMongoTemplate reactiveMongoTemplate;

	public SpringbootWebfluxApplication(ProductDao productDao, ReactiveMongoTemplate reactiveMongoTemplate) {
		this.productDao = productDao;
		this.reactiveMongoTemplate = reactiveMongoTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringbootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		reactiveMongoTemplate.dropCollection("products").subscribe();

		Flux.just(
			new Product("TV Panasonic LCD", 456.89),
			new Product("Sony Camara HD Digital", 177.89),
			new Product("Apple iPod", 46.89),
			new Product("Sony Notebook", 846.89),
			new Product("Hewlett Packard Multifuncional", 200.89),
			new Product("Bianchi Bicycle", 70.89),
			new Product("HP Notebook Omen 17", 2500.89),
			new Product("Mica 5 Drawers", 150.89),
			new Product("TV Sony Bravia OLED 4K Ultra HD", 2255.89)
		)
			.flatMap(product -> {
				product.setCreatedAt(new Date());
				return productDao.save(product);
			})
			.subscribe(
				productMono -> LOGGER.info("Insert :: ".concat(productMono.getId()).concat(": ").concat(productMono.getName()))
			);
	}
}
