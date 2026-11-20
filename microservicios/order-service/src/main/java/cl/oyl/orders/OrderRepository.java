package cl.oyl.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderHeader, String> {
    List<OrderHeader> findByClientAliasOrderByCreatedAtDesc(String clientAlias);
}
