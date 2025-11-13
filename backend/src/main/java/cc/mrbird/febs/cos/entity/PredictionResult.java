package cc.mrbird.febs.cos.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class PredictionResult {
    private List<Double> predictedValues;  // 预测值列表
    private List<String> dates;            // 对应日期
    private Double confidence;             // 预测置信度
    private String modelVersion;           // 模型版本
    private Date createTime;               // 创建时间
    private Map<String, List<Double>> drugPredictions;

}
