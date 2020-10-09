package com.cmh.dao;

import com.cmh.entity.Stock;

public interface StockDAO {
    Stock checkStock(Integer id);//检验库存
    int updateSale(Stock stock);//扣除库存
}
