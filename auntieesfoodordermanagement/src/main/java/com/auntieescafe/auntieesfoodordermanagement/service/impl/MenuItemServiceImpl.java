package com.auntieescafe.auntieesfoodordermanagement.service.impl;

import com.auntieescafe.auntieesfoodordermanagement.entity.MenuItem;
import com.auntieescafe.auntieesfoodordermanagement.repository.MenuItemRepository;
import com.auntieescafe.auntieesfoodordermanagement.service.MenuItemService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private MenuItemRepository menuItemRepository;

    @Override
    public MenuItem createMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    @Override
    public Optional<MenuItem> getMenuItemById(UUID menuItemId) {
        return menuItemRepository.findById(menuItemId);
    }

    @Override
    public Optional<MenuItem> getMenuItemByMenuCode(String menuCode) {
        return menuItemRepository.findByMenuCode(menuCode);
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Override
    public List<MenuItem> getActiveMenuItems() {
        return menuItemRepository.findByIsActiveTrue();
    }

    @Override
    public MenuItem updateMenuItem(UUID menuItemId, MenuItem updatedMenuItem) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item not found with id: " + menuItemId));
        menuItem.setMenuCode(updatedMenuItem.getMenuCode());
        menuItem.setName(updatedMenuItem.getName());
        menuItem.setPrice(updatedMenuItem.getPrice());
        menuItem.setCategory(updatedMenuItem.getCategory());
        menuItem.setActive(updatedMenuItem.isActive());
        return menuItemRepository.save(menuItem);
    }

    @Override
    public void deleteMenuItem(UUID menuItemId) {
        menuItemRepository.deleteById(menuItemId);
    }

    @Override
    public void toggleMenuItemStatus(UUID menuItemId, boolean isActive) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu Item not found with id: " + menuItemId));
        menuItem.setActive(isActive);
        menuItemRepository.save(menuItem);
    }
}
