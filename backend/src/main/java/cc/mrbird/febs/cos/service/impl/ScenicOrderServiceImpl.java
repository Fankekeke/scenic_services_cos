package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.dao.ScenicInfoMapper;
import cc.mrbird.febs.cos.entity.ScenicInfo;
import cc.mrbird.febs.cos.entity.ScenicOrder;
import cc.mrbird.febs.cos.dao.ScenicOrderMapper;
import cc.mrbird.febs.cos.service.IScenicOrderService;
import cn.hutool.core.collection.CollectionUtil;
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
        Map<Integer, String> infoMap = scenicInfoList.stream().collect(Collectors.toMap(ScenicInfo::getId, ScenicInfo::getScenicName));
        // 统计票数与价格
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, List<ScenicOrder>> entry : orderMap.entrySet()) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>() {
                {
                    put("scenicName", infoMap.get(entry.getKey()));
                    put("amount", entry.getValue().size());
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
}
