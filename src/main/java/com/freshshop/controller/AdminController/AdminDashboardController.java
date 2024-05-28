package com.freshshop.controller.AdminController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.freshshop.model.Report;
import com.freshshop.model.ReportOrder;
import com.freshshop.repository.CategoryRepository;
import com.freshshop.repository.ContactUsRepository;
import com.freshshop.repository.CustomerRepository;
import com.freshshop.repository.OrderRepository;
import com.freshshop.repository.ProductRepository;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
	@Autowired
	ContactUsRepository contactUsRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@GetMapping("/dashboard")
	public String displayDashboard(Model model) {
		LocalDateTime sevenDaysAgo1 = LocalDateTime.now().minusDays(7);
	    Double totalSalesLastSevenDays = orderRepository.findTotalSalesLastSevenDays(sevenDaysAgo1);
	    model.addAttribute("totalSalesLastSevenDays", totalSalesLastSevenDays);
		
		
		List<java.util.Map<String, Object>> yearlySales = orderRepository.findYearlySales();
	    model.addAttribute("yearlySales", yearlySales);

	    
		//tính tổng năm
		LocalDateTime now = LocalDateTime.now();
		double totalSalesThisYear = orderRepository.findTotalSalesForYear(now.getYear());
		model.addAttribute("totalSalesThisYear", totalSalesThisYear);
	    //tính 7 ngày
	    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
	    List<java.util.Map<String, Object>> dailySalesLastSevenDays 
	        = orderRepository.findDailySalesLastSevenDays(sevenDaysAgo);
	    model.addAttribute("dailySalesLastSevenDays", dailySalesLastSevenDays);
	    
		List<String> dateContactList = contactUsRepository.getContactUsReports().stream().map(Report::getDate)
				.collect(Collectors.toList());
		List<Long> countContactList = contactUsRepository.getContactUsReports().stream().map(Report::getCount)
				.collect(Collectors.toList());
		List<String> dateProductList = productRepository.getProductReports().stream().map(Report::getDate)
				.collect(Collectors.toList());
		List<Long> countProductList = productRepository.getProductReports().stream().map(Report::getCount)
				.collect(Collectors.toList());

		long totalCount = customerRepository.getCustomerReports().stream().mapToLong(Report::getCount).sum();
		List<ReportOrder> listOrderReport = orderRepository.getOrderReport();
		if (listOrderReport != null) {
			model.addAttribute("listOrderReport", listOrderReport);
			double totalAmount = listOrderReport.stream().mapToDouble(ReportOrder::getAmount).sum();
			model.addAttribute("totalAmount", totalAmount);
		}

		int categoryNumber = categoryRepository.findAll().size();
		model.addAttribute("dateContactList", dateContactList);
		model.addAttribute("countContactList", countContactList);
		model.addAttribute("dateProductList", dateProductList);
		model.addAttribute("countProductList", countProductList);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("categoryNumber", categoryNumber);

		
		
		
		
		return "Admin/admin-dashboard.html";
	}

}
