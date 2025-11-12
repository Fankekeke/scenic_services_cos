package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.dao.OrderInfoMapper;
import cc.mrbird.febs.cos.entity.*;
import cc.mrbird.febs.cos.dao.ScenicInfoMapper;
import cc.mrbird.febs.cos.service.*;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScenicInfoServiceImpl extends ServiceImpl<ScenicInfoMapper, ScenicInfo> implements IScenicInfoService {

    private final IEvaluationService evaluationService;

    private final IUserInfoService userInfoService;

    private final IHotelInfoService hotelInfoService;

    private final IRoomTypeService roomTypeService;

    @Lazy
    private final OrderInfoMapper orderInfoService;

    /**
     * 推荐景点
     *
     * @param lat    纬度
     * @param lng    经度
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public List<ScenicInfo> queryScenicRecommend(Double lat, Double lng, Integer userId) {
        // 获取所有景点信息
        List<ScenicInfo> allScenicList = list();
        for (ScenicInfo scenicInfo : allScenicList) {
            if (StrUtil.isEmpty(scenicInfo.getPoint())) {
                continue;
            }
            double latitude = Double.parseDouble(scenicInfo.getPoint().split(",")[0]);
            double longitude = Double.parseDouble(scenicInfo.getPoint().split(",")[1]);
            scenicInfo.setLongitude(longitude);
            scenicInfo.setLatitude(latitude);
        }
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userId));
        // 1. 构建用户-景点评分矩阵
        Map<Integer, Map<Integer, Double>> userScenicRatingMatrix = buildUserScenicMatrix();

        // 2. 计算用户相似度（基于余弦相似度）
        Map<Integer, Double> userSimilarity = calculateUserSimilarity(userInfo.getId(), userScenicRatingMatrix);

        // 3. 预测评分
        Map<Integer, Double> predictedRatings = predictRatings(userInfo.getId(), userSimilarity, userScenicRatingMatrix);

        // 4. 根据预测评分排序并返回推荐结果
        List<ScenicInfo> recommendedScenics = sortAndFilterScenics(predictedRatings, allScenicList);

        // 如果协同过滤没有产生结果，回退到基于地理位置的推荐
        if (recommendedScenics.isEmpty()) {
            if (lat != null && lng != null) {
                return allScenicList.stream()
                        .sorted((s1, s2) -> {
                            double distance1 = calculateDistance(lat, lng, s1.getLatitude(), s1.getLongitude());
                            double distance2 = calculateDistance(lat, lng, s2.getLatitude(), s2.getLongitude());
                            return Double.compare(distance1, distance2);
                        })
                        .limit(10)
                        .collect(java.util.stream.Collectors.toList());
            }

            // 如果没有位置信息，则返回默认排序的景点列表
            return allScenicList.stream()
                    .limit(10)
                    .collect(java.util.stream.Collectors.toList());
        }

        return recommendedScenics;
    }

    /**
     * 推荐酒店
     *
     * @param lat  纬度
     * @param lng  经度
     * @param type 类型  1.附近一公里类 2.附近三公里内 3.附近五公里内 4.评分4.0以上
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryHotelByPosition(Double lat, Double lng, Integer type) {
        // 获取所有酒店信息
        List<HotelInfo> allHotelList = hotelInfoService.list(Wrappers.<HotelInfo>lambdaQuery());
        if (CollectionUtil.isEmpty(allHotelList)) {
            return Collections.emptyList();
        }
        for (HotelInfo hotelInfo : allHotelList) {
            if (StrUtil.isEmpty(hotelInfo.getPoint())) {
                continue;
            }
            double latitude = Double.parseDouble(hotelInfo.getPoint().split(",")[0]);
            double longitude = Double.parseDouble(hotelInfo.getPoint().split(",")[1]);
            hotelInfo.setLongitude(longitude);
            hotelInfo.setLatitude(latitude);
        }

        List<HotelInfo> filteredHotels;
        // 根据不同类型进行筛选
        switch (type) {
            case 1:
                // 附近一公里内
                filteredHotels = allHotelList.stream()
                        .filter(hotel -> calculateDistance(lat, lng, hotel.getLatitude(), hotel.getLongitude()) <= 1)
                        .collect(Collectors.toList());
                break;
            case 2:
                // 附近三公里内
                filteredHotels = allHotelList.stream()
                        .filter(hotel -> calculateDistance(lat, lng, hotel.getLatitude(), hotel.getLongitude()) <= 3)
                        .collect(Collectors.toList());
                break;
            case 3:
                // 附近五公里内
                filteredHotels = allHotelList.stream()
                        .filter(hotel -> calculateDistance(lat, lng, hotel.getLatitude(), hotel.getLongitude()) <= 5)
                        .collect(Collectors.toList());
                break;
            case 4:
                // 评分4.0以上
                filteredHotels = allHotelList.stream()
                        .filter(hotel -> hotel.getScore() != null && hotel.getScore() >= 4.0)
                        .collect(Collectors.toList());
                break;
            default:
                filteredHotels = allHotelList;
                break;
        }

        // 酒店房间
        if (CollectionUtil.isEmpty(filteredHotels)) {
            return Collections.emptyList();
        }
        List<Integer> hotelIds = filteredHotels.stream().map(HotelInfo::getId).collect(Collectors.toList());
        List<RoomType> roomList = roomTypeService.list(Wrappers.<RoomType>lambdaQuery().in(RoomType::getHotelId, hotelIds));
        Map<Integer, List<RoomType>> roomMap = roomList.stream().collect(Collectors.groupingBy(RoomType::getHotelId));

        // 转换为 LinkedHashMap 列表
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (HotelInfo hotel : filteredHotels) {
            LinkedHashMap<String, Object> hotelMap = new LinkedHashMap<>();
            hotelMap.put("id", hotel.getId());
            hotelMap.put("name", hotel.getName());
            hotelMap.put("address", hotel.getAddress());
            hotelMap.put("score", hotel.getScore());
            hotelMap.put("point", hotel.getPoint());
            hotelMap.put("distance", calculateDistance(lat, lng, hotel.getLatitude(), hotel.getLongitude()));
            hotelMap.put("roomType", roomMap.get(hotel.getId()));
            result.add(hotelMap);
        }

        // 按距离排序
        if (lat != null && lng != null && type != 4) {
            result.sort(Comparator.comparingDouble(hotel -> (Double) hotel.get("distance")));
        } else if (type == 4) {
            // 按评分排序
            result.sort((h1, h2) -> Double.compare((Double) h2.get("score"), (Double) h1.get("score")));
        }

        return result;
    }

    /**
     * 查询酒店可订房间
     *
     * @param hotelIds 酒店ID
     * @return 结果
     */
    public LinkedHashMap<String, Object> queryRoomByHotel(List<Integer> hotelIds) {
        // 酒店房间
        List<RoomType> roomList = roomTypeService.list(Wrappers.<RoomType>lambdaQuery().in(RoomType::getHotelId, hotelIds));
        // 房间订单
        List<OrderInfo> orderList = orderInfoService.selectList(Wrappers.<OrderInfo>lambdaQuery().in(OrderInfo::getHotelId, hotelIds).eq(OrderInfo::getDelFlag, 0));
        if (CollectionUtil.isEmpty(roomList)) {
            return null;
        }

        // 按酒店ID分组的房间类型余量
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        // 按酒店ID对房间进行分组
        Map<Integer, List<RoomType>> roomsByHotel = roomList.stream()
                .collect(Collectors.groupingBy(RoomType::getHotelId));
        // 按酒店ID对订单进行分组
        Map<Integer, List<OrderInfo>> ordersByHotel = orderList.stream()
                .collect(Collectors.groupingBy(OrderInfo::getHotelId));
        // 计算每个酒店每个房间类型的余量
        for (Map.Entry<Integer, List<RoomType>> hotelEntry : roomsByHotel.entrySet()) {
            Integer hotelId = hotelEntry.getKey();
            List<RoomType> hotelRooms = hotelEntry.getValue();
            List<OrderInfo> hotelOrders = ordersByHotel.getOrDefault(hotelId, new ArrayList<>());
            // 按房间类型ID对订单进行分组
            Map<Integer, List<OrderInfo>> ordersByRoomType = hotelOrders.stream()
                    .collect(Collectors.groupingBy(OrderInfo::getTypeId));
            // 计算每个房间类型的余量
            List<LinkedHashMap<String, Object>> roomAvailability = new ArrayList<>();
            for (RoomType room : hotelRooms) {
                int totalRooms = room.getNum(); // 总房间数
                List<OrderInfo> roomOrders = ordersByRoomType.getOrDefault(room.getId(), new ArrayList<>());
                int bookedRooms = roomOrders.size(); // 已预订房间数
                int availableRooms = totalRooms - bookedRooms; // 剩余房间数

                LinkedHashMap<String, Object> roomInfo = new LinkedHashMap<>();
                roomInfo.put("roomId", room.getId());
                roomInfo.put("roomName", room.getName());
                roomInfo.put("totalRooms", totalRooms);
                roomInfo.put("bookedRooms", bookedRooms);
                roomInfo.put("availableRooms", availableRooms);
                roomInfo.put("price", room.getPrice());
                roomAvailability.add(roomInfo);
            }
            result.put("hotel_" + hotelId, roomAvailability);
        }
        return result;
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

    /**
     * 构建用户-景点评分矩阵
     *
     * @return 用户-景点评分矩阵
     */
    private Map<Integer, Map<Integer, Double>> buildUserScenicMatrix() {
        Map<Integer, Map<Integer, Double>> userScenicRatingMatrix = new HashMap<>();

        // 获取所有用户评价数据
        List<Evaluation> allEvaluations = evaluationService.list(Wrappers.<Evaluation>lambdaQuery().eq(Evaluation::getType, 2));

        // 构建矩阵
        for (Evaluation evaluation : allEvaluations) {
            Integer userId = evaluation.getUserId();
            Integer scenicId = evaluation.getScenicId();
            Double rating = evaluation.getScore();

            userScenicRatingMatrix.computeIfAbsent(userId, k -> new HashMap<>())
                    .put(scenicId, rating);
        }

        return userScenicRatingMatrix;
    }

    /**
     * 计算用户相似度（基于余弦相似度）
     *
     * @param targetUserId           目标用户ID
     * @param userScenicRatingMatrix 用户-景点评分矩阵
     * @return 用户相似度映射
     */
    private Map<Integer, Double> calculateUserSimilarity(Integer targetUserId,
                                                         Map<Integer, Map<Integer, Double>> userScenicRatingMatrix) {
        Map<Integer, Double> userSimilarity = new HashMap<>();

        Map<Integer, Double> targetUserRatings = userScenicRatingMatrix.get(targetUserId);
        if (targetUserRatings == null) {
            return userSimilarity;
        }
        for (Map.Entry<Integer, Map<Integer, Double>> entry : userScenicRatingMatrix.entrySet()) {
            Integer userId = entry.getKey();
            if (userId.equals(targetUserId)) {
                continue; // 跳过目标用户自身
            }
            Map<Integer, Double> userRatings = entry.getValue();
            // 计算余弦相似度
            double similarity = cosineSimilarity(targetUserRatings, userRatings);
            if (similarity > 0) { // 只保留正相关用户
                userSimilarity.put(userId, similarity);
            }
        }

        return userSimilarity;
    }

    /**
     * 计算两个用户评分向量的余弦相似度
     *
     * @param ratings1 用户1的评分
     * @param ratings2 用户2的评分
     * @return 余弦相似度值
     */
    private double cosineSimilarity(Map<Integer, Double> ratings1, Map<Integer, Double> ratings2) {
        // 找到共同评分的景点
        Set<Integer> commonScenicIds = new HashSet<>(ratings1.keySet());
        commonScenicIds.retainAll(ratings2.keySet());

        if (commonScenicIds.isEmpty()) {
            return 0;
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (Integer scenicId : commonScenicIds) {
            double rating1 = ratings1.get(scenicId);
            double rating2 = ratings2.get(scenicId);
            dotProduct += rating1 * rating2;
            norm1 += rating1 * rating1;
            norm2 += rating2 * rating2;
        }
        if (norm1 == 0 || norm2 == 0) {
            return 0;
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 预测评分
     *
     * @param targetUserId           目标用户ID
     * @param userSimilarity         用户相似度映射
     * @param userScenicRatingMatrix 用户-景点评分矩阵
     * @return 预测评分映射
     */
    private Map<Integer, Double> predictRatings(Integer targetUserId,
                                                Map<Integer, Double> userSimilarity,
                                                Map<Integer, Map<Integer, Double>> userScenicRatingMatrix) {
        Map<Integer, Double> predictions = new HashMap<>();

        Map<Integer, Double> targetUserRatings = userScenicRatingMatrix.get(targetUserId);
        if (targetUserRatings == null) {
            return predictions;
        }
        // 获取目标用户未评分的景点
        Set<Integer> allScenicIds = userScenicRatingMatrix.values().stream()
                .flatMap(map -> map.keySet().stream())
                .collect(java.util.stream.Collectors.toSet());
        for (Integer scenicId : allScenicIds) {
            if (targetUserRatings.containsKey(scenicId)) {
                continue; // 跳过已评分的景点
            }
            double weightedSum = 0.0;
            double similaritySum = 0.0;

            // 基于相似用户的评分进行预测
            for (Map.Entry<Integer, Double> similarityEntry : userSimilarity.entrySet()) {
                Integer similarUserId = similarityEntry.getKey();
                Double similarity = similarityEntry.getValue();
                Map<Integer, Double> similarUserRatings = userScenicRatingMatrix.get(similarUserId);
                if (similarUserRatings != null && similarUserRatings.containsKey(scenicId)) {
                    Double rating = similarUserRatings.get(scenicId);
                    weightedSum += similarity * rating;
                    similaritySum += Math.abs(similarity);
                }
            }
            if (similaritySum > 0) {
                predictions.put(scenicId, weightedSum / similaritySum);
            }
        }
        return predictions;
    }

    /**
     * 根据预测评分排序并过滤景点
     *
     * @param predictedRatings 预测评分映射
     * @param allScenicList    所有景点列表
     * @return 推荐景点列表
     */
    private List<ScenicInfo> sortAndFilterScenics(Map<Integer, Double> predictedRatings,
                                                  List<ScenicInfo> allScenicList) {
        return allScenicList.stream()
                .filter(scenic -> predictedRatings.containsKey(scenic.getId()))
                .sorted((s1, s2) -> {
                    Double rating1 = predictedRatings.get(s1.getId());
                    Double rating2 = predictedRatings.get(s2.getId());
                    return Double.compare(rating2 != null ? rating2 : 0.0,
                            rating1 != null ? rating1 : 0.0);
                })
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
    }
}
