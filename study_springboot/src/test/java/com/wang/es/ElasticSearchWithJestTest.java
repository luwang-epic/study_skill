package com.wang.es;

import com.wang.model.User;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Springboot默认支持两种技术来和Es交互  需要注意和es的版本对应
 *  1. 使用Jest（默认不生效，需要引入Jest包）  这个项目目前已经不更新了，springboot也不在支持了（以前版本支持）
 *  2. SpringData ElasticSearch
 *        通过ElasticSearchTemplate操作es，编写子类继承ElasticSearchRepository接口来操作ES
 */
public class ElasticSearchWithJestTest {

    private JestClient jestClient;

    @BeforeEach
    public void beforeEach() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                        .Builder("http://localhost:9200")
                        .multiThreaded(true)
                        .build());
        jestClient = factory.getObject();
    }


    @Test
    public void addDocument() throws Exception {
        User user = new User();
        user.setName("zhangsan");
        user.setAge(22);

        Index index = new Index.Builder(user).index("user").id("zhangsan").build();
        jestClient.execute(index);
    }

    @Test
    public void searchDocument() throws Exception {
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"match_all\": {}\n" +
                "  }\n" +
                "}";
        Search search = new Search.Builder(query).addIndex("user").build();
        SearchResult searchResult = jestClient.execute(search);
        System.out.println("hit num ----->" + searchResult.getTotal());
    }
}
