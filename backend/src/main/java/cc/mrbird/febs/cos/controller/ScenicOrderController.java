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

}
