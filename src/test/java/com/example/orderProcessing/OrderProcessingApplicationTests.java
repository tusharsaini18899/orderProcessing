package com.example.orderProcessing;

import com.example.orderProcessing.entity.Orders;
import com.example.orderProcessing.repository.OrderRepository;
import com.example.orderProcessing.service.OrderService;
import com.example.orderProcessing.utils.Common;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.example.orderProcessing.utils.Common.CRON_JOB_TIME;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderProcessingApplicationTests extends MockData{

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderService orderService;

	@Test
	public void testPlaceOrder() throws Exception {
		String orderJson = "{\"userId\": 1, \"itemIds\": \"item1,item2\", \"totalAmount\": 100.50}";
		mockMvc.perform(MockMvcRequestBuilders.post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(Common.PENDING));
	}

	@Test
	public void testGetOrderStatus() throws Exception {
		Orders order = MOCK_ORDER;
		order = orderRepository.save(order);
		mockMvc.perform(MockMvcRequestBuilders.get("/orders/" + order.getOrderId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(Common.PENDING));
	}

	@Test
	public void testOrderProcessingQueue() {
		Orders order = MOCK_ORDER;
		order = orderRepository.save(order);

		orderService.enqueueOrder(order);

		orderService.processOrders();

		Orders finalOrder = order;
		Awaitility.await().atMost(CRON_JOB_TIME+1, TimeUnit.SECONDS).until(() -> {
			Optional<Orders> processedOrder = orderRepository.findById(finalOrder.getOrderId());
			return processedOrder.isPresent() && Common.COMPLETED.equals(processedOrder.get().getStatus());
		});

		Optional<Orders> processedOrder = orderRepository.findById(order.getOrderId());
		assertThat(processedOrder).isPresent();
		assertThat(processedOrder.get().getStatus()).isEqualTo(Common.COMPLETED);
	}


	@Test
	public void testGetMetrics() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/orders/metrics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalProcessed").exists())
				.andExpect(jsonPath("$.pending").exists())
				.andExpect(jsonPath("$.processing").exists());
	}
}
