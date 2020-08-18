package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItemControllerTest {

    private ItemController itemController;

    private final ItemRepository itemRepository = mock(ItemRepository.class);

    @Before
    public void init() {
        itemController = new ItemController();
        TestUtils.injectObjects(itemController, "itemRepository", itemRepository);

        List<Item> itemsList = new ArrayList<>();
        itemsList.add(getTestItem());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(getTestItem()));
        when(itemRepository.findByName("Round Widget")).thenReturn(itemsList);
        when(itemRepository.findByName("Square Widget")).thenReturn(new ArrayList<>());
    }

    @Test
    public void get_item_by_id_happy_path() {
        Item item = getTestItem();

        ResponseEntity<Item> responseEntity = itemController.getItemById(item.getId());

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        Item responseBody = responseEntity.getBody();

        assertNotNull(responseBody);
        assertEquals(item.getId(), responseBody.getId());
        assertEquals(item.getName(), responseBody.getName());
        assertEquals(item.getPrice(), responseBody.getPrice());
        assertEquals(item.getDescription(), responseBody.getDescription());
    }

    @Test
    public void get_item_by_name_happy_path() {
        Item item = getTestItem();

        ResponseEntity<List<Item>> responseEntity = itemController.getItemsByName(item.getName());

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCodeValue());

        List<Item> responseBody = responseEntity.getBody();

        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals(responseBody.get(0).getId(), item.getId());
        assertEquals(responseBody.get(0).getName(), item.getName());
        assertEquals(responseBody.get(0).getPrice(), item.getPrice());
        assertEquals(responseBody.get(0).getDescription(), item.getDescription());
    }

    @Test
    public void get_item_by_name_not_found_path() {
        ResponseEntity<List<Item>> responseEntity = itemController.getItemsByName("Square Widget");

        assertNotNull(responseEntity);
        assertEquals(404, responseEntity.getStatusCodeValue());
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
