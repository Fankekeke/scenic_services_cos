package cc.mrbird.febs.cos.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ScenicSales {
    private Integer scenicId;           // 景点ID
    private String scenicName;          // 景点名称
    private List<DailySale> dailySales; // 每日销售数据
    private Double totalSales;        // 总销售量
    private Date startDate;           // 数据开始日期
    private Date endDate;             // 数据结束日期
}
