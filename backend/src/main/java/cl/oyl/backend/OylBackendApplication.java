package cl.oyl.backend;

import cl.oyl.backend.model.Product;
import cl.oyl.backend.repo.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OylBackendApplication {
 public static void main(String[] args) { SpringApplication.run(OylBackendApplication.class, args); }
 @Bean CommandLineRunner seed(ProductRepository products) {
  return args -> {
   if (products.count() != 0) return;
   products.save(new Product("OY001","Almendras naturales","Frutos secos","Almendras crujientes","250 g",2990,25,true));
   products.save(new Product("OY002","Nueces","Frutos secos","Nueces en mitades","250 g",3490,18,true));
   products.save(new Product("OY003","Mix premium","Frutos secos","Mezcla premium","250 g",3990,40,true));
   products.save(new Product("OY004","Pasas morenas","Deshidratados","Pasas","250 g",2490,36,true));
   products.save(new Product("OY005","Semillas de chía","Semillas","Semillas naturales","250 g",2290,12,true));
   products.save(new Product("OY006","Cranberries","Deshidratados","Arándanos deshidratados","250 g",2890,16,true));
  };
 }
}