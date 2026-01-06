package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.MenuItem;
import com.auntieescafe.auntieesfoodordermanagement.service.MenuItemService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu")
@AllArgsConstructor
@Slf4j
public class MenuController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<List<MenuItem>> getMenu() {
        return ResponseEntity.ok(menuItemService.getActiveMenuItems());
    }

    @GetMapping("/search")
    public ResponseEntity<MenuItem> getMenuItemByMenuCode(@RequestParam("code") String menuCode) {
        log.info("Searching for menu item with code: {}", menuCode);
        return menuItemService.getMenuItemByMenuCode(menuCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MenuItem> addMenuItem(@RequestBody MenuItem menuItem) {
        log.info("Admin adding new menu item: {}", menuItem.getName());
        MenuItem createdItem = menuItemService.createMenuItem(menuItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable UUID id, @RequestBody MenuItem menuItem) {
        log.info("Admin updating menu item: {}", id);
        MenuItem updatedItem = menuItemService.updateMenuItem(id, menuItem);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable UUID id) {
        log.info("Admin deleting menu item: {}", id);
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
    }
}
