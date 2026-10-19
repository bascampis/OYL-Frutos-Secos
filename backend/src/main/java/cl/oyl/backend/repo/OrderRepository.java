package cl.oyl.backend.repo;
import cl.oyl.backend.model.OrderHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OrderRepository extends JpaRepository<OrderHeader,String> {
 List<OrderHeader> findByClientAliasOrderByCreatedAtDesc(String clientAlias);
}