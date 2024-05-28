package com.freshshop.controller.UserController;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.freshshop.constant.FreshShopConstants;
import com.freshshop.implementation.CartImp;
import com.freshshop.model.Customer;
import com.freshshop.model.OrderDetails;
import com.freshshop.model.Orders;
import com.freshshop.model.Products;
import com.freshshop.repository.OrderDetailRepository;
import com.freshshop.repository.OrderRepository;
import com.freshshop.repository.ProductRepository;
import com.freshshop.service.OrderService;
import com.freshshop.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class CheckOutController {

	@Autowired
	private CartImp cartImp;
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private OrderDetailRepository orderDetailRepository;
	@Autowired
	private ProductRepository productRepository;
	
	

	private final MessageSource messageSource;
	Locale currentLocale = LocaleContextHolder.getLocale();

	public CheckOutController(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@GetMapping("/checkout")
	public String displayCheckOut(Model model, HttpSession session) {
		Customer customer = (Customer) session.getAttribute("loggingCustomer");
		if (customer != null) {
			model.addAttribute("customer", customer);
			List<String> addressOptions = null;
			if (customer.getAddress() != null && customer.getAddress().getAddressId() >= 0) {
				addressOptions = Arrays.asList(customer.getAddress().getAddress1(),
						customer.getAddress().getAddress2());
			}
			model.addAttribute("addressOptions", addressOptions);

			Collection<Products> productList = cartImp.getProducts();
			if (productList.isEmpty()) {
				String localizedMessage = messageSource.getMessage("cart.noItems", null, currentLocale);
				model.addAttribute("message", localizedMessage);
				return "User/cart.html";
			}
			model.addAttribute("cartProducts", cartImp.getProducts());
			model.addAttribute("count", cartImp.getCount());
			model.addAttribute("subtotalAmount", cartImp.getAmount());
			model.addAttribute("discount", cartImp.getAmount() * 0.1);
			double total = cartImp.getAmount() - cartImp.getAmount() * 0.1;
			model.addAttribute("total", total);
		}
		return "User/checkout.html";
		
		
			
		
	}
	
	@GetMapping("/checkoutVNPAY")
	public String displayCheckOut1(Model model1, HttpSession session) {
		Customer customer = (Customer) session.getAttribute("loggingCustomer");
		if (customer != null) {
			model1.addAttribute("customer", customer);
			List<String> addressOptions = null;
			if (customer.getAddress() != null && customer.getAddress().getAddressId() >= 0) {
				addressOptions = Arrays.asList(customer.getAddress().getAddress1(),
						customer.getAddress().getAddress2());
			}
			model1.addAttribute("addressOptions", addressOptions);

			Collection<Products> productList = cartImp.getProducts();
			if (productList.isEmpty()) {
				String localizedMessage = messageSource.getMessage("cart.noItems", null, currentLocale);
				model1.addAttribute("message", localizedMessage);
				return "User/cart.html";
			}
			model1.addAttribute("cartProducts", cartImp.getProducts());
			model1.addAttribute("count", cartImp.getCount());
			model1.addAttribute("subtotalAmount", cartImp.getAmount());
			model1.addAttribute("discount", cartImp.getAmount() * 0.1);
			double total = cartImp.getAmount() - cartImp.getAmount() * 0.1;
			model1.addAttribute("total", total);
			
			
			
		}
		return "User/VNPAY.html";
	}
	
	private boolean isValidPhoneNumber(String phoneNumber) {
	    // Kiểm tra số điện thoại có phải là số và có đúng 10 ký tự không
	    String phoneNumberRegex = "^[0-9]{10}$";
	    Pattern pattern = Pattern.compile(phoneNumberRegex);
	    Matcher matcher = pattern.matcher(phoneNumber);
	    return matcher.matches();
	}
	
	
	@GetMapping("/checkthuong")
	public String checkthuong(@RequestParam("selectedAddress") String selectedAddress,
			@RequestParam("phoneNumber") String phoneNumber, Model model, HttpSession session, RedirectAttributes ra) {
		Customer customer = (Customer) session.getAttribute("loggingCustomer");
		Collection<Products> listProduct = cartImp.getProducts();

		if (customer == null) {
			return "User/login.html";
		}
		if (phoneNumber.trim().isEmpty()) {
			ra.addFlashAttribute("message", "Phone number is required !");
			return "redirect:/checkout";
		} else {
			if (selectedAddress.trim().isEmpty()) {
				ra.addFlashAttribute("message", "Address is required !");
				return "redirect:/checkout";
			}
		}

		Orders order = new Orders();
		order.setCustomer(customer);
		order.setFullName(customer.getCustomerName());
		order.setPhoneNumber(phoneNumber);
		order.setAddress(selectedAddress);
		order.setTotalAmount(cartImp.getAmount() - cartImp.getAmount() * 0.1);
		order.setStatus(FreshShopConstants.WAITING);
		order.setStatus1(FreshShopConstants.CHECKTHUONG);
		orderService.saveOrder(order);
		if (order.getOrderId() > 0) {
			for (Products product : listProduct) {
				OrderDetails orderDetails = new OrderDetails();
				orderDetails.setOrder(order);
				orderDetails.setProduct(product);
				orderDetails.setQuantity(product.getQuantity());
				orderDetails.setAmount(product.getQuantity() * product.getPrice());
				orderDetailRepository.save(orderDetails);
			}
			cartImp.clear();
			session.setAttribute("itemInCart", cartImp.getSize());
//				ra.addFlashAttribute("message", "Wait for admin to accept");
			return "redirect:/order/completed";
		}
		ra.addFlashAttribute("message", "Checkout Failed");
		return "redirect:/checkout";
	}

	
	@GetMapping("/placeOrder")
	public String placeOrder(@RequestParam("selectedAddress") String selectedAddress,
			@RequestParam("phoneNumber") String phoneNumber, Model model, HttpSession session,
			HttpServletRequest request,
			RedirectAttributes ra) {
		
		 String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		 
		Customer customer = (Customer) session.getAttribute("loggingCustomer");
		Collection<Products> listProduct = cartImp.getProducts();

		if (customer == null) {
			return "User/login.html";
		}
		if (!isValidPhoneNumber(phoneNumber)) {
			ra.addFlashAttribute("message", "Phone number is not valid! !");
			return "redirect:/checkout";
		} else {
			if (selectedAddress.trim().isEmpty()) {
				ra.addFlashAttribute("message", "Address is required !");
				return "redirect:/checkout";
			}
		}

		Orders order = new Orders();
		order.setCustomer(customer);
		order.setFullName(customer.getCustomerName());
		order.setPhoneNumber(phoneNumber);
		order.setAddress(selectedAddress);
		order.setTotalAmount(cartImp.getAmount() - cartImp.getAmount() * 0.1);
		order.setStatus(FreshShopConstants.WAITING);
		order.setStatus1(FreshShopConstants.CHECKTHUONG);
		int total = Double.valueOf(order.getTotalAmount()).intValue();
		String vnpayUrl = VNPayService.createOrder( total,"Thanh toan id" + order.getOrderId() , baseUrl); 
		orderService.saveOrder(order);
		if (order.getOrderId() > 0) {
			for (Products product : listProduct) {
				OrderDetails orderDetails = new OrderDetails();
				orderDetails.setOrder(order);
				orderDetails.setProduct(product);
				orderDetails.setQuantity(product.getQuantity());
				orderDetails.setAmount(product.getQuantity() * product.getPrice());
				orderDetailRepository.save(orderDetails);
			}
			cartImp.clear();
			session.setAttribute("itemInCart", cartImp.getSize());
//				ra.addFlashAttribute("message", "Wait for admin to accept");
			return "redirect:" + vnpayUrl;
		}
		ra.addFlashAttribute("message", "Checkout Failed");
		return "redirect:/checkout";
	}
	
	
	
	@GetMapping("/VNPAY1")
	public String VNPAY1(@RequestParam("selectedAddress") String selectedAddress,
	        @RequestParam("phoneNumber") String phoneNumber, Model model, HttpSession session,
	        HttpServletRequest request,
	        RedirectAttributes ra) {

	    String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
	    Customer customer = (Customer) session.getAttribute("loggingCustomer");
	    Collection<Products> listProduct = cartImp.getProducts();

	    if (customer == null) {
	        return "User/login.html";
	    }
	    if (!isValidPhoneNumber(phoneNumber)) {
	        ra.addFlashAttribute("message", "Phone number is not valid! !");
	        return "redirect:/checkout";
	    }
	    if (selectedAddress.trim().isEmpty()) {
	        ra.addFlashAttribute("message", "Address is required !");
	        return "redirect:/checkout";
	    }

	    int total = Double.valueOf(cartImp.getAmount() - cartImp.getAmount() * 0.1).intValue();
	    String vnpayUrl = VNPayService.createOrder(total, "Thanh toan don hang", baseUrl);
	    if (vnpayUrl == null || vnpayUrl.isEmpty()) {
	        ra.addFlashAttribute("message", "There was an error processing the payment.");
	        return "redirect:/checkout";
	    }

	    // Chỉ lưu đơn hàng nếu việc tạo URL thành công và chưa thanh toán
	    Orders order = new Orders();
	    order.setCustomer(customer);
	    order.setFullName(customer.getCustomerName());
	    order.setPhoneNumber(phoneNumber);
	    order.setAddress(selectedAddress);
	    order.setTotalAmount(total);
	    order.setStatus(FreshShopConstants.WAITING); 
	    order.setStatus1(FreshShopConstants.CHECKVN);
	    orderService.saveOrder(order);
	    
	    if (order.getOrderId() > 0) {
	        for (Products product : listProduct) {
	            OrderDetails orderDetails = new OrderDetails();
	            orderDetails.setOrder(order);
	            orderDetails.setProduct(product);
	            orderDetails.setQuantity(product.getQuantity());
	            orderDetails.setAmount(product.getQuantity() * product.getPrice());
	            orderDetailRepository.save(orderDetails);
	        }
	        return "redirect:" + vnpayUrl;

	    } else {
	        ra.addFlashAttribute("message", "Could not create order, please try again.");
	        return "redirect:/checkout";
	    }
	}

	
	
	
	@GetMapping("/cancelOrder/{orderId}")
	public String cancelOrder(Model model, HttpSession session, @PathVariable("orderId") int orderId) {

		Optional<Orders> option = orderRepository.findById(orderId);
		Orders order = option.get();
		order.setStatus(FreshShopConstants.CANCELLED);
		orderService.saveOrder(order);
		return "redirect:/order/completed";

	}

	@GetMapping("/order/completed")
	public String orderCompleted(Model model, HttpSession session,
			@RequestParam("currentPage") Optional<Integer> currentPage, Optional<String> error,
			Optional<String> success) {
		Customer customer = (Customer) session.getAttribute("loggingCustomer");
		Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
		Pageable pageable = PageRequest.of(currentPage.orElse(0), 7, sort);
		Page<Orders> page = orderRepository.findAllOrderIsClose(customer.getCustomerId(), pageable);

		List<Orders> ordersList = page.getContent();

		model.addAttribute("ordersList", ordersList);
		model.addAttribute("page", page);
		model.addAttribute("currentPage", currentPage.orElse(0));
		return "User/order-completed.html";
	}

	@GetMapping("/orderDetail/completed/{orderId}")
	public String orderDetailCompleted(Model model, HttpSession session, @PathVariable("orderId") int orderId) {
		Customer customer = (Customer) session.getAttribute("loggingCustomer");
		List<OrderDetails> listOrderDetailDetail = orderDetailRepository
				.findOrderDetailsByCustomerId(customer.getCustomerId(), orderId);
		model.addAttribute("listOrderDetailCompleted", listOrderDetailDetail);

		Optional<Orders> option = orderRepository.findById(orderId);
		Orders order = option.get();
		model.addAttribute("order", order);

		return "User/order-detail-completed.html";
	}
	 @GetMapping("/vnpay-payment")
	    public String GetMapping(HttpServletRequest request, Model model){
	        int paymentStatus =VNPayService.orderReturn(request);

	        String orderInfo = request.getParameter("vnp_OrderInfo");
	        String paymentTime = request.getParameter("vnp_PayDate");
	        String transactionId = request.getParameter("vnp_TransactionNo");
	        String totalPrice = request.getParameter("vnp_Amount");

	        model.addAttribute("orderId", orderInfo);
	        model.addAttribute("totalPrice", totalPrice);
	        model.addAttribute("paymentTime", paymentTime);
	        model.addAttribute("transactionId", transactionId);

	        return paymentStatus == 1 ? "ordersuccess" : "orderfail";
	    }


}
