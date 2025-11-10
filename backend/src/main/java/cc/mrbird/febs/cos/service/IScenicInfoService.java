package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.HotelInfo;
import cc.mrbird.febs.cos.entity.ScenicInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
public interface IScenicInfoService extends IService<ScenicInfo> {

    /**
     * 推荐景点
     *
     * @param lat
     * @param lng
     * @param userId 用户ID
     * @return
     */
    List<ScenicInfo> queryScenicRecommend(Double lat, Double lng, Integer userId);

    /**
     * 推荐酒店
     *
     * @param lat  纬度
     * @param lng  经度
     * @param type 类型  1.附近一公里类 2.附近三公里内 3.附近五公里内 4.评分4.0以上
     * @return 结果
     */
    List<LinkedHashMap<String, Object>> queryHotelByPosition(Double lat, Double lng, Integer type);
}
