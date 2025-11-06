package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
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
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询附近的景点
     * @param lat
     * @param lng
     * @return
     */
    @GetMapping("/queryRoadImageByPosition")
    public String queryScenicByPosition(Double lat, Double lng) {
        // 判断是否为合法的经纬度
        if (!(lng > 0 && lng < 180 && lat > 0 && lat < 180)) {
            return null;
        }

        Circle circle = new Circle(lng, lat, 1000d);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().includeCoordinates().limit(1);

        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo().radius(CacheConstants.ROAD_POINT_IMAGE, circle, args);
        if (CollectionUtil.isEmpty(geoResults) || CollectionUtil.isEmpty(geoResults.getContent())) {
            return null;
        }

        RedisGeoCommands.GeoLocation<String> geoLocation = geoResults.getContent().get(0).getContent();
        String base64Image = redisTemplate.opsForValue().get(CacheConstants.ROAD_POINT_IMAGE_FLAG + geoLocation.getName());
        if (StrUtil.isEmpty(base64Image)) {
            return null;
        }
        return "data:image/jpeg;base64," + base64Image;
    }

    /**
     * 分页查询景点订单
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
     * 订单销票
     * @param orderId
     * @return
     */
    @GetMapping("/editStatus")
    public R editStatus(Integer orderId) {
        return R.ok(scenicOrderService.update(Wrappers.<ScenicOrder>lambdaUpdate().set(ScenicOrder::getOrderStatus, 2).eq(ScenicOrder::getId, orderId)));
    }

    /**
     * 添加景区订单
     * @param scenicOrder
     * @return
     */
    @PostMapping
    public R save(ScenicOrder scenicOrder) {
        return R.ok(scenicOrderService.save(scenicOrder));
    }

    /**
     * 修改景区订单
     * @param scenicOrder
     * @return
     */
    @PutMapping
    public R edit(ScenicOrder scenicOrder) {
        return R.ok(scenicOrderService.updateById(scenicOrder));
    }

    /**
     * 删除景区订单
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(scenicOrderService.removeByIds(ids));
    }

}
