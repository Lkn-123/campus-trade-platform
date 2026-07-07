package com.campustrade.service.impl;
import com.campustrade.service.AiService;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class AiServiceImpl implements AiService {
    @Override
    public Map<String,Object> generateTitle(String description, String category) {
        String title;
        if (description != null && description.length() > 5) {
            title = description.substring(0, Math.min(15, description.length()));
            if (category != null) title = category + title;
        } else {
            title = (category != null ? category : "") + "二手商品";
        }
        // In real impl, call LLM API here
        Map<String,Object> result = new HashMap<>();
        result.put("title", title + "-校园二手交易");
        result.put("note", "此标题由AI自动生成，可手动修改");
        return result;
    }
    @Override
    public Map<String,Object> optimizeDescription(String description, String conditionAlias) {
        StringBuilder sb = new StringBuilder();
        if (description != null) sb.append(description);
        if (conditionAlias != null) {
            sb.append("\n\n商品成色：").append(conditionAlias);
        }
        sb.append("\n欢迎学校同学们咨询，支持自提和校内交易。");
        // In real impl, call LLM API
        Map<String,Object> result = new HashMap<>();
        result.put("description", sb.toString());
        result.put("note", "此描述由AI优化生成，可手动修改");
        return result;
    }
    
    private double getConditionRate(String condition) {
        if (condition == null) return 0.7;
        return switch (condition) {
            case "全新" -> 1.0;
            case "几乎全新" -> 0.85;
            case "轻微使用痕迹" -> 0.7;
            case "明显使用痕迹" -> 0.5;
            default -> 0.3;
        };
    }
    @Override
    public Map<String,Object> priceSuggestion(String category, String condition, Double originalPrice) {
        if (originalPrice == null || originalPrice <= 0) {
            Map<String,Object> result = new HashMap<>();
            result.put("suggestedPrice", 0);
            result.put("priceRange", "请填写原价");
            result.put("condition", condition);
            result.put("referenceNote", "请先输入您购买时的价格，以便AI进行估价");
            return result;
        }

        double rate = getConditionRate(condition);

        // 按品类调整折旧系数
        double categoryFactor = 1.0;
        if (category != null) {
            if (category.contains("电子") || category.contains("数码")) {
                categoryFactor = 0.9;  // 电子产品贬值快
            } else if (category.contains("教材") || category.contains("书籍")) {
                categoryFactor = 0.7;  // 教材贬值很快
            } else if (category.contains("服饰") || category.contains("鞋包")) {
                categoryFactor = 1.0;  // 服饰看成色
            } else if (category.contains("体育") || category.contains("运动")) {
                categoryFactor = 0.85;
            } else if (category.contains("生活")) {
                categoryFactor = 0.8;
            }
        }

        // 价格越高，贬值越明显（非线性）
        double priceFactor = 1.0;
        if (originalPrice > 5000) {
            priceFactor = 0.85;
        } else if (originalPrice > 2000) {
            priceFactor = 0.9;
        } else if (originalPrice > 500) {
            priceFactor = 0.95;
        }

        double suggestedPrice = originalPrice * rate * categoryFactor * priceFactor;
        double minPrice = suggestedPrice * 0.85;
        double maxPrice = suggestedPrice * 1.15;

        // 保底价：不低于原价的 10%
        double floor = originalPrice * 0.1;
        if (suggestedPrice < floor) {
            suggestedPrice = floor;
            minPrice = floor * 0.9;
            maxPrice = floor * 1.2;
        }

        Map<String,Object> result = new HashMap<>();
        result.put("suggestedPrice", Math.round(suggestedPrice * 100) / 100.0);
        result.put("priceRange", String.format("%.2f - %.2f", minPrice, maxPrice));
        result.put("condition", condition);
        result.put("referenceNote", String.format(
            "基于原价￥%.0f、成色(%s)、品类(%s)综合计算，仅供参考",
            originalPrice, condition, category != null ? category : "未指定"
        ));
        return result;
    }
    @Override
    public Map<String,Object> customerService(String question) {
        Map<String,Object> result = new HashMap<>();
        result.put("question", question);
        String answer;
        String q = question != null ? question.toLowerCase() : "";
        if (q.contains("交易") || q.contains("怎么买") || q.contains("怎么卖")) {
            answer = "交易流程：\n1. 浏览商品列表，找到感兴趣的商品\n2. 点击商品进入详情页，查看详细信息\n3. 点击“立即购买”创建交易\n4. 与卖家沟通确认交易方式\n5. 完成交易后更新交易状态";
        } else if (q.contains("举报") || q.contains("投诉")) {
            answer = "举报流程：\n1. 在商品详情页点击“举报”按钮\n2. 选择举报原因并提交\n3. 管理员将在工作日内处理\n4. 处理结果将通过系统通知您";
        } else if (q.contains("退款") || q.contains("退货")) {
            answer = "退款政策：\n校园二手交易平台提供买卖双方自行协商退款。建议交易前充分沟通，确认商品状态。如遇到争议，可通过举报功能请求平台帮助。";
        } else if (q.contains("平台") || q.contains("介绍")) {
            answer = "校园二手交易平台是专为校园学生设计的二手商品交易平台。功能包括：商品发布、搜索、收藏、留言、交易管理等。支持AI智能客服、价格建议、风险提示等功能。";
        } else {
            answer = "您好！欢迎使用校园二手交易平台AI小助手。我可以回答以下问题：\n- 交易流程说明\n- 举报流程\n- 退款政策\n- 平台介绍\n请问有什么可以帮助您？";
        }
        result.put("answer", answer);
        return result;
    }
    @Override
    public Map<String,Object> riskCheck(String title, String description, Double price) {
        List<String> warnings = new ArrayList<>();
        int riskLevel = 0;
        String text = (title != null ? title : "") + " " + (description != null ? description : "");
        String[] suspiciousKeywords = {"假货", "代购", "发票", "全新未拆", "正品代购", "支付宝转账", "浇米"};
        for (String kw : suspiciousKeywords) {
            if (text.contains(kw)) {
                warnings.add("检测到可疑关键词“" + kw + "”，请警惕此类描述");
                riskLevel++;
            }
        }
        if (price != null && price < 5) {
            warnings.add("价格异常低廉，请注意有欺诈风险");
            riskLevel++;
        }
        if (price != null && price > 10000) {
            warnings.add("价格较高，建议确认商品真实性");
            riskLevel++;
        }
        if (text.length() < 10) {
            warnings.add("商品描述过短，建议补充详细信息");
        }
        Map<String,Object> result = new HashMap<>();
        String level = riskLevel == 0 ? "低风险" : (riskLevel <= 2 ? "中等风险" : "高风险");
        result.put("riskLevel", level);
        result.put("warnings", warnings);
        result.put("suggestion", riskLevel > 0 ? "建议谨慎处理此交易" : "未检测到明显风险");
        return result;
    }
}
