package cc.mrbird.febs.cos.entity;

import lombok.Data;

import java.util.Date;

@Data
public class OrderItem {

    private Integer quantity;  // 销售数量
    private Date date;  // 销售日期
}
