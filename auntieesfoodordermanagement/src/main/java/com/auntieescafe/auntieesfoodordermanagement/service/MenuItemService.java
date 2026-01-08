package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.MenuItem;
import com.auntieescafe.auntieesfoodordermanagement.mapper.MenuItemMapper;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.MenuItemRequest;
import com.auntieescafe.auntieesfoodordermanagement.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper menuItemMapper;

    @Transactional
    public MenuItem createMenuItem(MenuItemRequest request) {
        MenuItem menuItem = menuItemMapper.toMenuItem(request);
        // Additional logic, e.g., generating a menu code if it's not provided
        if (menuItem.getMenuCode() == null || menuItem.getMenuCode().isEmpty()) {
            menuItem.setMenuCode(String.format("%04d", menuItemRepository.count() + 1));
        }
        return menuItemRepository.save(menuItem);
    }

    public Optional<MenuItem> getMenuItemById(UUID menuItemId) {
        return menuItemRepository.findById(menuItemId);
    }

    public Optional<MenuItem> getMenuItemByMenuCode(String menuCode) {
        return menuItemRepository.findByMenuCode(menuCode);
    }

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public List<MenuItem> getActiveMenuItems() {
        return menuItemRepository.findByIsActiveTrue();
    }

    @Transactional
    public MenuItem updateMenuItem(UUID menuItemId, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item not found with id: " + menuItemId));
        
        menuItemMapper.updateMenuItemFromRequest(request, menuItem);
        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public void deleteMenuItem(UUID menuItemId) {
        if (!menuItemRepository.existsById(menuItemId)) {
            throw new RuntimeException("Menu Item not found with id: " + menuItemId);
        }
        menuItemRepository.deleteById(menuItemId);
    }

    @Transactional
    public void toggleMenuItemStatus(UUID menuItemId, boolean isActive) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item not found with id: " + menuItemId));
        menuItem.setActive(isActive);
        menuItemRepository.save(menuItem);
    }
}
