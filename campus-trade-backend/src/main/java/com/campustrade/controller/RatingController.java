package com.campustrade.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campustrade.common.Result;
import com.campustrade.entity.Rating;
import com.campustrade.mapper.RatingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/rating")
public class RatingController {
    @Autowired private RatingMapper ratingMapper;

    @PostMapping
    public Result<?> create(Authentication auth, @RequestBody Rating rating) {
        Long userId = (Long) auth.getPrincipal();
        rating.setId(null);
        rating.setUserId(userId);
        Rating exist = ratingMapper.selectOne(new LambdaQueryWrapper<Rating>().eq(Rating::getUserId, userId).eq(Rating::getTransactionId, rating.getTransactionId()));
        if (exist != null) return Result.error(400, "已评价过此交易");
        ratingMapper.insert(rating);
        return Result.success();
    }

    @GetMapping("/seller/{sellerId}")
    public Result<Map<String, Object>> sellerStats(@PathVariable Long sellerId) {
        List<Rating> list = ratingMapper.selectList(new LambdaQueryWrapper<Rating>().eq(Rating::getSellerId, sellerId));
        Map<String, Object> data = new HashMap<>();
        data.put("count", list.size());
        double avg = list.stream().mapToInt(Rating::getScore).average().orElse(0);
        data.put("avgScore", Math.round(avg * 10) / 10.0);
        // Per-product ratings
        Map<Long, List<Rating>> byProduct = list.stream().collect(java.util.stream.Collectors.groupingBy(Rating::getTransactionId));
        List<Map<String, Object>> detailRatings = new ArrayList<>();
        for (Map.Entry<Long, List<Rating>> entry : byProduct.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("transactionId", entry.getKey());
            double prodAvg = entry.getValue().stream().mapToInt(Rating::getScore).average().orElse(0);
            item.put("score", Math.round(prodAvg * 10) / 10.0);
            item.put("count", entry.getValue().size());
            detailRatings.add(item);
        }
        data.put("detailRatings", detailRatings);
        return Result.success(data);
    }
}