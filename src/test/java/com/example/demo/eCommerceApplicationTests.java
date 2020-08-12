package com.example.demo;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.LoginUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.example.demo.security.SecurityConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = eCommerceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class eCommerceApplicationTests {

	@LocalServerPort
	private String port;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void create_user_and_login() {
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUsername("test1");

		// Test password and password confirmation not match
		createUserRequest.setPassword("test1Pass");
		createUserRequest.setConfirmPassword("test2Pass");

		final ResponseEntity<User> responseEntity1 =
				testRestTemplate.postForEntity("http://localhost:" + port + "/api/user/create", createUserRequest, User.class);

		assertNotNull(responseEntity1);
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity1.getStatusCode());

		// Test password too short
		createUserRequest.setPassword("test");
		createUserRequest.setConfirmPassword("test");

		final ResponseEntity<User> responseEntity2 =
				testRestTemplate.postForEntity("http://localhost:" + port + "/api/user/create", createUserRequest, User.class);

		assertNotNull(responseEntity2);
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity1.getStatusCode());

		// Test create user success
		createUserRequest.setPassword("testPass");
		createUserRequest.setConfirmPassword("testPass");

		final ResponseEntity<User> responseEntity3 =
				testRestTemplate.postForEntity("http://localhost:" + port + "/api/user/create", createUserRequest, User.class);

		User createdUser = responseEntity3.getBody();

		assertNotNull(responseEntity3);
		assertEquals(HttpStatus.OK, responseEntity3.getStatusCode());
		assertEquals("test1", createdUser.getUsername());
		assertNotEquals("testPass", createdUser.getPassword()); // password should be hashed

		// Test login fail
		LoginUserRequest loginUserRequest = new LoginUserRequest();
		loginUserRequest.setUsername("test1");
		loginUserRequest.setPassword("invalidTestPass");

		final ResponseEntity responseEntity4 = testRestTemplate.postForEntity("http://localhost:" + port + "/login", loginUserRequest, null);

		assertNotNull(responseEntity4);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity4.getStatusCode());
		assertNull(responseEntity4.getHeaders().get(SecurityConstants.HEADER_STRING));

		// Test login success
		loginUserRequest.setPassword("testPass");

		final ResponseEntity responseEntity5 = testRestTemplate.postForEntity("http://localhost:" + port + "/login", loginUserRequest, null);

		assertNotNull(responseEntity5);
		assertEquals(HttpStatus.OK, responseEntity5.getStatusCode());
		assertNotNull(responseEntity5.getHeaders().get(SecurityConstants.HEADER_STRING));
	}

	@Test
	public void find_user() {
		User expectedUser = createTestUser("test2");
		HttpHeaders headers = getHttpWithJwtToken(getLoginTokenForTestUser(expectedUser));
		User actualUser;

		// Test get user by username with auth token
		HttpEntity request = new HttpEntity(null, headers);
		ResponseEntity<User> responseEntity1 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/user/" + expectedUser.getUsername(), HttpMethod.GET, request, User.class);

		assertNotNull(responseEntity1);
		assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
		actualUser = responseEntity1.getBody();
		assertEquals(expectedUser.getUsername(), actualUser.getUsername());

		// Test get user by id with auth token
		ResponseEntity<User> responseEntity2 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/user/id/" + expectedUser.getId(), HttpMethod.GET, request, User.class);

		assertNotNull(responseEntity2);
		assertEquals(HttpStatus.OK, responseEntity2.getStatusCode());
		actualUser = responseEntity2.getBody();
		assertEquals(expectedUser.getUsername(), actualUser.getUsername());

		// Test get user by username without auth token
		ResponseEntity<User> responseEntity3 =
				testRestTemplate.getForEntity("http://localhost:" + port + "/api/user/" + expectedUser.getUsername(), User.class);
		assertNotNull(responseEntity3);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity3.getStatusCode());

		// Test get user by id without auth token
		ResponseEntity<User> responseEntity4 =
				testRestTemplate.getForEntity("http://localhost:" + port + "/api/user/id/" + expectedUser.getId(), User.class);
		assertNotNull(responseEntity4);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity4.getStatusCode());
	}

	@Test
	public void get_item_or_items() {
		User user = createTestUser("test3");
		HttpHeaders headers = getHttpWithJwtToken(getLoginTokenForTestUser(user));
		HttpEntity request = new HttpEntity(null, headers);

		// test get all items with auth header
		ResponseEntity<Item[]> responseEntity1 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/item", HttpMethod.GET, request, Item[].class);

		assertNotNull(responseEntity1);
		assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
		Item[] items = responseEntity1.getBody();
		assertEquals(2, items.length);
		assertEquals("Round Widget", items[0].getName());
		assertEquals("A widget that is round", items[0].getDescription());
		assertEquals(BigDecimal.valueOf(2.99), items[0].getPrice());
		assertEquals("Square Widget", items[1].getName());
		assertEquals("A widget that is square", items[1].getDescription());
		assertEquals(BigDecimal.valueOf(1.99), items[1].getPrice());

		// test get item by id with auth header
		ResponseEntity<Item> responseEntity2 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/item/1", HttpMethod.GET, request, Item.class);
		assertNotNull(responseEntity2);
		assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
		Item item1 = responseEntity2.getBody();
		assertEquals("Round Widget", item1.getName());
		assertEquals("A widget that is round", item1.getDescription());
		assertEquals(BigDecimal.valueOf(2.99), item1.getPrice());

		// test get item by name with auth header
		ResponseEntity<Item[]> responseEntity3 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/item/name/Square Widget", HttpMethod.GET, request, Item[].class);
		assertNotNull(responseEntity3);
		assertEquals(HttpStatus.OK, responseEntity3.getStatusCode());
		items = responseEntity3.getBody();
		assertEquals("Square Widget", items[0].getName());
		assertEquals("A widget that is square", items[0].getDescription());
		assertEquals(BigDecimal.valueOf(1.99), items[0].getPrice());

		// test get all items without auth header
		ResponseEntity responseEntity4 =
				testRestTemplate.getForEntity("http://localhost:" + port + "/api/item", Item.class);
		assertNotNull(responseEntity4);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity4.getStatusCode());

		// test get item by id without auth header
		ResponseEntity<Item> responseEntity5 =
				testRestTemplate.getForEntity("http://localhost:" + port + "/api/item/1", Item.class);
		assertNotNull(responseEntity5);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity5.getStatusCode());

		// test get item by name without auth header
		ResponseEntity responseEntity6 =
				testRestTemplate.getForEntity("http://localhost:" + port + "/api/item/name/Square Widget", Item.class);
		assertNotNull(responseEntity6);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity6.getStatusCode());
	}

	@Test
	public void add_to_and_remove_from_cart() {
		User user = createTestUser("test4");
		HttpHeaders headers = getHttpWithJwtToken(getLoginTokenForTestUser(user));

		ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
		modifyCartRequest.setItemId(1L);
		modifyCartRequest.setQuantity(4);
		modifyCartRequest.setUsername(user.getUsername());

		// Test add 4 items to cart with auth header
		HttpEntity request = new HttpEntity(modifyCartRequest, headers);
		ResponseEntity<Cart> responseEntity1 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/cart/addToCart", HttpMethod.POST, request, Cart.class);
		assertNotNull(responseEntity1);
		assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
		Cart cart = responseEntity1.getBody();
		assertEquals(4, cart.getItems().size());
		assertEquals(BigDecimal.valueOf(2.99).multiply(BigDecimal.valueOf(4)), cart.getTotal());
		assertEquals(user.getUsername(), cart.getUser().getUsername());

		// Test remove 3 items from cart with auth header
		modifyCartRequest.setQuantity(3);
		ResponseEntity<Cart> responseEntity2 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/cart/removeFromCart", HttpMethod.POST, request, Cart.class);
		assertNotNull(responseEntity2);
		assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
		cart = responseEntity2.getBody();
		assertEquals(1, cart.getItems().size());
		assertEquals(BigDecimal.valueOf(2.99), cart.getTotal());
		assertEquals(user.getUsername(), cart.getUser().getUsername());

		// Test add items to cart without auth header
		modifyCartRequest.setQuantity(5);
		ResponseEntity<Cart> responseEntity3 =
				testRestTemplate.postForEntity("http://localhost:" + port + "/api/cart/addToCart", modifyCartRequest, Cart.class);
		assertNotNull(responseEntity3);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity3.getStatusCode());

		// Test remove from cart without auth header
		modifyCartRequest.setQuantity(1);
		ResponseEntity<Cart> responseEntity4 =
				testRestTemplate.postForEntity("http://localhost:" + port + "/api/cart/removeFromCart", modifyCartRequest, Cart.class);
		assertNotNull(responseEntity4);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity4.getStatusCode());
	}

	@Test
	public void create_and_get_order() {
		User user = createTestUser("test5");
		HttpHeaders headers = getHttpWithJwtToken(getLoginTokenForTestUser(user));
		ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
		modifyCartRequest.setItemId(1L);
		modifyCartRequest.setQuantity(4);
		modifyCartRequest.setUsername(user.getUsername());
		HttpEntity request = new HttpEntity(modifyCartRequest, headers);

		testRestTemplate.exchange("http://localhost:" + port + "/api/cart/addToCart", HttpMethod.POST, request, Cart.class);

		modifyCartRequest.setItemId(2L);
		modifyCartRequest.setQuantity(5);

		testRestTemplate.exchange("http://localhost:" + port + "/api/cart/addToCart", HttpMethod.POST, request, Cart.class);

		// Test create order with auth header
		ResponseEntity<UserOrder> responseEntity1 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/order/submit/" + user.getUsername(), HttpMethod.POST, request, UserOrder.class);
		assertNotNull(responseEntity1);
		assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
		UserOrder order = responseEntity1.getBody();
		assertEquals(9, order.getItems().size());
		assertEquals(BigDecimal.valueOf(21.91), order.getTotal());
		assertEquals(user.getUsername(), order.getUser().getUsername());

		// Empty out cart
		modifyCartRequest.setItemId(1L);
		modifyCartRequest.setQuantity(4);
		testRestTemplate.postForEntity("http://localhost:" + port + "/api/cart/removeFromCart", modifyCartRequest, Cart.class);

		modifyCartRequest.setItemId(2L);
		modifyCartRequest.setQuantity(5);
		testRestTemplate.postForEntity("http://localhost:" + port + "/api/cart/removeFromCart", modifyCartRequest, Cart.class);

		// Create another order
		modifyCartRequest.setItemId(1L);
		modifyCartRequest.setQuantity(10);
		testRestTemplate.exchange("http://localhost:" + port + "/api/cart/addToCart", HttpMethod.POST, request, Cart.class);
		testRestTemplate.exchange("http://localhost:" + port + "/api/order/submit/" + user.getUsername(), HttpMethod.POST, request, UserOrder.class);

		// Test get user order history with auth header
		ResponseEntity<UserOrder[]> responseEntity2 =
				testRestTemplate.exchange("http://localhost:" + port + "/api/order/history/" + user.getUsername(), HttpMethod.GET, request, UserOrder[].class);
		assertNotNull(responseEntity2);
		assertEquals(HttpStatus.OK, responseEntity2.getStatusCode());
		UserOrder[] orders = responseEntity2.getBody();
		assertEquals(2, orders.length);

		// Test create order without auth header
		ResponseEntity<UserOrder> responseEntity3 =
				testRestTemplate.postForEntity("http://localhost:" + port + "/api/order/submit/" + user.getUsername(), modifyCartRequest, UserOrder.class);
		assertNotNull(responseEntity3);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity3.getStatusCode());

		// Test get order history without auth header
		ResponseEntity<UserOrder> responseEntity4 =
				testRestTemplate.getForEntity("http://localhost:" + port + "/api/order/history/" + user.getUsername(), UserOrder.class);
		assertNotNull(responseEntity4);
		assertEquals(HttpStatus.FORBIDDEN, responseEntity4.getStatusCode());
	}

	private User createTestUser(String name) {
		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUsername(name);
		createUserRequest.setPassword("testPass");
		createUserRequest.setConfirmPassword("testPass");

		return testRestTemplate.postForEntity("http://localhost:" + port + "/api/user/create", createUserRequest, User.class).getBody();
	}

	private String getLoginTokenForTestUser(User user) {
		return JWT.create()
				.withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
				.sign(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes()));
	}

	private HttpHeaders getHttpWithJwtToken(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);

		return headers;
	}
}
