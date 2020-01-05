package com.cxb.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * es Blog 是查询数据的  es Model 是模拟数据的
 */
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class EsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsApplication.class, args);
    }

}
