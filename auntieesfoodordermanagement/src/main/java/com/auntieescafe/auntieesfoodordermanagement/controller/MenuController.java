package com.auntieescafe.auntieesfoodordermanagement.controller;

import com.auntieescafe.auntieesfoodordermanagement.entity.MenuItem;
import com.auntieescafe.auntieesfoodordermanagement.mapper.MenuItemMapper;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.MenuItemRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.MenuItemResponse;
import com.auntieescafe.auntieesfoodordermanagement.service.MenuItemService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final MenuItemMapper menuItemMapper;

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getMenu() {
        List<MenuItem> menuItems = menuItemService.getActiveMenuItems();
        return ResponseEntity.ok(menuItemMapper.toMenuItemResponseList(menuItems));
    }

    @GetMapping("/search")
    public ResponseEntity<MenuItemResponse> getMenuItemByMenuCode(@RequestParam("code") String menuCode) {
        log.info("Searching for menu item with code: {}", menuCode);
        return menuItemService.getMenuItemByMenuCode(menuCode)
                .map(menuItemMapper::toMenuItemResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> addMenuItem(@RequestBody MenuItemRequest request) {
        log.info("Admin adding new menu item: {}", request.getName());
        MenuItem createdItem = menuItemService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemMapper.toMenuItemResponse(createdItem));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable UUID id, @RequestBody MenuItemRequest request) {
        log.info("Admin updating menu item: {}", id);
        try {
            MenuItem updatedItem = menuItemService.updateMenuItem(id, request);
            return ResponseEntity.ok(menuItemMapper.toMenuItemResponse(updatedItem));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMenuItem(@PathVariable UUID id) {
        log.info("Admin deleting menu item: {}", id);
        try {
            menuItemService.deleteMenuItem(id);
            return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
