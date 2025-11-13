package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.PredictionResult;

// 创建预测服务接口
public interface IPredictionService {
    /**
     * 预测药品销售量
     * @param drugId 药品ID
     * @param days 预测天数
     * @return 预测结果
     */
    PredictionResult predictSales(Integer drugId, Integer days);

}