package cl.oyl.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/** Cliente HTTP hacia catalog-service (comunicación entre microservicios). */
@Component
public class CatalogClient {

    public record CatalogProduct(String id, String name, int price, int stock, boolean active) {}

    private final RestClient http;

    public CatalogClient(@Value("${catalog.url}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public CatalogProduct getProduct(String id) {
        try {
            return http.get().uri("/api/products/{id}", id).retrieve()
                    .onStatus(s -> s.value() == 404, (rq, rs) -> {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe: " + id);
                    })
                    .body(CatalogProduct.class);
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    /** delta negativo descuenta stock; positivo lo repone. */
    public void adjustStock(String id, int delta) {
        try {
            http.patch().uri("/api/products/{id}/stock", id).body(Map.of("delta", delta)).retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (rq, rs) -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para " + id);
                    })
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private ResponseStatusException unavailable(Exception e) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "catalog-service no disponible");
    }
}
