package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.dao.ScenicInfoMapper;
import cc.mrbird.febs.cos.entity.RoomType;
import cc.mrbird.febs.cos.dao.RoomTypeMapper;
import cc.mrbird.febs.cos.entity.ScenicInfo;
import cc.mrbird.febs.cos.service.IRoomTypeService;
import cc.mrbird.febs.cos.service.IScenicInfoService;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
@Service
public class RoomTypeServiceImpl extends ServiceImpl<RoomTypeMapper, RoomType> implements IRoomTypeService {

    @Resource
    private ScenicInfoMapper scenicInfoMapper;

    @Override
    public IPage<LinkedHashMap<String, Object>> roomTypeByPage(Page page, RoomType roomType) {
        return baseMapper.roomTypeByPage(page, roomType);
    }

    /**
     * 查询房间类型
     *
     * @param scenicId
     * @return
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryRoomTypeByScenic(Integer scenicId) {
        ScenicInfo scenicInfo = scenicInfoMapper.selectById(scenicId);

        List<LinkedHashMap<String, Object>> list = baseMapper.queryList();
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<LinkedHashMap<String, Object>> resultList = CollectionUtil.newArrayList();
        for (LinkedHashMap<String, Object> stringObjectLinkedHashMap : list) {
            if (stringObjectLinkedHashMap.get("point") != null) {
                String hotelPoint = (String) stringObjectLinkedHashMap.get("point");

                double latitude = Double.parseDouble(scenicInfo.getPoint().split(",")[1]);
                double longitude = Double.parseDouble(scenicInfo.getPoint().split(",")[0]);

                double latitude1 = Double.parseDouble(hotelPoint.split(",")[1]);
                double longitude1 = Double.parseDouble(hotelPoint.split(",")[0]);
                double distance = calculateDistance(latitude1, longitude1, latitude, longitude);

                if (distance <= 20) {
                    resultList.add(stringObjectLinkedHashMap);
                }
            }
        }
        return resultList;
    }

    @Override
    public Integer roomNum(Integer roomTypeId) {
        return baseMapper.roomNum(roomTypeId);
    }

    /**
     * 计算两个地点之间的距离（使用Haversine公式）
     *
     * @param lat1 第一个点的纬度
     * @param lng1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lng2 第二个点的经度
     * @return 距离（单位：公里）
     */
    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return Double.MAX_VALUE;
        }

        final int EARTH_RADIUS = 6371; // 地球半径（单位：公里）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
