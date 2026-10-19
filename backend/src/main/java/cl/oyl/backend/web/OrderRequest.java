package cl.oyl.backend.web;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record OrderRequest(
 @NotBlank String clientAlias,
 @NotBlank String fulfillment,
 @NotEmpty List<@Valid Item> items) {
 public record Item(@NotBlank String productId,@Min(1) int quantity) {}
}