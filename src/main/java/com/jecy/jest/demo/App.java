package com.jecy.jest.demo;

import com.alibaba.fastjson.JSON;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {

        searchingDocuments(getJestClient());
        System.out.println("Hello Jest!");
    }

    public static void searchingDocuments(JestClient jestClient) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("user","kimchy"));
        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex("articles").addType("articles").build();
        SearchResult searchResult =  jestClient.execute(search);
        System.out.println("搜索数据:"+searchResult.getJsonString());

    }
    public static void indexDocument(JestClient jestClient) throws IOException {
        String resource = jsonBuilder().startObject()
                .field("user", "kimchy")
                .field("postDate", "date")
                .field("message", "trying out Elastic Search")
                .endObject().string();
        Index index = new Index.Builder(resource).index("articles").type("articles").build();
    }

    public static void createMapping(JestClient jestClient) throws IOException {
        PutMapping putMapping = new PutMapping.Builder(
                "my_index",
                "my_type",
                "{ \"my_type\" : { \"properties\" : { \"message\" : {\"type\" : \"string\", \"store\" : \"yes\"} } } }"
        ).build();
        jestClient.execute(putMapping);
    }

    public static void createIndex(JestClient jestClient) throws IOException {

        Settings.Builder settingsBuilder = Settings.builder();
        settingsBuilder.put("number_of_shards", "3");
        settingsBuilder.put("number_of_replicas", "1");
        jestClient.execute(new CreateIndex.Builder("articles").
                settings(settingsBuilder.build().getAsMap()).build());
    }

    public static JestClient getJestClient() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200").multiThreaded(true).build());
        JestClient jestClient = factory.getObject();
        return jestClient;
    }
}
