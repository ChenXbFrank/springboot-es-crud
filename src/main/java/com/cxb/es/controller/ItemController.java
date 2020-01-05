package com.cxb.es.controller;

import com.alibaba.fastjson.JSONObject;
import com.cxb.es.entity.Item;
import com.cxb.es.repository.EsRepository;
import org.assertj.core.util.Lists;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", 3299.00, "http://image.baidu.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.baidu.com/13123.jpg"));
        list.add(new Item(3L, "华为META10", "手机", "华为", 4499.00, "http://image.baidu.com/13123.jpg"));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.baidu.com/13123.jpg"));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", 2799.00, "http://image.baidu.com/13123.jpg"));

        // 接收对象集合，实现批量新增
        esRepository.saveAll(list);
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

    /**
     * 自定义查询: matchQuery
     */
    @GetMapping("matchQuery")
    public Page<Item> matchQuery() {
        // 构建查询条件
        // NativeSearchQueryBuilder：Spring提供的一个查询条件构建器, 帮助构建json格式的请求体
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 添加基本分词查询
        // QueryBuilders.matchQuery(“title”, “小米手机”)：利用QueryBuilders来生成一个查询。
        // QueryBuilders提供了大量的静态方法, 用于生成各种不同类型的查询：
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "小米手机"));

        // 搜索, 获取结果
        // Page<item>：默认是分页查询, 因此返回的是一个分页的结果对象, 包含属性：
        // totalElements：总条数
        // totalPages：总页数
        // Iterator：迭代器, 本身实现了Iterator接口, 因此可直接迭代得到当前页的数据
        Page<Item> page = this.esRepository.search(queryBuilder.build());
        for (Item item : page) {
            System.out.println(item);
        }

        // 总条数
        long total = page.getTotalElements();
        System.out.println("total = " + total);

        // Iterator<Item> iterable = page.iterator();

        return page;
    }

    /**
     * 自定义查询: termQuery
     * termQuery: 功能更强大, 除了匹配字符串以外, 还可以匹配 int/long/double/float/....
     */
    @GetMapping("termQuery")
    public Page<Item> termQuery() {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(QueryBuilders.termQuery("price", 998.0));

        // 查找
        Page<Item> page = this.esRepository.search(builder.build());
        for (Item item : page) {
            System.out.println(item);
        }

        return page;
    }

    /**
     * 自定义查询: fuzzyQuery -- 模糊查询
     */
    @GetMapping("fuzzyQuery")
    public Page<Item> fuzzyQuery() {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(QueryBuilders.fuzzyQuery("title", "faceoooo"));
        Page<Item> page = this.esRepository.search(builder.build());

        for (Item item : page) {
            System.out.println(item + "");
        }

        return page;
    }

    /**
     * 分页查询
     */
    @GetMapping("searchByPage")
    public Page<Item> searchByPage() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));

        // 分页：
        int pageNum = 0;
        int size    = 2;
        queryBuilder.withPageable(PageRequest.of(pageNum, size));

        // 搜索, 获取结果
        Page<Item> page = this.esRepository.search(queryBuilder.build());

        // 总条数
        long total = page.getTotalElements();
        System.out.println("总条数 = " + total);

        // 总页数
        System.out.println("总页数 = " + page.getTotalPages());

        // 当前页
        System.out.println("当前页：" + page.getNumber());

        // 每页大小
        System.out.println("每页大小：" + page.getSize());

        for (Item item : page) {
            System.out.println(item);
        }

        return page;
    }

    /**
     * 排序查询
     */
    @GetMapping("searchAndSort")
    public Page<Item> searchAndSort() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 添加基本分词查询
        queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));

        // 排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));

        // 搜索, 获取结果
        Page<Item> page = this.esRepository.search(queryBuilder.build());

        // 总条数
        long totalNum = page.getTotalElements();
        System.out.println("总条数: " + totalNum);

        // 总页数
        System.out.println("总页数 = " + page.getTotalPages());

        // 当前页
        System.out.println("当前页： " + page.getNumber());

        // 每页大小
        System.out.println("每页大小：" + page.getSize());

        for (Item item : page) {
            System.out.println(item);
        }

        return page;
    }

