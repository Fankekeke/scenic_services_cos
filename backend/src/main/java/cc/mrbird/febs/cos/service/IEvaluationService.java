package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.Evaluation;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Fank
 */
public interface IEvaluationService extends IService<Evaluation> {

    /**
     * 分页查询评价信息
     *
     * @param page
     * @param evaluation
     * @return
     */
    IPage<LinkedHashMap<String, Object>> evaluationByPage(Page page, Evaluation evaluation);

    /**
     * 分页查询景区评价信息
     *
     * @param page       分页对象
     * @param evaluation 评价信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryScenicEvaluatePage(Page<Evaluation> page, Evaluation evaluation);

    /**
     * 获取景点评价
     *
     * @param scenicId 景区ID
     * @return 结果
     */
    List<LinkedHashMap<String, Object>> queryEvaluateByScenicId(@Param("scenicId") Integer scenicId);

    /**
     * 获取房间评价
     *
     * @param hotelId
     * @return
     */
    List<LinkedHashMap<String, Object>> getEvaluationByHotel(Integer hotelId);
}
