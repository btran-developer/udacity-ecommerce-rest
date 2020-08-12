package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CartControllerTest {

    private CartController cartController;

    private final UserRepository userRepository = mock(UserRepository.class);

    private final CartRepository cartRepository = mock(CartRepository.class);

    private final ItemRepository itemRepository = mock(ItemRepository.class);

    @Before
    public void init() {
        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepository);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepository);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepository);

        when(userRepository.findByUsername("test")).thenReturn(getTestUser());
        when(userRepository.findByUsername("test2")).thenReturn(getTestUserWithCartItems());
        when(userRepository.findByUsername("testUserNotFound")).thenReturn(null);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(getTestItem()));
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());
    }

    @Test
    public void add_to_cart_happy_path() {
        Item item = getTestItem();
        User user = getTestUser();

        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setItemId(item.getId());
        cartRequest.setQuantity(2);
        cartRequest.setUsername(user.getUsername());

        ResponseEntity<Cart> responseEntity = cartController.addToCart(cartRequest);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        Cart responseBody = responseEntity.getBody();
        assertEquals(item.getId(), responseBody.getItems().get(0).getId());
        assertEquals(item.getName(), responseBody.getItems().get(0).getName());
        assertEquals(item.getDescription(), responseBody.getItems().get(0).getDescription());
        assertEquals(item.getId(), responseBody.getItems().get(1).getId());
        assertEquals(item.getName(), responseBody.getItems().get(1).getName());
        assertEquals(item.getDescription(), responseBody.getItems().get(1).getDescription());
        assertEquals(item.getPrice().multiply(BigDecimal.valueOf(2)), responseBody.getTotal());
        assertEquals(cartRequest.getQuantity(), responseBody.getItems().size());
    }

    @Test
    public void remove_from_cart_happy_path() {
        Item item = getTestItem();
        User user = getTestUserWithCartItems();

        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setItemId(item.getId());
        cartRequest.setQuantity(1);
        cartRequest.setUsername(user.getUsername());

        ResponseEntity<Cart> responseEntity = cartController.removeFromCart(cartRequest);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        Cart responseBody = responseEntity.getBody();
        assertTrue(responseBody.getItems().isEmpty());
        assertEquals(item.getPrice().subtract(item.getPrice()), responseBody.getTotal());
    }

    @Test
    public void add_to_cart_with_invalid_user() {
        Item item = getTestItem();

        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setItemId(item.getId());
        cartRequest.setQuantity(1);
        cartRequest.setUsername("testUserNotFound");

        ResponseEntity<Cart> responseEntity = cartController.addToCart(cartRequest);

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    @Test
    public void add_to_cart_with_invalid_item() {
        Item item = getTestItem();
        item.setId(2L);
        User user = getTestUser();

        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setItemId(item.getId());
        cartRequest.setQuantity(1);
        cartRequest.setUsername(user.getUsername());

        ResponseEntity<Cart> responseEntity = cartController.addToCart(cartRequest);

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    @Test
    public void remove_from_cart_with_invalid_user() {
        Item item = getTestItem();

        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setItemId(item.getId());
        cartRequest.setQuantity(1);
        cartRequest.setUsername("testUserNotFound");

        ResponseEntity<Cart> responseEntity = cartController.removeFromCart(cartRequest);

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    @Test
    public void remove_from_cart_with_invalid_item() {
        Item item = getTestItem();
        item.setId(2L);
        User user = getTestUser();

        ModifyCartRequest cartRequest = new ModifyCartRequest();
        cartRequest.setItemId(item.getId());
        cartRequest.setQuantity(1);
        cartRequest.setUsername(user.getUsername());

        ResponseEntity<Cart> responseEntity = cartController.removeFromCart(cartRequest);

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    private User getTestUser() {
        User user = new User();
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        user.setId(1L);
        user.setUsername("test");
        user.setCart(cart);

        return user;
    }

    private User getTestUserWithCartItems() {
        User user = new User();
        Cart cart = new Cart();
        cart.setId(2L);
        cart.setUser(user);
        cart.addItem(getTestItem());
        user.setId(2L);
        user.setUsername("test2");
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
}
