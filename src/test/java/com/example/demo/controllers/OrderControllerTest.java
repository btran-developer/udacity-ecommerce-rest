package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

    private OrderController orderController;

    private final UserRepository userRepository = mock(UserRepository.class);

    private final OrderRepository orderRepository = mock(OrderRepository.class);

    @Before
    public void init() {
        orderController = new OrderController();
        TestUtils.injectObjects(orderController, "userRepository", userRepository);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepository);

        User user = getTestUserWithCartItems();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(userRepository.findByUsername("testInvalidUsername")).thenReturn(null);
        when(orderRepository.findByUser(user)).thenReturn(getTestOrders());
    }

    @Test
    public void submit_order_happy_path() {
        User user = getTestUserWithCartItems();

        ResponseEntity<UserOrder> responseEntity = orderController.submit(user.getUsername());

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        UserOrder responseBody = responseEntity.getBody();

        assertEquals(user.getCart().getTotal(), responseBody.getTotal());
        assertEquals(user.getCart().getItems().size(), responseBody.getItems().size());
        assertEquals(user.getUsername(), responseBody.getUser().getUsername());
    }

    @Test
    public void get_orders_for_user_happy_path() {
        User user = getTestUserWithCartItems();
        List<UserOrder> orders = getTestOrders();

        ResponseEntity<List<UserOrder>> responseEntity = orderController.getOrdersForUser(user.getUsername());

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        List<UserOrder> responseBody = responseEntity.getBody();

        assertNotNull(responseBody);
        assertEquals(orders.size(), responseBody.size());

        assertEquals(orders.get(0).getItems().size(), responseBody.get(0).getItems().size());
        assertEquals(orders.get(0).getTotal(), responseBody.get(0).getTotal());
        assertEquals(orders.get(0).getUser().getUsername(), responseBody.get(0).getUser().getUsername());
        assertEquals(orders.get(0).getItems().get(0).getId(), responseBody.get(0).getItems().get(0).getId());

        assertEquals(orders.get(1).getItems().size(), responseBody.get(1).getItems().size());
        assertEquals(orders.get(1).getTotal(), responseBody.get(1).getTotal());
        assertEquals(orders.get(1).getUser().getUsername(), responseBody.get(1).getUser().getUsername());
        assertEquals(orders.get(1).getItems().get(0).getId(), responseBody.get(1).getItems().get(0).getId());
    }

    @Test
    public void get_orders_for_user_with_invalid_username() {
        ResponseEntity<UserOrder> responseEntity = orderController.submit("testInvalidUsername");

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    @Test
    public void submit_order_with_invalid_username() {
        ResponseEntity<List<UserOrder>> responseEntity = orderController.getOrdersForUser("testInvalidUsername");

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    private User getTestUserWithCartItems() {
        User user = new User();
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.addItem(getTestItem());
        user.setId(1L);
        user.setUsername("test");
        user.setCart(cart);

        return user;
    }

    private Item getTestItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Round Widget");
        item.setPrice(BigDecimal.valueOf(2.99));
        item.setDescription("A widget that is round");

        return item;
    }

    private List<UserOrder> getTestOrders() {
        User user = getTestUserWithCartItems();
        UserOrder order1 = UserOrder.createFromCart(user.getCart());
        user.getCart().addItem(getTestItem());
        user.getCart().addItem(getTestItem());
        UserOrder order2 = UserOrder.createFromCart(user.getCart());

        List<UserOrder> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);

        return orders;
    }
}
