package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.ScenicInfo;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
