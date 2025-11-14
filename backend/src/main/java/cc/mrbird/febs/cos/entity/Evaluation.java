package cc.mrbird.febs.cos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户评价
 *
 * @author Fank
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 评分
     */
    private Double score;

    /**
     * 评价
     */
    private String content;

    /**
     * 评价时间
     */
    private String createDate;

    /**
     * 所属订单
     */
    private Integer orderId;

    /**
     * 景点ID
     */
    private Integer scenicId;

    /**
     * 酒店ID
     */
    private Integer hotelId;

    /**
     * 评价类型 (1.酒店 2.景点)
     */
    private Integer type;

    private String images;

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String hotelName;
    @TableField(exist = false)
    private String scenicName;
    @TableField(exist = false)
    private String roomName;
    @TableField(exist = false)
    private String code;

}
