package cl.oyl.backend.repo;
import cl.oyl.backend.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ProductRepository extends JpaRepository<Product,String> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select p from Product p where p.id=:id")
 Optional<Product> lockById(@Param("id") String id);
}