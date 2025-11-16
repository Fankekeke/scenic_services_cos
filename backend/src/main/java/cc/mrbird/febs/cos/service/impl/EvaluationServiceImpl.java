package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.dao.EvaluationMapper;
import cc.mrbird.febs.cos.entity.Evaluation;
import cc.mrbird.febs.cos.service.IEvaluationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Fank
 */
@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements IEvaluationService {

    @Override
    public IPage<LinkedHashMap<String, Object>> evaluationByPage(Page page, Evaluation evaluation) {
        return baseMapper.evaluationByPage(page, evaluation);
    }

    /**
     * 分页查询景区评价信息
     *
     * @param page       分页对象
     * @param evaluation 评价信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryScenicEvaluatePage(Page<Evaluation> page, Evaluation evaluation) {
        return baseMapper.queryScenicEvaluatePage(page, evaluation);
    }

    /**
     * 获取景点评价
     *
     * @param scenicId 景区ID
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> queryEvaluateByScenicId(Integer scenicId) {
        return baseMapper.queryEvaluateByScenicId(scenicId);
    }

    @Override
    public List<LinkedHashMap<String, Object>> getEvaluationByHotel(Integer hotelId) {
        return baseMapper.getEvaluationByHotel(hotelId);
    }
}
