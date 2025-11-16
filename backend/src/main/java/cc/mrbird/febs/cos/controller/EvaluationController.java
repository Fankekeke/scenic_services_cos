package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.Evaluation;
import cc.mrbird.febs.cos.entity.HotelInfo;
import cc.mrbird.febs.cos.entity.ScenicInfo;
import cc.mrbird.febs.cos.entity.UserInfo;
import cc.mrbird.febs.cos.service.IEvaluationService;
import cc.mrbird.febs.cos.service.IHotelInfoService;
import cc.mrbird.febs.cos.service.IScenicInfoService;
import cc.mrbird.febs.cos.service.IUserInfoService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author Fank
 */
@RestController
@RequestMapping("/cos/evaluation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EvaluationController {

    private final IEvaluationService evaluationService;

    private final IUserInfoService userInfoService;

    private final IScenicInfoService scenicInfoService;

    private final IHotelInfoService hotelInfoService;

    /**
     * 分页查询景区评价信息
     *
     * @param page       分页对象
     * @param evaluation 评价信息
     * @return 结果
     */
    @GetMapping("/scenic/page")
    public R queryScenicEvaluatePage(Page<Evaluation> page, Evaluation evaluation) {
        return R.ok(evaluationService.queryScenicEvaluatePage(page, evaluation));
    }

    /**
     * 分页查询酒店评价信息
     *
     * @param page       分页对象
     * @param evaluation 评价信息
     * @return 评价信息
     */
    @GetMapping("/hotel/page")
    public R evaluationByPage(Page<Evaluation> page, Evaluation evaluation) {
        return R.ok(evaluationService.evaluationByPage(page, evaluation));
    }

    /**
     * 查询用户评价信息
     *
     * @param scenicId 景区ID
     * @return 结果
     */
    @GetMapping("/queryEvaluateByScenicId")
    public R queryEvaluateByScenicId(Integer scenicId) {
        return R.ok(evaluationService.queryEvaluateByScenicId(scenicId));
    }

    /**
     * 查询用户评价信息
     *
     * @param hotelId 景区ID
     * @return 结果
     */
    @GetMapping("/queryEvaluateByHotelId")
    public R queryEvaluateByHotelId(Integer hotelId) {
        return R.ok(evaluationService.getEvaluationByHotel(hotelId));
    }

    /**
     * 添加评价信息
     *
     * @param evaluation
     * @return
     */
    @PostMapping
    public R save(Evaluation evaluation) {
        evaluation.setCode("EVA-" + System.currentTimeMillis());
        evaluation.setCreateDate(DateUtil.formatDateTime(new Date()));

        evaluationService.save(evaluation);
        if (evaluation.getType() == 1) {
            HotelInfo hotelInfo = hotelInfoService.getById(evaluation.getHotelId());
            List<Evaluation> evaluationList = evaluationService.list(Wrappers.<Evaluation>lambdaQuery().eq(Evaluation::getHotelId, hotelInfo.getId()));
            if (CollectionUtil.isEmpty(evaluationList)) {
                hotelInfo.setScore(3.0);
            } else {
                hotelInfo.setScore(evaluationList.stream().mapToDouble(Evaluation::getScore).average().getAsDouble());
            }
            hotelInfoService.updateById(hotelInfo);
        } else {
            ScenicInfo scenicInfo = scenicInfoService.getById(evaluation.getScenicId());
            List<Evaluation> evaluationList = evaluationService.list(Wrappers.<Evaluation>lambdaQuery().eq(Evaluation::getScenicId, scenicInfo.getId()));
            if (CollectionUtil.isEmpty(evaluationList)) {
                scenicInfo.setScore(3.0);
            } else {
                scenicInfo.setScore(evaluationList.stream().mapToDouble(Evaluation::getScore).average().getAsDouble());
            }
            scenicInfoService.updateById(scenicInfo);
        }
        return R.ok(true);
    }

    /**
     * 删除评价信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(evaluationService.removeByIds(ids));
    }

}
