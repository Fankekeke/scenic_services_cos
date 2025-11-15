package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.dao.OrderInfoMapper;
import cc.mrbird.febs.cos.dao.ScenicInfoMapper;
import cc.mrbird.febs.cos.entity.OrderInfo;
import cc.mrbird.febs.cos.entity.ScenicInfo;
import cc.mrbird.febs.cos.entity.ScenicOrder;
import cc.mrbird.febs.cos.dao.ScenicOrderMapper;
import cc.mrbird.febs.cos.service.IScenicInfoService;
import cc.mrbird.febs.cos.service.IScenicOrderService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScenicOrderServiceImpl extends ServiceImpl<ScenicOrderMapper, ScenicOrder> implements IScenicOrderService {

    private final ScenicInfoMapper scenicInfoMapper;

    private final OrderInfoMapper orderInfoMapper;

    private final IScenicInfoService scenicInfoService;

    @Override
    public IPage<LinkedHashMap<String, Object>> scenicInfoByPage(Page page, ScenicOrder scenicOrder) {
        return baseMapper.scenicInfoByPage(page, scenicOrder);
    }

    /**
     * 查询本月景点流量排行榜
     *
     * @param date 统计日期
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryScenicTop(String date) {
        // 根据时间获取本月订单
        List<ScenicOrder> scenicOrderList = this.list(new LambdaQueryWrapper<ScenicOrder>().apply("DATE_FORMAT(create_date, '%Y-%m') = {0}", date));
        if (CollectionUtil.isEmpty(scenicOrderList)) {
            return Collections.emptyList();
        }
        Map<Integer, List<ScenicOrder>> orderMap = scenicOrderList.stream().collect(Collectors.groupingBy(ScenicOrder::getScenicId));
        List<ScenicInfo> scenicInfoList = scenicInfoMapper.selectList(new LambdaQueryWrapper<ScenicInfo>().in(ScenicInfo::getId, orderMap.keySet()));
        Map<Integer, ScenicInfo> infoMap = scenicInfoList.stream().collect(Collectors.toMap(ScenicInfo::getId, e -> e));
        // 统计票数与价格
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, List<ScenicOrder>> entry : orderMap.entrySet()) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>() {
                {
                    put("scenicName", infoMap.get(entry.getKey()).getScenicName());
                    put("scenicImages", infoMap.get(entry.getKey()).getWebImg());
                    put("amount", entry.getValue().stream().map(ScenicOrder::getAmount).reduce(0, Integer::sum));
                    put("price", entry.getValue().stream().map(ScenicOrder::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
                }
            };
            result.add(map);
        }
        // 统计票数与价格并排序
        return result.stream()
                .sorted(Comparator.comparingInt(o -> -(Integer) o.get("amount")))
                .collect(Collectors.toList());
    }

    /**
     * 年统计订单及收益
     *
     * @param date 年份
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> selectStoreStatisticsByYear(String date) {
        // 返回数据
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        int year =  DateUtil.year(new Date());
        if (StrUtil.isNotEmpty(date)) {
            year = Integer.parseInt(date);
        }

        List<ScenicOrder> scenicOrderList = this.list(new LambdaQueryWrapper<ScenicOrder>().apply("DATE_FORMAT(create_date, '%Y') = {0}", year));
        for (ScenicOrder scenicOrder : scenicOrderList) {
            scenicOrder.setMonth(DateUtil.month(DateUtil.parseDate(scenicOrder.getCreateDate())) + 1);
        }
        Map<Integer, List<ScenicOrder>> orderOutMonthMap = scenicOrderList.stream().collect(Collectors.groupingBy(ScenicOrder::getMonth));
        List<OrderInfo> hotelOrderList = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>().apply("DATE_FORMAT(create_date, '%Y') = {0}", year));
        for (OrderInfo orderInfo : hotelOrderList) {
            orderInfo.setMonth(DateUtil.month(DateUtil.parseDate(orderInfo.getCreateDate())) + 1);
        }
        Map<Integer, List<OrderInfo>> orderPutMonthMap = hotelOrderList.stream().collect(Collectors.groupingBy(OrderInfo::getMonth));

        result.put("orderNum", scenicOrderList.size());
        BigDecimal totalPrice = scenicOrderList.stream().map(ScenicOrder::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalPrice", totalPrice);
        result.put("putNum", hotelOrderList.size());
        BigDecimal outlayPrice = hotelOrderList.stream().map(OrderInfo::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("outlayPrice", outlayPrice);

        List<Integer> orderNumList = new ArrayList<>();
        List<BigDecimal> orderPriceList = new ArrayList<>();
        List<Integer> outlayNumList = new ArrayList<>();
        List<BigDecimal> outlayPriceList = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {

            List<ScenicOrder> currentMonthOutList = orderOutMonthMap.get(i);
            if (CollectionUtil.isEmpty(currentMonthOutList)) {
                orderNumList.add(0);
                orderPriceList.add(BigDecimal.ZERO);
            } else {
                orderNumList.add(currentMonthOutList.size());
                BigDecimal currentMonthOutPrice = currentMonthOutList.stream().map(ScenicOrder::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                orderPriceList.add(currentMonthOutPrice);
            }

            List<OrderInfo> currentMonthPutList = orderPutMonthMap.get(i);
            if (CollectionUtil.isEmpty(currentMonthPutList)) {
                outlayNumList.add(0);
                outlayPriceList.add(BigDecimal.ZERO);
            } else {
                outlayNumList.add(currentMonthPutList.size());
                BigDecimal currentMonthPutPrice = currentMonthPutList.stream().map(OrderInfo::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                outlayPriceList.add(currentMonthPutPrice);
            }

        }
        result.put("orderPriceList", orderPriceList);
        result.put("orderNumList", orderNumList);
        result.put("outlayPriceList", outlayPriceList);
        result.put("outlayNumList", orderNumList);
//        result.put("outlayNumList", outlayNumList);

        // 景点销量排行
        List<LinkedHashMap<String, Object>> saleRank = new ArrayList<>();
        List<LinkedHashMap<String, Object>> salePriceRank = new ArrayList<>();
        LinkedHashMap<String, Integer> saleTypeRankMap = new LinkedHashMap<>();

        Map<Integer, List<ScenicOrder>> recordInfoMap = scenicOrderList.stream().collect(Collectors.groupingBy(ScenicOrder::getScenicId));

        // 景点信息
        Set<Integer> scenicCodeList = recordInfoMap.keySet();
        List<ScenicInfo> scenicInfoList = scenicInfoService.list(Wrappers.<ScenicInfo>lambdaQuery().in(CollectionUtil.isNotEmpty(scenicCodeList), ScenicInfo::getId, scenicCodeList));
        Map<Integer, ScenicInfo> scenicMap = scenicInfoList.stream().collect(Collectors.toMap(ScenicInfo::getId, e -> e));
        List<String> scenicTypeList = scenicInfoService.list().stream().map(ScenicInfo::getLevel).distinct().collect(Collectors.toList());

        recordInfoMap.forEach((key, value) -> {
            ScenicInfo scenic= scenicMap.get(key);
            if (scenic != null) {
                saleRank.add(new LinkedHashMap<String, Object>() {
                    {
                        put("name", scenic.getScenicName());
                        put("num", value.stream().mapToInt(ScenicOrder::getAmount).sum());
                    }
                });
                salePriceRank.add(new LinkedHashMap<String, Object>() {
                    {
                        put("name", scenic.getScenicName());
                        put("num", value.stream().map(ScenicOrder::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
                    }
                });

                saleTypeRankMap.merge(scenic.getLevel(), value.size(), Integer::sum);
            }
        });
        result.put("saleRank", saleRank);
        result.put("salePriceRank", salePriceRank);
        // 销售景点分类
        LinkedHashMap<String, Integer> saleTypeRankMapCopy = new LinkedHashMap<>();
        for (String level : scenicTypeList) {
            saleTypeRankMapCopy.put(level, saleTypeRankMap.get(level) == null ? 0 : saleTypeRankMap.get(level));
        }
        result.put("saleTypeRankMapCopy", saleTypeRankMapCopy);
        return result;
    }

    /**
     * 月统计订单及收益
     *
     * @param dateStr 日期
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> selectStoreStatisticsByMonth(String dateStr) {
        String date = dateStr + "-01";
        // 返回数据
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        int year =  DateUtil.year(new Date());
        int month =  DateUtil.month(new Date()) + 1;
        if (StrUtil.isNotEmpty(date)) {
            year = DateUtil.year(DateUtil.parseDate(date));
            month = DateUtil.month(DateUtil.parseDate(date)) + 1;
        }

        List<ScenicOrder> scenicOrderList = this.list(new LambdaQueryWrapper<ScenicOrder>().apply("DATE_FORMAT(create_date, '%Y%m') = {0}", (year + "" + ((month < 10) ? "0" + month : month))));
        for (ScenicOrder scenicOrder : scenicOrderList) {
            scenicOrder.setMonth(DateUtil.month(DateUtil.parseDate(scenicOrder.getCreateDate())) + 1);
            scenicOrder.setDay(DateUtil.dayOfMonth(DateUtil.parseDate(scenicOrder.getCreateDate())));
        }
        Map<Integer, List<ScenicOrder>> orderOutDayMap = scenicOrderList.stream().collect(Collectors.groupingBy(ScenicOrder::getDay));
        List<OrderInfo> hotelOrderList = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>().apply("DATE_FORMAT(create_date, '%Y%m') = {0}", (year + "" + ((month < 10) ? "0" + month : month))));
        for (OrderInfo orderInfo : hotelOrderList) {
            orderInfo.setMonth(DateUtil.month(DateUtil.parseDate(orderInfo.getCreateDate())) + 1);
            orderInfo.setDay(DateUtil.dayOfMonth(DateUtil.parseDate(orderInfo.getCreateDate())));
        }
        Map<Integer, List<OrderInfo>> orderPutDayMap = hotelOrderList.stream().collect(Collectors.groupingBy(OrderInfo::getDay));

        // 本月订单量
        result.put("orderNum", scenicOrderList.size());
        // 本月总收益
        BigDecimal totalPrice = scenicOrderList.stream().map(ScenicOrder::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalPrice", totalPrice);
        result.put("putNum", hotelOrderList.size());
        BigDecimal outlayPrice = hotelOrderList.stream().map(OrderInfo::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("outlayPrice", outlayPrice);

        List<Integer> orderNumList = new ArrayList<>();
        List<BigDecimal> orderPriceList = new ArrayList<>();
        List<Integer> outlayNumList = new ArrayList<>();
        List<BigDecimal> outlayPriceList = new ArrayList<>();
        int days = DateUtil.getLastDayOfMonth(DateUtil.parseDate(date));

        // 本月日期
        List<String> dateTimeList = new ArrayList<>();

        for (int i = 1; i <= days; i++) {
            dateTimeList.add(month + "月" + i + "日");
            List<ScenicOrder> currentDayOutList = orderOutDayMap.get(i);
            if (CollectionUtil.isEmpty(currentDayOutList)) {
                orderNumList.add(0);
                orderPriceList.add(BigDecimal.ZERO);
            } else {
                orderNumList.add(currentDayOutList.size());
                BigDecimal currentDayOutPrice = currentDayOutList.stream().map(ScenicOrder::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                orderPriceList.add(currentDayOutPrice);
            }

            // 本天入库
            List<OrderInfo> currentDayPutList = orderPutDayMap.get(i);
            if (CollectionUtil.isEmpty(currentDayPutList)) {
                outlayNumList.add(0);
                outlayPriceList.add(BigDecimal.ZERO);
            } else {
                outlayNumList.add(currentDayPutList.size());
                BigDecimal currentDayPutPrice = currentDayPutList.stream().map(OrderInfo::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                outlayPriceList.add(currentDayPutPrice);
            }

        }
        result.put("orderPriceList", orderPriceList);
        result.put("orderNumList", orderNumList);
        result.put("outlayPriceList", outlayPriceList);
        result.put("outlayNumList", orderNumList);
//        result.put("outlayNumList", outlayNumList);

        result.put("dateList", dateTimeList);
        // 景点销量排行
        List<LinkedHashMap<String, Object>> saleRank = new ArrayList<>();
        List<LinkedHashMap<String, Object>> salePriceRank = new ArrayList<>();
        LinkedHashMap<String, Integer> saleTypeRankMap = new LinkedHashMap<>();

        Map<Integer, List<ScenicOrder>> recordInfoMap = scenicOrderList.stream().collect(Collectors.groupingBy(ScenicOrder::getScenicId));

        // 景点信息
        Set<Integer> scenicIdList = recordInfoMap.keySet();
        List<ScenicInfo> scenicInfoList = scenicInfoService.list(Wrappers.<ScenicInfo>lambdaQuery().in(CollectionUtil.isNotEmpty(scenicIdList), ScenicInfo::getId, scenicIdList));
        Map<Integer, ScenicInfo> scenicMap = scenicInfoList.stream().collect(Collectors.toMap(ScenicInfo::getId, e -> e));

        recordInfoMap.forEach((key, value) -> {
            ScenicInfo scenicInfo = scenicMap.get(key);
            if (scenicInfo != null) {
                saleRank.add(new LinkedHashMap<String, Object>() {
                    {
                        put("name", scenicInfo.getScenicName());
                        put("num", value.stream().mapToInt(ScenicOrder::getAmount).sum());
                    }
                });
                salePriceRank.add(new LinkedHashMap<String, Object>() {
                    {
                        put("name", scenicInfo.getScenicName());
                        put("num", value.stream().map(ScenicOrder::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
                    }
                });

                saleTypeRankMap.merge(scenicInfo.getLevel(), value.size(), Integer::sum);
            }
        });
        result.put("saleRank", saleRank);
        result.put("salePriceRank", salePriceRank);
        // 销售景点分类
        LinkedHashMap<String, Integer> saleTypeRankMapCopy = new LinkedHashMap<>();
        List<String> scenicTypeList = scenicInfoService.list().stream().map(ScenicInfo::getLevel).distinct().collect(Collectors.toList());
        for (String level : scenicTypeList) {
            saleTypeRankMapCopy.put(level, saleTypeRankMap.get(level) == null ? 0 : saleTypeRankMap.get(level));
        }
        result.put("saleTypeRankMapCopy", saleTypeRankMapCopy);
        return result;
    }
}
