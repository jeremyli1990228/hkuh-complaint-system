package com.hkuh.complaint.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.hkuh.complaint.constant.ResultCode;
import com.hkuh.complaint.dto.*;
import com.hkuh.complaint.exception.BusinessException;
import com.hkuh.complaint.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/satisfaction")
@RequiredArgsConstructor
public class SatisfactionController {

    private final JwtUtil jwtUtil;

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "SERVICE", "ENVIRONMENT", "ATTITUDE", "EFFICIENCY", "TECHNICAL"
    );
    private static final List<String> VALID_PERIODS = Arrays.asList(
            "WEEK", "MONTH", "QUARTER", "YEAR"
    );
    private static final BigDecimal MIN_SCORE = new BigDecimal("1");
    private static final BigDecimal MAX_SCORE = new BigDecimal("10");
    private static final BigDecimal SATISFACTION_THRESHOLD = new BigDecimal("8");

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('satisfaction:create')")
    public Result<SatisfactionSurveyVO> createSurvey(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody SatisfactionCreateDTO createDTO) {

        validateAuthorization(authorization);

        if (StrUtil.isBlank(createDTO.getTitle())) {
            throw new BusinessException(400, "调查标题不能为空");
        }
        if (createDTO.getStartTime() == null) {
            throw new BusinessException(400, "开始时间不能为空");
        }
        if (createDTO.getEndTime() == null) {
            throw new BusinessException(400, "结束时间不能为空");
        }
        if (createDTO.getEndTime().before(createDTO.getStartTime())) {
            throw new BusinessException(400, "结束时间不能早于开始时间");
        }

        String token = authorization.substring(7);
        String username = jwtUtil.getUsername(token);

        String surveyNo = generateSurveyNo();
        Date now = new Date();
        String status = now.before(createDTO.getStartTime()) ? "SCHEDULED" :
                now.after(createDTO.getEndTime()) ? "EXPIRED" : "ACTIVE";

        SatisfactionSurveyVO surveyVO = SatisfactionSurveyVO.builder()
                .id(System.currentTimeMillis())
                .surveyNo(surveyNo)
                .feedbackId(createDTO.getFeedbackId())
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .startTime(createDTO.getStartTime())
                .endTime(createDTO.getEndTime())
                .status(status)
                .responseCount(0L)
                .avgScore(BigDecimal.ZERO)
                .createTime(now)
                .createBy(username)
                .build();

        log.info("创建满意度调查成功: surveyNo={}, userId={}", surveyNo, jwtUtil.getUserId(token));

        return Result.success(surveyVO);
    }

    @PostMapping("/submit")
    public Result<Void> submitSatisfaction(
            @RequestBody SatisfactionSubmitDTO submitDTO) {

        if (submitDTO.getSurveyId() == null) {
            throw new BusinessException(400, "调查ID不能为空");
        }
        if (submitDTO.getOverallScore() == null) {
            throw new BusinessException(400, "总体评分不能为空");
        }
        if (submitDTO.getOverallScore().compareTo(MIN_SCORE) < 0 ||
                submitDTO.getOverallScore().compareTo(MAX_SCORE) > 0) {
            throw new BusinessException(400, "评分必须在1-10之间");
        }
        if (submitDTO.getItems() == null || submitDTO.getItems().isEmpty()) {
            throw new BusinessException(400, "评价项目不能为空");
        }

        for (SatisfactionItemDTO item : submitDTO.getItems()) {
            if (StrUtil.isBlank(item.getCategory())) {
                throw new BusinessException(400, "评价类别不能为空");
            }
            if (!VALID_CATEGORIES.contains(item.getCategory())) {
                throw new BusinessException(400, "评价类别无效: " + item.getCategory());
            }
            if (item.getScore() == null ||
                    item.getScore().compareTo(MIN_SCORE) < 0 ||
                    item.getScore().compareTo(MAX_SCORE) > 0) {
                throw new BusinessException(400, "评分必须在1-10之间");
            }
        }

        log.info("提交满意度评价成功: surveyId={}, score={}", submitDTO.getSurveyId(), submitDTO.getOverallScore());

        return Result.success("评价提交成功，感谢您的反馈");
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('satisfaction:statistics')")
    public Result<SatisfactionStatisticsVO> getStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "MONTH") String period) {

        validateAuthorization(authorization);

        if (!VALID_PERIODS.contains(period)) {
            throw new BusinessException(400, "统计周期无效");
        }

        Map<Integer, Long> scoreDistribution = new LinkedHashMap<>();
        for (int i = 1; i <= 10; i++) {
            scoreDistribution.put(i, (long) (Math.random() * 50 + 10));
        }

        List<CategoryScoreVO> categoryScores = new ArrayList<>();
        String[] categoryNames = {"服务态度", "就医环境", "沟通交流", "处理效率", "技术水平"};
        for (int i = 0; i < VALID_CATEGORIES.size(); i++) {
            categoryScores.add(CategoryScoreVO.builder()
                    .category(VALID_CATEGORIES.get(i))
                    .categoryName(categoryNames[i])
                    .avgScore(BigDecimal.valueOf(Math.random() * 2 + 7))
                    .satisfactionRate(BigDecimal.valueOf(Math.random() * 0.3 + 0.65))
                    .responseCount((long) (Math.random() * 100 + 50))
                    .trend(createMockTrend())
                    .build());
        }

        List<DeptScoreVO> deptRanking = new ArrayList<>();
        String[] deptNames = {"内科", "外科", "儿科", "妇产科", "急诊科", "门诊部", "检验科", "放射科", "药剂科", "护理部"};
        for (int i = 0; i < deptNames.length; i++) {
            deptRanking.add(DeptScoreVO.builder()
                    .deptId((long) (i + 1))
                    .deptName(deptNames[i])
                    .avgScore(BigDecimal.valueOf(Math.random() * 2 + 7))
                    .satisfactionRate(BigDecimal.valueOf(Math.random() * 0.3 + 0.65))
                    .responseCount((long) (Math.random() * 200 + 50))
                    .ranking(i + 1)
                    .build());
        }
        deptRanking.sort((a, b) -> b.getAvgScore().compareTo(a.getAvgScore()));
        for (int i = 0; i < deptRanking.size(); i++) {
            deptRanking.get(i).setRanking(i + 1);
        }

        List<MonthlyTrendVO> monthlyTrend = new ArrayList<>();
        String[] months = {"2024-01", "2024-02", "2024-03", "2024-04", "2024-05", "2024-06"};
        for (String month : months) {
            monthlyTrend.add(MonthlyTrendVO.builder()
                    .month(month)
                    .avgScore(BigDecimal.valueOf(Math.random() * 1.5 + 8))
                    .responseCount((long) (Math.random() * 100 + 200))
                    .satisfactionRate(BigDecimal.valueOf(Math.random() * 0.15 + 0.8))
                    .nps(BigDecimal.valueOf(Math.random() * 30 + 40))
                    .build());
        }

        long totalResponses = 1250L;
        long promoters = (long) (totalResponses * 0.65);
        long passives = (long) (totalResponses * 0.25);
        long detractors = totalResponses - promoters - passives;
        BigDecimal nps = BigDecimal.valueOf((promoters - detractors) * 100.0 / totalResponses);

        SatisfactionStatisticsVO statisticsVO = SatisfactionStatisticsVO.builder()
                .totalResponses(totalResponses)
                .overallAvgScore(BigDecimal.valueOf(8.6))
                .overallSatisfactionRate(BigDecimal.valueOf(0.85))
                .nps(nps)
                .categoryScores(categoryScores)
                .scoreDistribution(scoreDistribution)
                .deptRanking(deptRanking)
                .monthlyTrend(monthlyTrend)
                .promoters(promoters)
                .passives(passives)
                .detractors(detractors)
                .build();

        log.info("查询满意度统计成功: startDate={}, endDate={}, deptId={}, period={}",
                startDate, endDate, deptId, period);

        return Result.success(statisticsVO);
    }

    @GetMapping("/dept-ranking")
    @PreAuthorize("hasAuthority('satisfaction:dept-ranking')")
    public Result<List<DeptScoreVO>> getDeptRanking(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") int top) {

        validateAuthorization(authorization);

        if (StrUtil.isBlank(startDate)) {
            throw new BusinessException(400, "开始日期不能为空");
        }
        if (StrUtil.isBlank(endDate)) {
            throw new BusinessException(400, "结束日期不能为空");
        }

        List<DeptScoreVO> deptRanking = new ArrayList<>();
        String[] deptNames = {"内科", "外科", "儿科", "妇产科", "急诊科", "门诊部", "检验科", "放射科", "药剂科", "护理部"};
        for (int i = 0; i < Math.min(top, deptNames.length); i++) {
            deptRanking.add(DeptScoreVO.builder()
                    .deptId((long) (i + 1))
                    .deptName(deptNames[i])
                    .avgScore(BigDecimal.valueOf(9.5 - i * 0.3))
                    .satisfactionRate(BigDecimal.valueOf(0.95 - i * 0.02))
                    .responseCount((long) (200 - i * 15))
                    .ranking(i + 1)
                    .build());
        }

        log.info("查询科室满意度排名成功: startDate={}, endDate={}, top={}", startDate, endDate, top);

        return Result.success(deptRanking);
    }

    @GetMapping("/detail/{surveyId}")
    @PreAuthorize("hasAuthority('satisfaction:detail')")
    public Result<SatisfactionDetailVO> getSurveyDetail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long surveyId) {

        validateAuthorization(authorization);

        if (surveyId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        List<SatisfactionItemDTO> items = new ArrayList<>();
        String[] itemNames = {"服务态度", "等候时间", "就医环境", "沟通效果", "收费透明"};
        for (int i = 0; i < itemNames.length; i++) {
            items.add(SatisfactionItemDTO.builder()
                    .category(VALID_CATEGORIES.get(i % VALID_CATEGORIES.size()))
                    .itemName(itemNames[i])
                    .score(BigDecimal.valueOf(Math.random() * 2 + 7.5))
                    .comment("")
                    .build());
        }

        List<CategoryScoreVO> categoryScores = new ArrayList<>();
        String[] categoryNames = {"服务态度", "就医环境", "沟通交流", "处理效率", "技术水平"};
        for (int i = 0; i < VALID_CATEGORIES.size(); i++) {
            categoryScores.add(CategoryScoreVO.builder()
                    .category(VALID_CATEGORIES.get(i))
                    .categoryName(categoryNames[i])
                    .avgScore(BigDecimal.valueOf(Math.random() * 2 + 7))
                    .satisfactionRate(BigDecimal.valueOf(Math.random() * 0.3 + 0.65))
                    .responseCount((long) (Math.random() * 100 + 50))
                    .build());
        }

        long responseCount = 156L;
        long promoters = 105L;
        long passives = 35L;
        long detractors = 16L;
        BigDecimal nps = BigDecimal.valueOf((promoters - detractors) * 100.0 / responseCount);

        SatisfactionDetailVO detailVO = SatisfactionDetailVO.builder()
                .id(surveyId)
                .surveyNo("SV" + String.format("%010d", surveyId))
                .feedbackId(1001L)
                .feedbackNo("FB2024010001")
                .title("2024年第一季度满意度调查")
                .description("请对本次就医体验进行评价，感谢您的配合")
                .startTime(new Date(System.currentTimeMillis() - 86400000L * 30))
                .endTime(new Date(System.currentTimeMillis() + 86400000L * 30))
                .status("ACTIVE")
                .avgScore(BigDecimal.valueOf(8.6))
                .responseCount(responseCount)
                .promoters(promoters)
                .passives(passives)
                .detractors(detractors)
                .nps(nps)
                .createTime(new Date(System.currentTimeMillis() - 86400000L * 30))
                .createBy("admin")
                .items(items)
                .categoryScores(categoryScores)
                .build();

        log.info("查询调查详情成功: surveyId={}", surveyId);

        return Result.success(detailVO);
    }

    private void validateAuthorization(String authorization) {
        if (StrUtil.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    private String generateSurveyNo() {
        return "SV" + IdUtil.getSnowflakeNextIdStr().substring(0, 14).toUpperCase();
    }

    private List<TrendItem> createMockTrend() {
        List<TrendItem> trend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            trend.add(TrendItem.builder()
                    .date("2024-01-" + (1 + i))
                    .total((long) (Math.random() * 20 + 10))
                    .processed((long) (Math.random() * 15 + 5))
                    .closed((long) (Math.random() * 10 + 2))
                    .avgHandleTime(Math.random() * 48 + 2)
                    .build());
        }
        return trend;
    }
}
