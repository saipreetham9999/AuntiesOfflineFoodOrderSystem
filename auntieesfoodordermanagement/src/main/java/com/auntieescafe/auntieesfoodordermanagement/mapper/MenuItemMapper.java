package com.auntieescafe.auntieesfoodordermanagement.mapper;

import com.auntieescafe.auntieesfoodordermanagement.entity.MenuItem;
import com.auntieescafe.auntieesfoodordermanagement.payload.request.MenuItemRequest;
import com.auntieescafe.auntieesfoodordermanagement.payload.response.MenuItemResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuItemMapper {

    public MenuItemResponse toMenuItemResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getMenuCode(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.isActive()
        );
    }

    public List<MenuItemResponse> toMenuItemResponseList(List<MenuItem> menuItems) {
        return menuItems.stream()
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());
    }

    public void updateMenuItemFromRequest(MenuItemRequest request, MenuItem menuItem) {
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setActive(request.isActive());
    }

    public MenuItem toMenuItem(MenuItemRequest request) {
        MenuItem menuItem = new MenuItem();
        updateMenuItemFromRequest(request, menuItem);
        return menuItem;
    }
}
