package org.winivin;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.QueryParamsMap;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static spark.Spark.*;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    private static final RestClient restClient = RestClient
            .builder(HttpHost.create("http://localhost:9200"))
            .build();
    private static final ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    private static final ElasticsearchClient client = new ElasticsearchClient(transport);

    public static void main(String[] args) {
        port(3000);
        routeMaps();
        init();
    }

    public static void routeMaps() {
        post("/dumpLarge", (request, response) -> {
            int length = request.body().length();
            String contentType = request.contentType();
            String body = request.body();

            logger.info(String.valueOf(length));
            logger.info(contentType);

            try {
                if(!contentType.equals("application/json"))
                    throw new Exception("Invalid Content Type");
                bulkJson(body);
                return "Objects Mapped Successfully";
            } catch (Exception e) {
                logger.error(e.getMessage());
                return "Invalid JSON File/Data";
            }
        });

        post("/dump", (request, response) -> {
            int length = request.contentLength();
            String contentType = request.contentType();
            String body = request.body();

            logger.info(String.valueOf(length));
            logger.info(contentType);

            try {
                if(!contentType.equals("application/json"))
                    throw new Exception("Invalid Content Type");
                singleJson(body);
                return "Objects Mapped Successfully";
            } catch (Exception e) {
                logger.error(e.getMessage());
                return "Invalid JSON File/Data";
            }
        });

        get("/search", (request, response) -> {
            QueryParamsMap queryParamsMap = request.queryMap();
            Set<String> queryKeys = request.queryParams();
            List<Query> queries = getQueryObjects(queryParamsMap, queryKeys);

            int size;
            if(queryKeys.contains("size"))
                size = Integer.parseInt(queryParamsMap.value("size"));
            else {
                size = 10;
            }

            SearchResponse<LogObj> search = client.search(s -> s
                            .index("log").size(size)
                            .query(q -> q.bool(b -> b
                                    .must(queries))),
                    LogObj.class);

            return getParentDiv(search);
        });
    }

    private static String getParentDiv(SearchResponse<LogObj> search) {
        List<Hit<LogObj>> hits = search.hits().hits();
        String parentDiv = "<div>";
        for(Hit<LogObj> x : hits) {
            String temp = "<div>";
            temp += "<div>" + x.source().getLevel() +"</div>";
            temp += "<div>" + x.source().getMessage() +"</div>";
            temp += "<div>" + x.source().getTimestamp() +"</div>";
            temp += "<div>" + x.source().getCommit() +"</div>";
            temp += "</div>";
            parentDiv += temp;
            parentDiv += "<hr></hr>";
        }
        parentDiv += "</div>";
        return parentDiv;
    }

    public static void singleJson(String jsonString) throws Exception{
        JSONObject object = new JSONObject(jsonString);
        if (checkValidObject(object)) {
            pushToElastic(object);
        }
    }

    public static void bulkJson(String jsonString) throws Exception{
        JSONArray array = new JSONArray(jsonString);
        BulkRequest.Builder br = new BulkRequest.Builder();
        for(int i=0; i < array.length(); i++)
        {
            JSONObject object = array.getJSONObject(i);
            if( checkValidObject(object) ) {
                pushToElastic(object);

                Metadata metadata = new Metadata(object.getJSONObject("metadata").getString("parentResourceId"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.format).withZone(ZoneId.of("UTC"));
                LocalDateTime date = LocalDateTime.parse(object.getString("timestamp"), formatter);
                LogObj log_entity = new LogObj(object.getString("level"), object.getString("message"),
                        object.getString("resourceId"), date,
                        object.getString("traceId"), object.getString("spanId"),
                        object.getString("commit"), metadata);

                br.operations(op -> op.index(idx -> idx.index("log").document(log_entity)));
            }
        }

        BulkResponse result = client.bulk(br.build());
        if (result.errors()) {
            logger.error("Bulk had errors");
            for (BulkResponseItem item: result.items()) {
                if (item.error() != null) {
                    logger.error(item.error().reason());
                }
            }
        }
    }

    public static boolean checkValidObject(JSONObject object) {
        boolean level = object.has("level");
        boolean message = object.has("message");
        boolean resourceId = object.has("resourceId");
        boolean timestamp = object.has("timestamp");
        boolean traceId = object.has("traceId");
        boolean spanId = object.has("spanId");
        boolean commit = object.has("commit");
        boolean metadata = object.has("metadata");
        boolean parentResourceId = object.getJSONObject("metadata").has("parentResourceId");

        if (level && message && resourceId && timestamp && traceId && spanId && commit && metadata &&
                parentResourceId) {
            return true;
        }
        return false;
    }

    public static void pushToElastic(JSONObject object) throws Exception{
        Metadata metadata = new Metadata(object.getJSONObject("metadata").getString("parentResourceId"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.format).withZone(ZoneId.of("UTC"));
        LocalDateTime date = LocalDateTime.parse(object.getString("timestamp"), formatter);
        LogObj log_entity = new LogObj(object.getString("level"), object.getString("message"),
                object.getString("resourceId"), date,
                object.getString("traceId"), object.getString("spanId"),
                object.getString("commit"), metadata);
        try {
            IndexResponse response = client.index(j ->
                    j.index("log").document(log_entity));
            logger.info("Indexed with version: {}", response.version());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception from elastic : " + e.getMessage());
        }
    }

    public static Query getLevel(String value) {
        return MatchQuery.of(m -> m
                .field("level")
                .query(value))._toQuery();
    }

    public static Query getCommit(String value) {
        return MatchQuery.of(m -> m
                .field("commit")
                .query(value))._toQuery();
    }

    public static Query getTimeStamp(String value) {
        return MatchQuery.of(m -> m
                .field("timestamp")
                .query(value))._toQuery();
    }

    public static Query getResourceId(String value) {
        return MatchQuery.of(m -> m
                .field("resourceId")
                .query(value))._toQuery();
    }

    public static Query getTraceId(String value) {
        return MatchQuery.of(m -> m
                .field("traceId")
                .query(value))._toQuery();
    }

    public static Query getMessage(String value) {
        return Query.of(query -> query.term(term -> term.field("message")
                .value(v -> v.stringValue(value))));
    }

    public static Query getSpanId(String value) {
        return MatchQuery.of(m -> m
                .field("spanId")
                .query(value))._toQuery();
    }

    public static Query getParentResourceId(String value) {
        return MatchQuery.of(m -> m
                .field("parentResourceId")
                .query(value))._toQuery();
    }

    public static Query getRange(String value1, String value2) {
        RangeQuery rangeQuery = RangeQuery.of(x -> x.field("timestamp").from(value1).to(value2));
        return Query.of(x -> x.range(rangeQuery)).range()._toQuery();
    }

    public static List<Query> getQueryObjects(QueryParamsMap queryParamsMap, Set<String> keys, String ...value) {
        List<Query> queries = new ArrayList<>();
        for(String key : keys) {
            if (key.equals("level")) {
                queries.add(getLevel(queryParamsMap.value("level")));
            } else if (key.equals("commit")) {
                queries.add(getCommit(queryParamsMap.value("commit")));
            } else if (key.equals("spanId")) {
                queries.add(getSpanId(queryParamsMap.value("spanId")));
            } else if (key.equals("resourceId")) {
                queries.add(getResourceId(queryParamsMap.value("resourceId")));
            } else if (key.equals("parentResourceId")) {
                queries.add(getParentResourceId(queryParamsMap.value("parentResourceId")));
            } else if (key.equals("message")) {
                queries.add(getMessage(queryParamsMap.value("message")));
            } else if (key.equals("timestamp")) {
                queries.add(getTimeStamp(queryParamsMap.value("timestamp")));
            } else if (key.equals("traceId")) {
                queries.add(getTraceId(queryParamsMap.value("traceId")));
            } else if (key.equals("range")) {
                queries.add(getRange(queryParamsMap.value("from"), queryParamsMap.value("to")));
            }
        }
        return queries;
    }
}