// 聚合查询:
//   比较常用的一些度量聚合方式:
//     Avg Aggregation：求平均值
//     Max Aggregation：求最大值
//     Min Aggregation：求最小值
//     Percentiles Aggregation：求百分比
//     Stats Aggregation：同时返回avg、max、min、sum、count等
//     Sum Aggregation：求和
//     Top hits Aggregation：求前几
//     Value Count Aggregation：求总数
//     ……
    /**
     * Elasticsearch中的聚合, 包含多种类型, 最常用的两种, 一个叫桶, 一个叫度量：
     * 聚合查询: 聚合为桶 -- 查询
     * aggregation bucket 查询
     */
    @GetMapping("aggBucketSearch")
    public AggregatedPage<Item> aggBucketSearch() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));

        // 1、添加一个新的聚合, 聚合类型为terms, 聚合名称为brands, 聚合字段为brand
        // AggregationBuilders：聚合的构建工厂类, 所有聚合都由这个类来构建
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("brand"));

        // 2、查询, 需要把结果强转为AggregatedPage类型
        AggregatedPage<Item> aggPage = (AggregatedPage<Item>)
                this.esRepository.search(queryBuilder.build());

        // 3 解析
        // 3.1 从结果中取出名为brands的那个聚合,
        // 因为是利用String类型字段来进行的term聚合, 所以结果要强转为StringTerm类型
        StringTerms agg = (StringTerms) aggPage.getAggregation("brands");

        // 3.2、获取桶
        List<StringTerms.Bucket> buckets = agg.getBuckets();

        // 3.3、遍历
        for (StringTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key, 即品牌名称
            System.out.println(bucket.getKeyAsString());
            // 3.5、获取桶中的文档数量
            System.out.println(bucket.getDocCount());
        }

        return aggPage;
    }

// （1）统计某个字段的数量
//     ValueCountBuilder vcb=  AggregationBuilders.count("count_uid").field("uid");
// （2）去重统计某个字段的数量（有少量误差）
//     CardinalityBuilder cb= AggregationBuilders.cardinality("distinct_count_uid").field("uid");
// （3）聚合过滤
//     FilterAggregationBuilder fab= AggregationBuilders.filter("uid_filter")
//                                   .filter(QueryBuilders.queryStringQuery("uid:001"));
// （4）按某个字段分组
//     TermsBuilder tb=  AggregationBuilders.terms("group_name").field("name");
// （5）求和
//     SumBuilder  sumBuilder=	AggregationBuilders.sum("sum_price").field("price");
// （6）求平均
//     AvgBuilder ab= AggregationBuilders.avg("avg_price").field("price");
// （7）求最大值
//     MaxBuilder mb= AggregationBuilders.max("max_price").field("price");
// （8）求最小值
//     MinBuilder min=	AggregationBuilders.min("min_price").field("price");
// （9）按日期间隔分组
//     DateHistogramBuilder dhb= AggregationBuilders.dateHistogram("dh").field("date");
// （10）获取聚合里面的结果
//     TopHitsBuilder thb=  AggregationBuilders.topHits("top_result");
// （11）嵌套的聚合
//     NestedBuilder nb= AggregationBuilders.nested("negsted_path").path("quests");
// （12）反转嵌套
//    AggregationBuilders.reverseNested("res_negsted").path("kps ");

    /**
     * 聚合查询: 嵌套聚合, 求平均值
     */
    @GetMapping("aggSubSearch")
    public AggregatedPage<Item> aggSubSearch() {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));

        // 1、添加一个新的聚合, 聚合类型为terms, 聚合名称为brands, 聚合字段为brand
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("brand")
                // 在品牌聚合桶内进行嵌套聚合, 求平均值
                .subAggregation(AggregationBuilders.avg("priceAvg").field("price")));

        // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPage<Item> aggPage = (AggregatedPage<Item>)
                this.esRepository.search(queryBuilder.build());

        // 3 解析
        // 3.1 从结果中取出名为brands的那个聚合, 
        // 因为是利用String类型字段来进行的term聚合, 所以结果要强转为StringTerm类型
        StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
        // 3.2、获取桶
        List<StringTerms.Bucket> buckets = agg.getBuckets();
        // 3.3、遍历
        for (StringTerms.Bucket bucket : buckets) {
            // 3.4、获取桶中的key, 即品牌名称  3.5、获取桶中的文档数量
            System.out.println(bucket.getKeyAsString() + ", 共" + bucket.getDocCount() + "台");

            // 3.6.获取子聚合结果：
            InternalAvg avg = (InternalAvg) bucket.getAggregations().asMap().get("priceAvg");
            System.out.println("平均售价：" + avg.getValue());
        }

        return aggPage;
    }
}
