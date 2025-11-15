package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.ScenicInfo;
import cc.mrbird.febs.cos.service.IScenicInfoService;
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

import java.util.List;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/scenic-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScenicInfoController {

    private final IScenicInfoService scenicInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询景点信息
     *
     * @param key 关键字
     * @return 结果
     */
    @GetMapping("/queryScenicInfoByKey")
    public R queryScenicInfoByKey(@RequestParam(value = "key", required = false, defaultValue = "拉卜楞寺") String key) {
        List<ScenicInfo> scenicInfos = scenicInfoService.list(Wrappers.<ScenicInfo>lambdaQuery()
            .like(ScenicInfo::getScenicName, key)
            .last("LIMIT 10"));
        return R.ok(scenicInfos);
    }

    /**
     * 查询附近的景点
     *
     * @param lat
     * @param lng
     * @return
     */
    @GetMapping("/queryScenicByPosition")
    public String queryScenicByPosition(Double lat, Double lng) {
        // 判断是否为合法的经纬度
        if (!(lng > 0 && lng < 180 && lat > 0 && lat < 180)) {
            return null;
        }

        Circle circle = new Circle(lng, lat, 1000d);
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().includeCoordinates().limit(1);

        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo().radius("geo:scenic", circle, args);
        if (CollectionUtil.isEmpty(geoResults) || CollectionUtil.isEmpty(geoResults.getContent())) {
            return null;
        }
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoLocationGeoResult : geoResults.getContent()) {
            System.out.println(geoLocationGeoResult.getContent());
        }
//        RedisGeoCommands.GeoLocation<String> geoLocation = geoResults.getContent().get(0).getContent();
//        String base64Image = redisTemplate.opsForValue().get(CacheConstants.ROAD_POINT_IMAGE_FLAG + geoLocation.getName());
//        if (StrUtil.isEmpty(base64Image)) {
//            return null;
//        }
        return null;
    }

    /**
     * 推荐景点
     *
     * @param lat    纬度
     * @param lng    经度
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/queryScenicRecommend")
    public R queryScenicRecommend(Double lat, Double lng, Integer userId) {
        return R.ok(scenicInfoService.queryScenicRecommend(lat, lng, userId));
    }

    /**
     * 推荐酒店
     *
     * @param lat  纬度
     * @param lng  经度
     * @param type 类型  1.附近一公里类 2.附近三公里内 3.附近五公里内 4.评分4.0以上
     * @return 结果
     */
    @GetMapping("/queryHotelByPosition")
    public R queryHotelByPosition(Double lat, Double lng, Integer type) {
        return R.ok(scenicInfoService.queryHotelByPosition(lat, lng, type));
    }

    /**
     * 分页查询景点信息
     *
     * @param page
     * @param scenicInfo
     * @return
     */
    @GetMapping("/page")
    public R page(Page page, ScenicInfo scenicInfo) {
        return R.ok(scenicInfoService.page(page, Wrappers.<ScenicInfo>lambdaQuery()
                .like(!StrUtil.isBlank(scenicInfo.getScenicName()), ScenicInfo::getScenicName, scenicInfo.getScenicName())
                .like(!StrUtil.isBlank(scenicInfo.getAddress()), ScenicInfo::getAddress, scenicInfo.getAddress()).orderByDesc(ScenicInfo::getHot)));
    }

    /**
     * 添加景点信息
     *
     * @param scenicInfo
     * @return
     */
    @PostMapping
    public R save(ScenicInfo scenicInfo) {
        return R.ok(scenicInfoService.save(scenicInfo));
    }

    /**
     * 修改景点信息
     *
     * @param scenicInfo
     * @return
     */
    @PutMapping
    public R edit(ScenicInfo scenicInfo) {
        return R.ok(scenicInfoService.updateById(scenicInfo));
    }

    /**
     * 删除景点信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(scenicInfoService.removeByIds(ids));
    }
}
