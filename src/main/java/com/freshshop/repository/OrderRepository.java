package com.freshshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.freshshop.model.Orders;
import com.freshshop.model.ReportOrder;

import jakarta.transaction.Transactional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {
	@Query("select o from Orders o")
	Page<Orders> getOderStatus(String status, Pageable pageable);

	@Transactional
	@Modifying
	@Query("Update Orders o set o.status = ?1 where o.orderId = ?2")
	void updateByStatus(String status, int orderId);

	List<Orders> findAllByStatus(String status);
	
	Page<Orders> findAllByOrderByCreatedAtDesc(Pageable pageable);

	@Query("select o from Orders o where o.customer.customerId =?1 ORDER BY o.createdAt DESC")
	Page<Orders> findAllOrderIsClose(int customerID, Pageable pageable);

	@Query("SELECT new com.freshshop.model.ReportOrder(CONCAT(FUNCTION('YEAR', o.createdAt), '-', FUNCTION('MONTH', o.createdAt)), SUM(o.totalAmount)) "
			+ "FROM Orders o " + "GROUP BY CONCAT(FUNCTION('YEAR', o.createdAt), '-', FUNCTION('MONTH', o.createdAt))")
	List<ReportOrder> getOrderReport();

	@Query("select o from Orders o where o.fullName like ?1")
	Page<Orders> findByCustomerName(Pageable pageable, String fullName);

	@Query("SELECT o FROM Orders o WHERE o.fullName LIKE %:fullName% " + "AND o.status LIKE %:status%")
	Page<Orders> findByCustomerNameAndDateAndStatus(@Param("fullName") String fullName, @Param("status") String status,
			Pageable pageable);

//	@Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.createdAt >= :sevenDaysAgo")
//	Double findTotalSalesLastSevenDays(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
	
	//theo từng ngày trong 7 ngày gần nhất
	@Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE o.createdAt >= :sevenDaysAgo")
	Double findTotalSalesLastSevenDays(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
	@Query("SELECT FUNCTION('DATE', o.createdAt) as date, SUM(o.totalAmount) as total " +
		       "FROM Orders o " +
		       "WHERE o.createdAt >= :sevenDaysAgo " +
		       "GROUP BY FUNCTION('DATE', o.createdAt)")
		List<java.util.Map<String, Object>> findDailySalesLastSevenDays(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

	//tổng năm
	@Query("SELECT SUM(o.totalAmount) FROM Orders o WHERE FUNCTION('YEAR', o.createdAt) = :year")
	Double findTotalSalesForYear(@Param("year") int year);
	
	//từng năm
	@Query("SELECT FUNCTION('YEAR', o.createdAt) as year, SUM(o.totalAmount) as total " +
		       "FROM Orders o " +
		       "GROUP BY FUNCTION('YEAR', o.createdAt)")
		List<Map<String, Object>> findYearlySales();
}
