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

import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final JwtUtil jwtUtil;

    private static final List<String> VALID_SOURCES = Arrays.asList("WECHAT", "PHONE", "EMAIL", "ONLINE", "LETTER");
    private static final List<String> VALID_PRIORITIES = Arrays.asList("LOW", "NORMAL", "HIGH", "URGENT");
    private static final List<String> VALID_STATUSES = Arrays.asList("PENDING", "ASSIGNED", "PROCESSING", "REPLIED", "CLOSED", "REJECTED");
    private static final List<String> VALID_ACTIONS = Arrays.asList("PROCESS", "REPLY", "TRANSFER", "CLOSE", "REJECT");
    private static final List<String> VALID_RESULTS = Arrays.asList("PENDING", "RESOLVED", "UNRESOLVED");

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('feedback:create')")
    public Result<FeedbackVO> createFeedback(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody FeedbackCreateDTO createDTO) {

        validateAuthorization(authorization);

        if (StrUtil.isBlank(createDTO.getTitle())) {
            throw new BusinessException(400, "标题不能为空");
        }
        if (createDTO.getTitle().length() > 200) {
            throw new BusinessException(400, "标题长度不能超过200字");
        }
        if (StrUtil.isBlank(createDTO.getContent())) {
            throw new BusinessException(400, "内容不能为空");
        }
        if (createDTO.getFeedbackTypeId() == null) {
            throw new BusinessException(400, "反馈类型不能为空");
        }
        if (StrUtil.isBlank(createDTO.getSource())) {
            throw new BusinessException(400, "来源不能为空");
        }
        if (!VALID_SOURCES.contains(createDTO.getSource())) {
            throw new BusinessException(400, "来源类型无效");
        }

        String token = authorization.substring(7);
        String username = jwtUtil.getUsername(token);
        Long userId = jwtUtil.getUserId(token);

        String feedbackNo = generateFeedbackNo();
        String priority = StrUtil.isBlank(createDTO.getPriority()) ? "NORMAL" : createDTO.getPriority();
        if (!VALID_PRIORITIES.contains(priority)) {
            throw new BusinessException(400, "优先级类型无效");
        }

        FeedbackVO feedbackVO = FeedbackVO.builder()
                .id(System.currentTimeMillis())
                .feedbackNo(feedbackNo)
                .title(createDTO.getTitle())
                .content(createDTO.getContent())
                .feedbackTypeId(createDTO.getFeedbackTypeId())
                .feedbackTypeName("服务态度投诉")
                .source(createDTO.getSource())
                .priority(priority)
                .status("PENDING")
                .isAnonymous(createDTO.getIsAnonymous() != null && createDTO.getIsAnonymous() ? 1 : 0)
                .contactPhone(createDTO.getContactPhone())
                .contactEmail(createDTO.getContactEmail())
                .createTime(new Date())
                .createBy(username)
                .build();

        if (createDTO.getComplainant() != null && !createDTO.getIsAnonymous()) {
            feedbackVO.setComplainantName(createDTO.getComplainant().getName());
        }

        log.info("创建反馈工单成功: feedbackNo={}, userId={}", feedbackNo, userId);

        return Result.success(feedbackVO);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('feedback:list')")
    public Result<PageResult<FeedbackVO>> getFeedbackList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String feedbackNo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long feedbackTypeId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long handlerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword) {

        if (source != null && !source.isEmpty() && !VALID_SOURCES.contains(source)) {
            throw new BusinessException(400, "来源类型无效");
        }
        if (priority != null && !priority.isEmpty() && !VALID_PRIORITIES.contains(priority)) {
            throw new BusinessException(400, "优先级类型无效");
        }
        if (status != null && !status.isEmpty() && !VALID_STATUSES.contains(status)) {
            throw new BusinessException(400, "状态类型无效");
        }

        List<FeedbackVO> mockList = createMockFeedbackList(pageNum, pageSize);
        long total = 100;

        PageResult<FeedbackVO> pageResult = PageResult.of(mockList, total, pageNum, pageSize);

        log.info("查询反馈列表成功: pageNum={}, pageSize={}", pageNum, pageSize);

        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('feedback:detail')")
    public Result<FeedbackDetailVO> getFeedbackDetail(
            @PathVariable Long id) {

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        FeedbackDetailVO detailVO = FeedbackDetailVO.builder()
                .id(id)
                .feedbackNo("FB" + System.currentTimeMillis())
                .title("服务态度投诉")
                .content("医护人员服务态度不好，需要改善")
                .feedbackTypeId(1L)
                .feedbackTypeName("服务态度投诉")
                .source("WECHAT")
                .priority("NORMAL")
                .status("PROCESSING")
                .deptId(1L)
                .deptName("客户服务部")
                .handlerId(2L)
                .handlerName("张三")
                .isAnonymous(0)
                .contactPhone("13800138000")
                .contactEmail("test@example.com")
                .createTime(new Date())
                .createBy("user001")
                .updateTime(new Date())
                .remark("")
                .complainant(ComplainantDTO.builder()
                        .name("李四")
                        .phone("13800138000")
                        .email("test@example.com")
                        .gender("M")
                        .age(35)
                        .address("香港")
                        .build())
                .handleRecords(Arrays.asList(
                        HandleRecordVO.builder()
                                .id(1L)
                                .feedbackId(id)
                                .handlerId(2L)
                                .handlerName("张三")
                                .action("ASSIGN")
                                .content("已分配给张三处理")
                                .result("PENDING")
                                .isVisible(1)
                                .createTime(new Date())
                                .build(),
                        HandleRecordVO.builder()
                                .id(2L)
                                .feedbackId(id)
                                .handlerId(2L)
                                .handlerName("张三")
                                .action("REPLY")
                                .content("正在调查中，请耐心等待")
                                .result("PENDING")
                                .isVisible(1)
                                .createTime(new Date())
                                .build()
                ))
                .attachments(Arrays.asList(
                        AttachmentVO.builder()
                                .id(1L)
                                .fileName("投诉截图1.png")
                                .filePath("/uploads/2024/01/01/screenshot1.png")
                                .fileSize(1024L)
                                .fileType("image/png")
                                .createTime(new Date())
                                .build()
                ))
                .build();

        log.info("查询反馈详情成功: id={}", id);

        return Result.success(detailVO);
    }

    @PostMapping("/handle/{id}")
    @PreAuthorize("hasAuthority('feedback:handle')")
    public Result<HandleRecordVO> handleFeedback(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody FeedbackHandleDTO handleDTO) {

        validateAuthorization(authorization);

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (StrUtil.isBlank(handleDTO.getAction())) {
            throw new BusinessException(400, "操作类型不能为空");
        }
        if (!VALID_ACTIONS.contains(handleDTO.getAction())) {
            throw new BusinessException(400, "操作类型无效");
        }
        if (handleDTO.getAction().equals("TRANSFER")) {
            if (handleDTO.getTransferTo() == null) {
                throw new BusinessException(400, "转派目标处理人不能为空");
            }
            if (handleDTO.getTransferDept() == null) {
                throw new BusinessException(400, "转派目标科室不能为空");
            }
        }

        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);

        String newStatus = switch (handleDTO.getAction()) {
            case "PROCESS" -> "PROCESSING";
            case "REPLY" -> "REPLIED";
            case "TRANSFER" -> "ASSIGNED";
            case "CLOSE" -> "CLOSED";
            case "REJECT" -> "REJECTED";
            default -> "PROCESSING";
        };

        HandleRecordVO recordVO = HandleRecordVO.builder()
                .id(System.currentTimeMillis())
                .feedbackId(id)
                .handlerId(userId)
                .handlerName(username)
                .action(handleDTO.getAction())
                .content(handleDTO.getContent())
                .result(handleDTO.getResult())
                .isVisible(handleDTO.getIsVisible() != null && !handleDTO.getIsVisible() ? 0 : 1)
                .createTime(new Date())
                .build();

        log.info("处理反馈成功: id={}, action={}, userId={}", id, handleDTO.getAction(), userId);

        return Result.success(recordVO);
    }

    @PostMapping("/assign/{id}")
    @PreAuthorize("hasAuthority('feedback:assign')")
    public Result<Void> assignFeedback(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody FeedbackAssignDTO assignDTO) {

        validateAuthorization(authorization);

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (assignDTO.getHandlerId() == null && assignDTO.getDeptId() == null) {
            throw new BusinessException(400, "处理人或科室至少选一个");
        }

        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserId(token);

        log.info("指派反馈成功: id={}, handlerId={}, deptId={}, userId={}",
                id, assignDTO.getHandlerId(), assignDTO.getDeptId(), userId);

        return Result.success("指派成功");
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('feedback:export')")
    public void exportFeedback(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String feedbackNo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long feedbackTypeId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "EXCEL") String exportType,
            HttpServletResponse response) {

        validateAuthorization(authorization);

        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserId(token);

        String fileName = "反馈列表_" + System.currentTimeMillis();
        String contentType = "application/octet-stream";

        if ("PDF".equalsIgnoreCase(exportType)) {
            fileName += ".pdf";
        } else {
            fileName += ".xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        log.info("导出反馈列表成功: exportType={}, userId={}", exportType, userId);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('feedback:statistics')")
    public Result<FeedbackStatisticsVO> getStatistics(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long deptId) {

        validateAuthorization(authorization);

        if (StrUtil.isBlank(startDate) || StrUtil.isBlank(endDate)) {
            throw new BusinessException(400, "开始日期和结束日期不能为空");
        }

        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("PENDING", 15L);
        byStatus.put("ASSIGNED", 10L);
        byStatus.put("PROCESSING", 25L);
        byStatus.put("REPLIED", 20L);
        byStatus.put("CLOSED", 80L);
        byStatus.put("REJECTED", 5L);

        Map<String, Long> byType = new HashMap<>();
        byType.put("服务态度", 40L);
        byType.put("医疗质量", 30L);
        byType.put("环境设施", 20L);
        byType.put("收费问题", 15L);
        byType.put("其他", 20L);

        Map<String, Long> bySource = new HashMap<>();
        bySource.put("WECHAT", 50L);
        bySource.put("PHONE", 30L);
        bySource.put("EMAIL", 15L);
        bySource.put("ONLINE", 20L);
        bySource.put("LETTER", 10L);

        Map<String, Long> byPriority = new HashMap<>();
        byPriority.put("LOW", 25L);
        byPriority.put("NORMAL", 50L);
        byPriority.put("HIGH", 30L);
        byPriority.put("URGENT", 20L);

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

        FeedbackStatisticsVO statisticsVO = FeedbackStatisticsVO.builder()
                .total(175L)
                .pending(15L)
                .processing(25L)
                .resolved(100L)
                .closed(80L)
                .byStatus(byStatus)
                .byType(byType)
                .bySource(bySource)
                .byPriority(byPriority)
                .avgHandleTime(24.5)
                .slaComplianceRate(0.92)
                .trend(trend)
                .build();

        log.info("查询反馈统计成功: startDate={}, endDate={}, deptId={}", startDate, endDate, deptId);

        return Result.success(statisticsVO);
    }

    @PostMapping("/{id}/rate")
    public Result<Void> rateFeedback(
            @PathVariable Long id,
            @RequestBody FeedbackRateDTO rateDTO) {

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (rateDTO.getRating() == null || rateDTO.getRating() < 1 || rateDTO.getRating() > 5) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }

        log.info("评价反馈成功: id={}, rating={}", id, rateDTO.getRating());

        return Result.success("评价成功，感谢您的反馈");
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

    private String generateFeedbackNo() {
        return "FB" + IdUtil.getSnowflakeNextIdStr().substring(0, 14).toUpperCase();
    }

    private List<FeedbackVO> createMockFeedbackList(int pageNum, int pageSize) {
        List<FeedbackVO> list = new ArrayList<>();
        String[] titles = {"服务态度投诉", "医疗质量问题", "环境设施问题", "收费投诉", "等待时间长"};
        String[] statuses = {"PENDING", "ASSIGNED", "PROCESSING", "REPLIED", "CLOSED"};
        String[] priorities = {"LOW", "NORMAL", "HIGH", "URGENT"};
        String[] sources = {"WECHAT", "PHONE", "EMAIL", "ONLINE", "LETTER"};

        for (int i = 0; i < pageSize; i++) {
            int index = (pageNum - 1) * pageSize + i;
            list.add(FeedbackVO.builder()
                    .id((long) (index + 1))
                    .feedbackNo("FB" + String.format("%012d", index + 1))
                    .title(titles[index % titles.length])
                    .content("投诉内容详情...")
                    .feedbackTypeId((long) (index % 5 + 1))
                    .feedbackTypeName("类型" + (index % 5 + 1))
                    .source(sources[index % sources.length])
                    .priority(priorities[index % priorities.length])
                    .status(statuses[index % statuses.length])
                    .deptId((long) (index % 3 + 1))
                    .deptName("部门" + (index % 3 + 1))
                    .handlerId((long) (index % 5 + 1))
                    .handlerName("处理人" + (index % 5 + 1))
                    .createTime(new Date())
                    .rating(index % 3 == 0 ? null : (index % 5 + 1))
                    .build());
        }
        return list;
    }
}
