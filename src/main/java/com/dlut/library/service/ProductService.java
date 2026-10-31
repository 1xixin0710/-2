package com.dlut.library.service;

import com.dlut.library.mapper.ProductMapper;
import com.dlut.library.model.Product;
import com.dlut.library.model.ProductQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<Product> searchStore(ProductQuery query) {
        normalizeQuery(query);
        return productMapper.searchStore(query);
    }

    public List<Product> searchAdmin(ProductQuery query) {
        normalizeQuery(query);
        return productMapper.searchAdmin(query);
    }

    public Product findById(Long id) {
        return productMapper.findById(id);
    }

    @Transactional
    public void create(Product product) {
        normalize(product);
        product.setSoldCount(0);
        productMapper.insert(product);
    }

    @Transactional
    public void update(Long id, Product product) {
        Product existing = productMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        normalize(product);
        product.setId(id);
        productMapper.update(product);
    }

    @Transactional
    public void deactivate(Long id) {
        if (productMapper.findById(id) == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        productMapper.deactivate(id);
    }

    public int countOnSale() {
        return productMapper.countOnSale();
    }

    public int sumStock() {
        return productMapper.sumStock();
    }

    public int countLowStock() {
        return productMapper.countLowStock();
    }

    public int countOutOfStock() {
        return productMapper.countOutOfStock();
    }

    private void normalize(Product product) {
        product.setIsbn(product.getIsbn().trim());
        product.setName(product.getName().trim());
        product.setAuthor(product.getAuthor().trim());
        product.setPublisher(product.getPublisher().trim());
        product.setSummary(product.getSummary().trim());
        product.setTags(String.join(",", product.getTagChoices()));
        product.setPrice(product.getPrice().setScale(2, RoundingMode.HALF_UP));
        product.setOriginalPrice(product.getOriginalPrice().setScale(2, RoundingMode.HALF_UP));
        if (product.getOriginalPrice().compareTo(product.getPrice()) < 0) {
            throw new IllegalArgumentException("划线价不能低于销售价");
        }
        if (product.getCoverUrl() == null || product.getCoverUrl().isBlank()) {
            product.setCoverUrl("/images/covers/default-book.jpg");
        } else {
            product.setCoverUrl(product.getCoverUrl().trim());
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("库存不能小于0");
        }
    }

    private void normalizeQuery(ProductQuery query) {
        query.setKeyword(blankToNull(query.getKeyword()));
        query.setCategory(blankToNull(query.getCategory()));
        query.setStockStatus(blankToNull(query.getStockStatus()));
        query.setPriceRange(blankToNull(query.getPriceRange()));
        query.setSort(blankToNull(query.getSort()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
