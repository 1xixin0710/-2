package com.dlut.library.mapper;

import com.dlut.library.model.Product;
import com.dlut.library.model.ProductQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {

    List<Product> searchStore(ProductQuery query);

    List<Product> searchAdmin(ProductQuery query);

    Product findById(Long id);

    int insert(Product product);

    int update(Product product);

    int deactivate(Long id);

    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    int incrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    int incrementSold(@Param("id") Long id, @Param("quantity") int quantity);

    int decrementSold(@Param("id") Long id, @Param("quantity") int quantity);

    int countOnSale();

    int sumStock();

    int countLowStock();

    int countOutOfStock();
}
