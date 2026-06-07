package com.mallcloud.mallsearch.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallsearch.api.dto.ProductSearchDTO;
import com.mallcloud.mallsearch.api.vo.AggregationItemVO;
import com.mallcloud.mallsearch.api.vo.ProductSearchItemVO;
import com.mallcloud.mallsearch.api.vo.ProductSearchVO;
import com.mallcloud.mallsearch.client.ProductClient;
import com.mallcloud.mallsearch.client.dto.ProductDetailDTO;
import com.mallcloud.mallsearch.client.dto.SkuDTO;
import com.mallcloud.mallsearch.domain.ProductDocument;
import com.mallcloud.mallsearch.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索服务实现
 *
 * @author lisi
 * @since 2026-03-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int STATUS_ON_SALE = 1;

    private final ElasticsearchClient elasticsearchClient;
    private final ProductClient productClient;

    @Value("${mallcloud.search.index.product:mall_product}")
    private String productIndex;

    @Override
    public ProductSearchVO searchProducts(ProductSearchDTO dto) {
        int pageNum = normalizePageNum(dto.getPageNum());
        int pageSize = normalizePageSize(dto.getPageSize());
        try {
            SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> {
                s.index(productIndex)
                        .from((pageNum - 1) * pageSize)
                        .size(pageSize)
                        .query(buildQuery(dto))
                        .highlight(h -> h.fields("name", f -> f.preTags("<em>").postTags("</em>")));
                applySort(s, dto.getSort());
                return s;
            }, ProductDocument.class);
            return buildResult(response);
        } catch (IOException e) {
            log.error("商品搜索失败 keyword={}", dto.getKeyword(), e);
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "商品搜索失败");
        }
    }

    @Override
    public List<String> getHotWords() {
        return List.of("手机", "iPhone", "笔记本", "耳机", "秒杀");
    }

    @Override
    public void syncProduct(Long spuId, Integer status) {
        if (spuId == null) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "商品 ID 不能为空");
        }
        try {
            if (status != null && status != STATUS_ON_SALE) {
                deleteDocument(spuId);
                return;
            }
            Result<ProductDetailDTO> result = productClient.getProduct(spuId);
            if (!result.isSuccess() || result.getData() == null) {
                deleteDocument(spuId);
                return;
            }
            ProductDocument document = toDocument(result.getData(), status);
            if (document.getStatus() != null && document.getStatus() != STATUS_ON_SALE) {
                deleteDocument(spuId);
                return;
            }
            elasticsearchClient.index(IndexRequest.of(i -> i
                    .index(productIndex)
                    .id(String.valueOf(spuId))
                    .document(document)));
            log.info("同步商品到 ES spuId={}", spuId);
        } catch (IOException e) {
            log.error("同步商品到 ES 失败 spuId={}", spuId, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "同步商品搜索索引失败");
        }
    }

    private Query buildQuery(ProductSearchDTO dto) {
        BoolQuery.Builder bool = new BoolQuery.Builder();
        bool.filter(f -> f.term(t -> t.field("status").value(STATUS_ON_SALE)));
        if (StringUtils.hasText(dto.getKeyword())) {
            bool.must(m -> m.multiMatch(mm -> mm
                    .query(dto.getKeyword())
                    .fields("name^3", "description", "brand")));
        } else {
            bool.must(m -> m.matchAll(ma -> ma));
        }
        if (dto.getCategoryId() != null) {
            bool.filter(f -> f.term(t -> t.field("categoryId").value(FieldValue.of(dto.getCategoryId()))));
        }
        if (dto.getMinPrice() != null || dto.getMaxPrice() != null) {
            bool.filter(f -> f.range(r -> {
                r.field("price");
                if (dto.getMinPrice() != null) {
                    r.gte(JsonData.of(dto.getMinPrice()));
                }
                if (dto.getMaxPrice() != null) {
                    r.lte(JsonData.of(dto.getMaxPrice()));
                }
                return r;
            }));
        }
        return Query.of(q -> q.bool(bool.build()));
    }

    private void applySort(co.elastic.clients.elasticsearch.core.SearchRequest.Builder builder, String sort) {
        if (!StringUtils.hasText(sort) || "_score".equals(sort)) {
            builder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
            return;
        }
        String[] parts = sort.split(",");
        String field = parts[0];
        SortOrder order = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? SortOrder.Asc : SortOrder.Desc;
        if (!"price".equals(field) && !"sales".equals(field)) {
            builder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
            return;
        }
        builder.sort(s -> s.field(f -> f.field(field).order(order)));
    }

    private ProductSearchVO buildResult(SearchResponse<ProductDocument> response) {
        List<ProductSearchItemVO> items = new ArrayList<>();
        Map<Long, Long> categoryCounts = new HashMap<>();
        Map<String, Long> priceRangeCounts = new HashMap<>();
        for (Hit<ProductDocument> hit : response.hits().hits()) {
            ProductDocument document = hit.source();
            if (document == null) {
                continue;
            }
            ProductSearchItemVO item = new ProductSearchItemVO();
            item.setSpuId(document.getSpuId());
            item.setName(document.getName());
            item.setHighlightName(hit.highlight().getOrDefault("name", List.of(document.getName())).get(0));
            item.setPrice(document.getPrice());
            item.setSales(document.getSales());
            item.setMainImage(document.getMainImage());
            items.add(item);

            if (document.getCategoryId() != null) {
                categoryCounts.merge(document.getCategoryId(), 1L, Long::sum);
            }
            priceRangeCounts.merge(priceRange(document.getPrice()), 1L, Long::sum);
        }

        ProductSearchVO vo = new ProductSearchVO();
        vo.setList(items);
        vo.setTotal(response.hits().total() == null ? (long) items.size() : response.hits().total().value());
        Map<String, List<AggregationItemVO>> aggregations = new HashMap<>();
        aggregations.put("categories", categoryCounts.entrySet().stream()
                .map(e -> new AggregationItemVO(null, e.getKey(), String.valueOf(e.getKey()), e.getValue()))
                .toList());
        aggregations.put("priceRanges", priceRangeCounts.entrySet().stream()
                .map(e -> new AggregationItemVO(e.getKey(), null, null, e.getValue()))
                .toList());
        vo.setAggregations(aggregations);
        return vo;
    }

    private ProductDocument toDocument(ProductDetailDTO detail, Integer status) {
        ProductDocument document = new ProductDocument();
        document.setSpuId(detail.getSpuId());
        document.setName(detail.getName());
        document.setDescription(detail.getDescription());
        document.setMainImage(detail.getMainImage());
        document.setCategoryId(detail.getCategoryId());
        document.setBrand(detail.getBrand());
        document.setSales(detail.getSales() == null ? 0 : detail.getSales());
        document.setStatus(status == null ? detail.getStatus() : status);
        document.setPrice(minPrice(detail.getSkus()));
        return document;
    }

    private BigDecimal minPrice(List<SkuDTO> skus) {
        if (skus == null || skus.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return skus.stream()
                .map(SkuDTO::getPrice)
                .filter(p -> p != null)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private String priceRange(BigDecimal price) {
        if (price == null || price.compareTo(new BigDecimal("1000")) < 0) {
            return "0-1000";
        }
        if (price.compareTo(new BigDecimal("5000")) < 0) {
            return "1000-5000";
        }
        if (price.compareTo(new BigDecimal("10000")) < 0) {
            return "5000-10000";
        }
        return "10000+";
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private void deleteDocument(Long spuId) throws IOException {
        elasticsearchClient.delete(DeleteRequest.of(d -> d.index(productIndex).id(String.valueOf(spuId))));
        log.info("从 ES 删除商品 spuId={}", spuId);
    }
}
