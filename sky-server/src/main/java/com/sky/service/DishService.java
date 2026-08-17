package com.sky.service;

import com.sky.dto.DishDTO;

/**
 * 菜品业务接口
 */
public interface DishService {

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

}