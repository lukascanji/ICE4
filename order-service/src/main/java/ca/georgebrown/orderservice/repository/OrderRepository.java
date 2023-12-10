package ca.georgebrown.orderservice.repository;

import ca.georgebrown.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderRepository extends JpaRepository<Order, Long>{

}
