package cn.itcast.hotel;


import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.itcast.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

@SpringBootTest
public class HotelElasticsearchTest {

    private RestHighLevelClient client;

    @Autowired
    private IHotelService hotelService;


    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.88.109:9200")));
    }

    @AfterEach
    void tearDown() throws Exception {
        this.client.close();
    }

    @Test
    void testInit() {
        System.out.println(client);
    }

    @Test
    void createHotelIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void deleteHotelIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testExistsIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("hotel");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @Test
    void testIndexDocument() throws IOException {
        IndexRequest request = new IndexRequest("indexName").id("1");
        request.source("{\"name\":\"Java\",\"age\":21}", XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testAddDocument() throws IOException {
        List<Hotel> hotels = hotelService.list();
        HotelDoc hotelDoc = new HotelDoc(hotels.get(0));
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testAddDocumentBatch() throws IOException {
        List<Hotel> hotels = hotelService.list();
        BulkRequest bulkRequest = new BulkRequest();
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
            request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
            bulkRequest.add(request);
        }
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testGetDocumentById() throws IOException {
        GetRequest request = new GetRequest("hotel", "1937347815");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    @Test
    public void testUpdateDocumentById() throws IOException {
        UpdateRequest request = new UpdateRequest("hotel", "1937347815");
        request.doc("price", "900", "name", "自来也");
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    public void testDeleteDocumentById() throws IOException {
        DeleteRequest request = new DeleteRequest("hotel", "1937347815");
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    public void testMatchAll() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.out.println(JSON.parseObject(json, HotelDoc.class));
        }
        System.out.println(response);
    }

    @Test
    public void testMatchQuery() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.out.println(JSON.parseObject(json, HotelDoc.class));
        }
        System.out.println(response);
    }

    @Test
    public void testMultiMatchQuery() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.multiMatchQuery("如家", "name", "business"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.out.println(JSON.parseObject(json, HotelDoc.class));
        }
        System.out.println(response);
    }

    @Test
    public void testTermQuery() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.termQuery("city", "杭州"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.out.println(JSON.parseObject(json, HotelDoc.class));
        }
        System.out.println(response);
    }

    @Test
    public void testRangeQuery() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.rangeQuery("price").gte(100).lte(150));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.out.println(JSON.parseObject(json, HotelDoc.class));
        }
        System.out.println(response);
    }

    @Test
    public void testBooleanQuery() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("city", "上海"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(250));
        request.source().query(boolQueryBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.out.println(JSON.parseObject(json, HotelDoc.class));
        }
        System.out.println(response);
    }


    @Test
    public void testPageQuery() throws IOException {
        int page = 2, size = 2;
        SearchRequest request = new SearchRequest("hotel");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("city", "上海"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(250));
        request.source().query(boolQueryBuilder).from((page - 1) * size).size(size).sort("price", SortOrder.DESC);
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField != null) {
                    String name = highlightField.getFragments()[0].string();
                    hotelDoc.setName(name);
                }
            }
            System.out.println(hotelDoc);
        }
        System.out.println(response);
    }

    @Test
    void testAggregation() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().size(0);
        request.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(20));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Terms brandTerms = aggregations.get("brandAgg");
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String brandName = bucket.getKeyAsString();
            System.out.println(brandName);
        }
        System.out.println("==================");
    }

    @Test
    void testSuggest() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().suggest(new SuggestBuilder().addSuggestion("mySuggestion", SuggestBuilders.completionSuggestion("suggestion").prefix("h").skipDuplicates(true).size(10)));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Suggest suggest = response.getSuggest();
        CompletionSuggestion suggestion = suggest.getSuggestion("mySuggestion");
        for (CompletionSuggestion.Entry.Option option : suggestion.getOptions()) {
            String text = option.getText().string();
            System.out.println(text);
        }
        System.out.println(response);
    }


}
