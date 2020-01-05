package com.cxb.es.controller;

import com.alibaba.fastjson.JSONObject;
import com.cxb.es.entity.Item;
import com.cxb.es.repository.EsRepository;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private EsRepository esRepository;

    /**
     * 创建索引  这里好像不需要测试 直接就会创建item的索引 在entity里面创建的 不是很清楚0.0
     */
    @GetMapping("/createIndex")
    public JSONObject createIndex(Object obj) {
        JSONObject json = new JSONObject();
        boolean result = elasticsearchTemplate.createIndex(obj.getClass());
        json.put("result", result);
        return json;
    }

    @GetMapping("/insert")
    public String insert() {
        Item item = new Item(1L, "华为mete30", " 手机",
                "华为", 6499.00, "http://image.baidu.com/13123.jpg");
        esRepository.save(item);
        return "success";
    }

    @GetMapping("/insert1")
    public String insert1() {
        Item item = new Item(2L, "小米9", " 手机",
                "小米", 3499.00, "http://image.baidu.com/13123.jpg");
        esRepository.save(item);
        return "success";
    }

    //http://localhost:8085/delete?id=2
    @GetMapping("/delete")
    public String delete(long id) {
        esRepository.deleteById(id);
        return "success";
    }

    /**
     * elasticsearch中本没有修改，它的修改原理是该是先删除在新增
     * 修改和新增是同一个接口，区分的依据就是id。
     */
    //http://localhost:8085/item/update?id=2&title=%E4%BF%AE%E6%94%B9&brand=%E8%8B%B9%E6%9E%9C
    @GetMapping("/update")
    public String update(Long id, String title, String category, String brand, Double price, String images) {
        Item Item = new Item(id, title, category, brand, price, images);
        esRepository.save(Item);
        return "success";
    }

    @RequestMapping("/findAll")
    public List<Item> findAll() {
        Iterable<Item> iterable = esRepository.findAll();
        List<Item> list = Lists.newArrayList(iterable);
        return list;
    }
}
