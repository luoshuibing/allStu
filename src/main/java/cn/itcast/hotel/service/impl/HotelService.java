package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams requestParams) {
        try {
            SearchRequest request = new SearchRequest("hotel");
            buildQueryParamFunctionScore(requestParams, request);
            int page = requestParams.getPage(), size = requestParams.getSize();
            request.source().from((page - 1) * size).size(size);
            String location = requestParams.getLocation();
            if (!StringUtils.isEmpty(location)) {
                request.source().sort(SortBuilders.geoDistanceSort("location", new GeoPoint(location)).order(SortOrder.ASC).unit(DistanceUnit.KILOMETERS));
            }
            request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return handlerResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<String>> filters(RequestParams requestParams) {
        try {
            Map<String, List<String>> map = new HashMap<>();
            SearchRequest request = new SearchRequest("hotel");
            request.source().size(0);
            buildQueryParamFunctionScore(requestParams,request);
            buildAggregation(request);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            Aggregations aggregations = response.getAggregations();
            List<String> brandAgg = getResponseByName(aggregations, "brandAgg");
            List<String> cityAgg = getResponseByName(aggregations, "cityAgg");
            List<String> starNameAgg = getResponseByName(aggregations, "starNameAgg");
            map.put("brand", brandAgg);
            map.put("city", cityAgg);
            map.put("starName", starNameAgg);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getSuggestions(String key) {
        try {
            List<String> result = new ArrayList<>();
            SearchRequest request = new SearchRequest("hotel");
            request.source().suggest(new SuggestBuilder().addSuggestion("mySuggestion", SuggestBuilders.completionSuggestion("suggestion").prefix(key).skipDuplicates(true).size(10)));
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            Suggest suggest = response.getSuggest();
            CompletionSuggestion suggestion = suggest.getSuggestion("mySuggestion");
            for (CompletionSuggestion.Entry.Option option : suggestion.getOptions()) {
                String text = option.getText().string();
                result.add(text);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getResponseByName(Aggregations aggregations, String aggName) {
        Terms brandTerms = aggregations.get(aggName);
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            String brandName = bucket.getKeyAsString();
            brandList.add(brandName);
        }
        return brandList;
    }

    private static void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(100));
        request.source().aggregation(AggregationBuilders.terms("cityAgg").field("city").size(100));
        request.source().aggregation(AggregationBuilders.terms("starNameAgg").field("starName").size(100));
    }

    private void buildQueryParamFunctionScore(RequestParams requestParams, SearchRequest request) {
        String key = requestParams.getKey();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isEmpty(key)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", requestParams.getKey()));
        }
        if (!StringUtils.isEmpty(requestParams.getCity())) {
            boolQuery.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        if (!StringUtils.isEmpty(requestParams.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }

        if (!StringUtils.isEmpty(requestParams.getStarName())) {
            boolQuery.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }

        if (!StringUtils.isEmpty(requestParams.getMinPrice()) && !StringUtils.isEmpty(requestParams.getMaxPrice())) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(requestParams.getMinPrice()).lte(requestParams.getMaxPrice()));
        }
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(boolQuery, new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isAD", true), ScoreFunctionBuilders.weightFactorFunction(5))});
        request.source().query(functionScoreQueryBuilder);
    }

    private static void buildQueryParam(RequestParams requestParams, SearchRequest request) {
        String key = requestParams.getKey();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isEmpty(key)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", requestParams.getKey()));
        }
        if (!StringUtils.isEmpty(requestParams.getCity())) {
            boolQuery.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        if (!StringUtils.isEmpty(requestParams.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }

        if (!StringUtils.isEmpty(requestParams.getStarName())) {
            boolQuery.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }

        if (!StringUtils.isEmpty(requestParams.getMinPrice()) && !StringUtils.isEmpty(requestParams.getMaxPrice())) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(requestParams.getMinPrice()).lte(requestParams.getMaxPrice()));
        }
        request.source().query(boolQuery);
    }

    private static PageResult handlerResponse(SearchResponse response) {
        SearchHits hits = response.getHits();
        long count = hits.getTotalHits().value;
        List<HotelDoc> list = new ArrayList<>();
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
            Object[] sortValues = hit.getSortValues();
            if (sortValues != null && sortValues.length != 0) {
                hotelDoc.setDistance(sortValues[0]);
            }
            list.add(hotelDoc);
        }
        return new PageResult(count, list);
    }


}
