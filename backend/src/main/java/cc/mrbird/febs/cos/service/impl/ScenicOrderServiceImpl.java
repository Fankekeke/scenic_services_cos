package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.dao.EvaluationMapper;
import cc.mrbird.febs.cos.dao.OrderInfoMapper;
import cc.mrbird.febs.cos.dao.ScenicInfoMapper;
import cc.mrbird.febs.cos.entity.Evaluation;
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

    private final EvaluationMapper evaluationMapper;

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
     * 查询订单详情
     *
     * @param orderCode 订单ID
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> queryScenicOrderDetailByCode(String orderCode) {
        return baseMapper.queryScenicOrderDetailByCode(orderCode);
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

    /**
     * 景点评价占比
     *
     * @return 评价占比
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryEvaluateRate() {
        List<Evaluation> evaluationList = evaluationMapper.selectList(Wrappers.<Evaluation>lambdaQuery().eq(Evaluation::getType, 2));
        // 分数维度 从1.0到5.0，保留小数点后一位
        List<Double> scoreList = new ArrayList<>();
        for (int i = 10; i <= 50; i += 1) {
            scoreList.add(i / 10.0);
        }
        if (CollectionUtil.isEmpty(evaluationList)) {
            return scoreList.stream().map(score -> {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                map.put("score", score);
                map.put("num", 0);
                return map;
            }).collect(Collectors.toList());
        }

        Map<Double, List<Evaluation>> scoreMap = evaluationList.stream()
                .collect(Collectors.groupingBy(evaluation -> {
                    // 对评价分数进行四舍五入到一位小数
                    double originalScore = evaluation.getScore();
                    BigDecimal roundedScore = new BigDecimal(originalScore).setScale(1, BigDecimal.ROUND_HALF_UP);
                    return roundedScore.doubleValue();
                }));

        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (Double score : scoreList) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("score", score);
            map.put("num", scoreMap.get(score) != null ? scoreMap.get(score).size() : 0);
            result.add(map);
        }
        return result;
    }

    /**
     * 景点区域占比
     *
     * @return 区域占比
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryAreaScenicNumRate() {
        // 获取所有景点信息
        List<ScenicInfo> scenicInfoList = scenicInfoService.list();

        // 提取省份并统计数量
        Map<String, Integer> provinceCountMap = new HashMap<>();
        for (ScenicInfo scenicInfo : scenicInfoList) {
            String address = scenicInfo.getArea();
            if (StrUtil.isNotEmpty(address)) {
                String province = extractProvince(address);
                if (StrUtil.isNotEmpty(province)) {
                    provinceCountMap.merge(province, 1, Integer::sum);
                }
            }
        }

        // 转换为结果格式
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : provinceCountMap.entrySet()) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("province", entry.getKey());
            map.put("count", entry.getValue());
            result.add(map);
        }
        return result;
    }

    /**
     * 从地址中提取省份
     *
     * @param address 完整地址
     * @return 省份名称
     */
    private String extractProvince(String address) {
        if (StrUtil.isEmpty(address)) {
            return null;
        }

        // 处理常见省份格式，如"江苏·南京·玄武区"或"江苏省南京市玄武区"
        if (address.contains("·")) {
            // 处理"江苏·南京·玄武区"这种格式
            String[] parts = address.split("·");
            if (parts.length > 0) {
                String provincePart = parts[0];
                // 去除可能的省份后缀
                if (provincePart.endsWith("省")) {
                    return provincePart.substring(0, provincePart.length() - 1);
                }
                return provincePart;
            }
        } else {
            // 处理"江苏省南京市玄武区"这种格式
            if (address.length() >= 2) {
                String potentialProvince = address.substring(0, 2);
                if (potentialProvince.endsWith("省") || potentialProvince.endsWith("市") ||
                        potentialProvince.equals("北京") || potentialProvince.equals("上海") ||
                        potentialProvince.equals("天津") || potentialProvince.equals("重庆")) {
                    // 包含省份或直辖市
                    if (potentialProvince.endsWith("省")) {
                        return potentialProvince.substring(0, potentialProvince.length() - 1);
                    } else {
                        return potentialProvince;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 景点等级占比
     *
     * @return 等级占比
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryScenicLevelRate() {
        // 获取所有景点信息
        List<ScenicInfo> scenicInfoList = scenicInfoService.list();
        int totalCount = scenicInfoList.size();

        // 统计各等级的景点数量
        Map<String, Integer> levelCountMap = new HashMap<>();
        for (ScenicInfo scenicInfo : scenicInfoList) {
            String level = scenicInfo.getLevel();
            if (StrUtil.isNotEmpty(level)) {
                levelCountMap.merge(level, 1, Integer::sum);
            }
        }
        // 转换为结果格式，包含占比
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : levelCountMap.entrySet()) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("level", entry.getKey());
            map.put("count", entry.getValue());
            map.put("percentage", (double) entry.getValue() / totalCount * 100);
            result.add(map);
        }
        return result;
    }

    /**
     * 景点价格分布占比
     *
     * @return 价格分布占比
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryPriceStepRate() {
        // 获取所有景点信息
        List<ScenicInfo> scenicInfoList = scenicInfoService.list();

        // 定义价格区间
        int[] priceRanges = {0, 50, 100, 150, 200, 250, 300, 350, 400, 500, 600};
        String[] rangeLabels = {"0-50", "50-100", "100-150", "150-200", "200-250", "250-350", "350-400", "400-500", "500-600"};

        // 初始化各区间计数器
        Map<String, Integer> rangeCountMap = new HashMap<>();
        for (String label : rangeLabels) {
            rangeCountMap.put(label, 0);
        }
        // 统计每个景点的价格所属区间
        for (ScenicInfo scenicInfo : scenicInfoList) {
            BigDecimal price = scenicInfo.getScenicPrice();
            if (price != null) {
                int priceInt = price.intValue();
                String rangeLabel = getPriceRangeLabel(priceInt, priceRanges, rangeLabels);
                if (rangeLabel != null) {
                    rangeCountMap.merge(rangeLabel, 1, Integer::sum);
                }
            }
        }
        // 转换为结果格式
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (String rangeLabel : rangeLabels) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("range", rangeLabel);
            map.put("count", rangeCountMap.get(rangeLabel));
            result.add(map);
        }
        return result;
    }

    /**
     * 根据价格获取所属区间标签
     *
     * @param price 价格
     * @param priceRanges 价格区间数组
     * @param rangeLabels 区间标签数组
     * @return 区间标签
     */
    private String getPriceRangeLabel(int price, int[] priceRanges, String[] rangeLabels) {
        for (int i = 0; i < priceRanges.length - 1; i++) {
            if (price >= priceRanges[i] && price < priceRanges[i + 1]) {
                // 处理特定区间标签
                if (i == 0) return "0-50";
                else if (i == 1) return "50-100";
                else if (i == 2) return "100-150";
                else if (i == 3) return "150-200";
                else if (i == 4) return "200-250";
                else if (i == 5) return "250-350";
                else if (i == 6) return "350-400";
                else if (i == 7) return "400-500";
                else if (i == 8) return "500-600";
            }
        }
        // 如果价格超过最大区间，归入最大区间
        if (price >= priceRanges[priceRanges.length - 1]) {
            return "500-600";
        }
        return null;
    }

    /**
     * 订单词云
     *
     * @return 订单词云
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryOrderWordCloud() {
        List<ScenicInfo> scenicInfoList = scenicInfoService.list();
        List<String> wordList = new ArrayList<>();

        // 收集景点名称和描述中的关键词
        for (ScenicInfo scenicInfo : scenicInfoList) {
            if (StrUtil.isNotEmpty(scenicInfo.getHistory())) {
                wordList.addAll(segmentWords(scenicInfo.getHistory()));
            }
        }

        // 统计词频
        Map<String, Integer> wordCountMap = new HashMap<>();
        for (String word : wordList) {
            if (StrUtil.isNotEmpty(word) && !isStopWord(word)) {
                wordCountMap.merge(word, 1, Integer::sum);
            }
        }

        // 按词频排序并取前50个
        List<LinkedHashMap<String, Object>> result = wordCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(50)
                .map(entry -> {
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    map.put("word", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 判断字符是否为中文
     *
     * @param c 待判断字符
     * @return 是否为中文
     */
    private boolean isChineseChar(char c) {
        return c >= 0x4e00 && c <= 0x9fa5;
    }

    /**
     * 简单的中文分词处理
     *
     * @param text 待分词的文本
     * @return 分词结果列表
     */
    private List<String> segmentWords(String text) {
        List<String> words = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        for (char c : text.toCharArray()) {
            // 判断是否为中文字符
            if (isChineseChar(c)) {
                currentWord.append(c);
            } else {
                // 遇到非中文字符，将当前词加入列表
                if (currentWord.length() > 0) {
                    String word = currentWord.toString();
                    if (word.length() >= 2) { // 过滤单个字符
                        words.add(word);
                    }
                    currentWord = new StringBuilder();
                }
            }
        }
        // 处理最后一个词
        if (currentWord.length() > 0) {
            String word = currentWord.toString();
            if (word.length() >= 2) {
                words.add(word);
            }
        }
        return words;
    }


    /**
     * 判断是否为停用词
     *
     * @param word 待判断词汇
     * @return 是否为停用词
     */
    private boolean isStopWord(String word) {
        // 定义常见停用词
        Set<String> stopWords = new HashSet<>();
        stopWords.add("的");
        stopWords.add("了");
        stopWords.add("在");
        stopWords.add("是");
        stopWords.add("我");
        stopWords.add("有");
        stopWords.add("和");
        stopWords.add("就");
        stopWords.add("不");
        stopWords.add("人");
        stopWords.add("都");
        stopWords.add("一");
        stopWords.add("一个");
        stopWords.add("上");
        stopWords.add("也");
        stopWords.add("很");
        stopWords.add("到");
        stopWords.add("说");
        stopWords.add("要");
        stopWords.add("去");
        stopWords.add("你");
        stopWords.add("会");
        stopWords.add("着");
        stopWords.add("没有");
        stopWords.add("看");
        stopWords.add("好");
        stopWords.add("自己");
        stopWords.add("这");

        return stopWords.contains(word);
    }
}
