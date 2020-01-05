package com.cxb.es.repository;

import com.cxb.es.entity.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface EsRepository extends ElasticsearchRepository<Item, Long> {
    List<Item> findByPriceBetween(double price1, double price2);
}
