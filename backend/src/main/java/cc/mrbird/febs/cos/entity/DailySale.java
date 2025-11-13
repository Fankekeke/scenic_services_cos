package cc.mrbird.febs.cos.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DailySale {
    private Date saleDate;    // 销售日期
    private Integer quantity; // 销售数量
    private Double amount;    // 销售金额

    private Integer scenicId;

}