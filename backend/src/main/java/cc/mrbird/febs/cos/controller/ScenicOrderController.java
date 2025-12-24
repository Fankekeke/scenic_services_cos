package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.ScenicInfo;
import cc.mrbird.febs.cos.entity.ScenicOrder;
import cc.mrbird.febs.cos.entity.UserInfo;
import cc.mrbird.febs.cos.service.IScenicInfoService;
import cc.mrbird.febs.cos.service.IScenicOrderService;
import cc.mrbird.febs.cos.service.IUserInfoService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/scenic-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScenicOrderController {

    private final IScenicOrderService scenicOrderService;

    private final IUserInfoService userInfoService;

    private final IScenicInfoService scenicInfoService;

    /**
     * 分页查询景点订单
     *
     * @param page
     * @param scenicOrder
     * @return
     */
    @GetMapping("/page")
    public R page(Page page, ScenicOrder scenicOrder) {
        if (scenicOrder.getUserId() != null) {
            UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, scenicOrder.getUserId()));
            scenicOrder.setUserId(userInfo.getId());
        }
        return R.ok(scenicOrderService.scenicInfoByPage(page, scenicOrder));
    }

    /**
     * 查询本月景点流量排行榜
     *
     * @param date 统计日期
     * @return 结果
     */
    @GetMapping("/queryScenicTop")
    public R queryScenicTop(@RequestParam String date) {
        return R.ok(scenicOrderService.queryScenicTop(date));
    }

    /**
     * 获取景点订单详情
     *
     * @param orderCode 订单ID
     * @return 详情
     */
    @GetMapping("/queryScenicOrderDetail")
    public R queryScenicOrderDetail(String orderCode) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>() {
            {
                put("data", scenicOrderService.queryScenicOrderDetailByCode(orderCode));
            }
        };
        return R.ok(result);
    }

    /**
     * 核销订单
     *
     * @param orderCode 订单编号
     * @return 结果
     */
    @GetMapping("/verifyOrder")
    public R verifyOrder(String orderCode) {
        return R.ok(scenicOrderService.update(Wrappers.<ScenicOrder>lambdaUpdate().set(ScenicOrder::getOrderStatus, "2").eq(ScenicOrder::getCode, orderCode)));
    }

    /**
     * 查询景点列表占比
     *
     * @return
     */
    @GetMapping("/queryScenicListRate")
    public R queryScenicListRate() {
        List<ScenicInfo> scenicInfoList = scenicInfoService.list();
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (ScenicInfo scenicInfo : scenicInfoList) {
            String numberStr = scenicInfo.getHot().replaceAll("[^\\d.]", ""); // "0.82"
            double hotValue = Double.parseDouble(numberStr); // 0.82
            result.add(new LinkedHashMap<String, Object>() {
                {
                    put("name", scenicInfo.getScenicName());
                    put("point", scenicInfo.getPoint());
                    put("hot", hotValue);
                }
            });
        }
        return R.ok(result);
    }

    /**
     * 订单销票
     *
     * @param orderId
     * @return
     */
    @GetMapping("/editStatus")
    public R editStatus(Integer orderId) {
        return R.ok(scenicOrderService.update(Wrappers.<ScenicOrder>lambdaUpdate().set(ScenicOrder::getOrderStatus, 2).eq(ScenicOrder::getId, orderId)));
    }

    /**
     * 添加景区订单
     *
     * @param scenicOrder
     * @return
     */
    @PostMapping
    public R save(ScenicOrder scenicOrder) {
        return R.ok(scenicOrderService.save(scenicOrder));
    }

    /**
     * 修改景区订单
     *
     * @param scenicOrder
     * @return
     */
    @PutMapping
    public R edit(ScenicOrder scenicOrder) {
        return R.ok(scenicOrderService.updateById(scenicOrder));
    }

    /**
     * 删除景区订单
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(scenicOrderService.removeByIds(ids));
    }

    /**
     * 年统计订单及收益
     *
     * @param date 年份
     * @return 结果
     */
    @GetMapping("/statistics/year")
    public R selectStoreStatisticsByYear(String date) {
        return R.ok(scenicOrderService.selectStoreStatisticsByYear(date));
    }

    /**
     * 月统计订单及收益
     *
     * @param date 日期
     * @return 结果
     */
    @GetMapping("/statistics/month")
    public R selectStoreStatisticsByMonth(String date) {
        return R.ok(scenicOrderService.selectStoreStatisticsByMonth(date));
    }

    /**
     * 统计评价占比
     *
     * @return 评价占比
     */
    @GetMapping("/queryEvaluateRate")
    public R queryEvaluateRate() {
        return R.ok(scenicOrderService.queryEvaluateRate());
    }

    /**
     * 区域景点数量占比
     *
     * @return 区域景点数量占比
     */
    @GetMapping("/queryAreaScenicNumRate")
    public R queryAreaScenicNumRate() {
        return R.ok(scenicOrderService.queryAreaScenicNumRate());
    }

    /**
     * 景点等级占比
     *
     * @return 景点等级占比
     */
    @GetMapping("/queryScenicLevelRate")
    public R queryScenicLevelRate() {
        return R.ok(scenicOrderService.queryScenicLevelRate());
    }

    /**
     * 价格分布占比
     *
     * @return 价格分布占比
     */
    @GetMapping("/queryPriceStepRate")
    public R queryPriceStepRate() {
        return R.ok(scenicOrderService.queryPriceStepRate());
    }

    /**
     * 订单词云
     *
     * @return 订单词云
     */
    @GetMapping("/queryOrderWordCloud")
    public R queryOrderWordCloud() {
        return R.ok(scenicOrderService.queryOrderWordCloud());
    }
}
