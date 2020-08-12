package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private UserController userController;

    private final UserRepository userRepository = mock(UserRepository.class);

    private final CartRepository cartRepository = mock(CartRepository.class);

    private final BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);

    @Before
    public void init() {
        userController = new UserController();
        TestUtils.injectObjects(userController,"userRepository", userRepository);
        TestUtils.injectObjects(userController, "cartRepository", cartRepository);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);
    }

    @Test
    public void create_user_happy_path() {
        when(bCryptPasswordEncoder.encode("testpass")).thenReturn("thisIsHashed");

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test");
        createUserRequest.setPassword("testpass");
        createUserRequest.setConfirmPassword("testpass");

        final ResponseEntity<User> responseEntity = userController.createUser(createUserRequest);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        User u = responseEntity.getBody();
        assertNotNull(u);
        assertEquals(0, u.getId());
        assertEquals("test", u.getUsername());
        assertEquals("thisIsHashed", u.getPassword());
    }

    @Test
    public void find_by_username_happy_path() {
        User user = getTestUser();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        final ResponseEntity<User> responseEntity = userController.findByUserName(user.getUsername());

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        User responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(user.getId(), responseBody.getId());
        assertEquals(user.getUsername(), responseBody.getUsername());
        assertEquals(user.getPassword(), responseBody.getPassword());
    }

    @Test
    public void create_user_with_invalid_password() {
        // password shorter than 7
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("test");
        createUserRequest.setPassword("short");
        createUserRequest.setConfirmPassword("short");

        final ResponseEntity<User> responseEntity = userController.createUser(createUserRequest);

        assertNotNull(responseEntity);
        assertEquals(400, responseEntity.getStatusCodeValue());

        // password and confirm password not match case
        createUserRequest.setPassword("testPass");

        final ResponseEntity<User> responseEntity2 = userController.createUser(createUserRequest);

        assertNotNull(responseEntity2);
        assertEquals(400, responseEntity2.getStatusCodeValue());
    }

    @Test
    public void find_by_id_happy_path() {
        User user = getTestUser();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        final ResponseEntity<User> responseEntity = userController.findById(user.getId());

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        User responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(user.getId(), responseBody.getId());
        assertEquals(user.getUsername(), responseBody.getUsername());
        assertEquals(user.getPassword(), responseBody.getPassword());
    }

    @Test
    public void find_by_username_with_invalid_username() {
        when(userRepository.findByUsername("testNotFound")).thenReturn(null);

        final ResponseEntity<User> responseEntity = userController.findByUserName("testNotFound");

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    @Test
    public void find_by_id_with_invalid_id() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        final ResponseEntity<User> responseEntity = userController.findById(2L);

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
    }

    private User getTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("hashedPassword");

        return user;
    }
}
