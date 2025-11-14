package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.ScenicOrder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
public interface IScenicOrderService extends IService<ScenicOrder> {

    /**
     * 分页查询景点订单
     *
     * @param page
     * @param scenicOrder
     * @return
     */
    IPage<LinkedHashMap<String, Object>> scenicInfoByPage(Page page, ScenicOrder scenicOrder);

    /**
     * 查询本月景点流量排行榜
     *
     * @param date 统计日期
     * @return 结果
     */
    List<LinkedHashMap<String, Object>> queryScenicTop(String date);

    /**
     * 年统计订单及收益
     *
     * @param date 年份
     * @return 结果
     */
    LinkedHashMap<String, Object> selectStoreStatisticsByYear(String date);

    /**
     * 月统计订单及收益
     *
     * @param date 日期
     * @return 结果
     */
    LinkedHashMap<String, Object> selectStoreStatisticsByMonth(String date);
}
