package com.auntieescafe.auntieesfoodordermanagement.service;

import com.auntieescafe.auntieesfoodordermanagement.entity.MenuItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemService {
    MenuItem createMenuItem(MenuItem menuItem);
    Optional<MenuItem> getMenuItemById(UUID menuItemId);
    Optional<MenuItem> getMenuItemByMenuCode(String menuCode);
    List<MenuItem> getAllMenuItems();
    List<MenuItem> getActiveMenuItems();
    MenuItem updateMenuItem(UUID menuItemId, MenuItem menuItem);
    void deleteMenuItem(UUID menuItemId);
    void toggleMenuItemStatus(UUID menuItemId, boolean isActive);
}
