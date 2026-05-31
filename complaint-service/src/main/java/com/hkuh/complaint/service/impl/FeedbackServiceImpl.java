package com.hkuh.complaint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hkuh.complaint.dto.*;
import com.hkuh.complaint.entity.*;
import com.hkuh.complaint.exception.BusinessException;
import com.hkuh.complaint.mapper.*;
import com.hkuh.complaint.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final ComplainantMapper complainantMapper;
    private final HandleRecordMapper handleRecordMapper;
    private final AttachmentMapper attachmentMapper;
    private final FeedbackTypeMapper feedbackTypeMapper;
    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String FEEDBACK_NO_KEY_PREFIX = "feedback:no:";
    private static final String BUSINESS_TYPE_FEEDBACK = "FEEDBACK";
    private static final long REDIS_KEY_EXPIRE_SECONDS = 86400L;

    @Override
    public FeedbackVO createFeedback(FeedbackCreateDTO dto, Long operatorId) {
        if (dto == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException(400, "标题不能为空");
        }
        if (dto.getTitle().length() > 200) {
            throw new BusinessException(400, "标题长度不能超过200字");
        }
        if (!StringUtils.hasText(dto.getContent())) {
            throw new BusinessException(400, "内容不能为空");
        }
        if (dto.getFeedbackTypeId() == null) {
            throw new BusinessException(400, "反馈类型不能为空");
        }

        FeedbackType feedbackType = feedbackTypeMapper.selectById(dto.getFeedbackTypeId());
        if (feedbackType == null) {
            throw new BusinessException(404, "反馈类型不存在");
        }

        Long complainantId = null;
        if (dto.getComplainant() != null && !Boolean.TRUE.equals(dto.getIsAnonymous())) {
            ComplainantDTO complainantDTO = dto.getComplainant();
            Complainant complainant = complainantMapper.selectByPhone(complainantDTO.getPhone());

            if (complainant == null) {
                complainant = new Complainant();
                complainant.setName(complainantDTO.getName());
                complainant.setPhone(complainantDTO.getPhone());
                complainant.setEmail(complainantDTO.getEmail());
                complainant.setGender(complainantDTO.getGender());
                complainant.setAge(complainantDTO.getAge());
                complainant.setAddress(complainantDTO.getAddress());
                complainant.setPatientId(complainantDTO.getPatientId());
                complainant.setIdCard(complainantDTO.getIdCard());
                complainant.setOccupation(complainantDTO.getOccupation());
                complainant.setCreateTime(new Date());
                complainant.setDelFlag(0);
                complainantMapper.insert(complainant);
            } else {
                complainant.setName(complainantDTO.getName());
                complainant.setEmail(complainantDTO.getEmail());
                complainant.setGender(complainantDTO.getGender());
                complainant.setAge(complainantDTO.getAge());
                complainant.setAddress(complainantDTO.getAddress());
                complainant.setPatientId(complainantDTO.getPatientId());
                complainant.setUpdateTime(new Date());
                complainantMapper.updateById(complainant);
            }
            complainantId = complainant.getId();
        }

        String feedbackNo = generateFeedbackNo();
        Date now = new Date();

        Feedback feedback = new Feedback();
        feedback.setFeedbackNo(feedbackNo);
        feedback.setTitle(dto.getTitle());
        feedback.setContent(dto.getContent());
        feedback.setFeedbackTypeId(dto.getFeedbackTypeId());
        feedback.setComplainantId(complainantId);
        feedback.setSource(dto.getSource());
        feedback.setPriority(StringUtils.hasText(dto.getPriority()) ? dto.getPriority() : "NORMAL");
        feedback.setStatus("PENDING");
        feedback.setIsAnonymous(Boolean.TRUE.equals(dto.getIsAnonymous()) ? 1 : 0);
        feedback.setContactPhone(dto.getContactPhone());
        feedback.setContactEmail(dto.getContactEmail());
        feedback.setCreateTime(now);
        feedback.setCreateBy(String.valueOf(operatorId));
        feedback.setDelFlag(0);

        calculateSLA(feedback);

        feedbackMapper.insert(feedback);

        if (dto.getAttachmentIds() != null && !dto.getAttachmentIds().isEmpty()) {
            attachmentMapper.updateBusinessId(dto.getAttachmentIds(), feedback.getId(), BUSINESS_TYPE_FEEDBACK);
        }

        User operator = userMapper.selectById(operatorId);
        if (operator != null && operator.getDeptId() != null) {
            feedback.setDeptId(operator.getDeptId());
            feedback.setHandlerId(dto.getHandlerId());
            feedbackMapper.updateById(feedback);

            HandleRecord assignRecord = new HandleRecord();
            assignRecord.setFeedbackId(feedback.getId());
            assignRecord.setHandlerId(operatorId);
            assignRecord.setAction("ASSIGN");
            assignRecord.setContent("工单已创建并分配给 " + (dto.getHandlerId() != null ?
                    "处理人(ID:" + dto.getHandlerId() + ")" : "当前部门"));
            assignRecord.setResult("PENDING");
            assignRecord.setIsVisible(1);
            assignRecord.setCreateTime(now);
            handleRecordMapper.insert(assignRecord);
        }

        log.info("创建反馈工单成功: feedbackNo={}, id={}", feedbackNo, feedback.getId());

        return convertToFeedbackVO(feedback, feedbackType);
    }

    @Override
    public PageResult<FeedbackVO> listFeedback(FeedbackQueryDTO query) {
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        int offset = (pageNum - 1) * pageSize;

        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getFeedbackNo())) {
            wrapper.like(Feedback::getFeedbackNo, query.getFeedbackNo());
        }
        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(Feedback::getTitle, query.getTitle());
        }
        if (query.getFeedbackTypeId() != null) {
            wrapper.eq(Feedback::getFeedbackTypeId, query.getFeedbackTypeId());
        }
        if (StringUtils.hasText(query.getSource())) {
            wrapper.eq(Feedback::getSource, query.getSource());
        }
        if (StringUtils.hasText(query.getPriority())) {
            wrapper.eq(Feedback::getPriority, query.getPriority());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Feedback::getStatus, query.getStatus());
        }
        if (query.getDeptId() != null) {
            wrapper.eq(Feedback::getDeptId, query.getDeptId());
        }
        if (query.getHandlerId() != null) {
            wrapper.eq(Feedback::getHandlerId, query.getHandlerId());
        }
        if (StringUtils.hasText(query.getStartDate())) {
            wrapper.ge(Feedback::getCreateTime, query.getStartDate());
        }
        if (StringUtils.hasText(query.getEndDate())) {
            wrapper.le(Feedback::getCreateTime, query.getEndDate());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Feedback::getTitle, query.getKeyword())
                    .or().like(Feedback::getContent, query.getKeyword()));
        }

        wrapper.eq(Feedback::getDelFlag, 0);
        wrapper.orderByDesc(Feedback::getCreateTime);

        List<Feedback> feedbackList = feedbackMapper.selectList(wrapper);
        long total = feedbackMapper.selectCount(wrapper);

        List<FeedbackVO> voList = feedbackList.stream()
                .map(this::convertToFeedbackVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, total, pageNum, pageSize);
    }

    @Override
    public FeedbackDetailVO getFeedbackDetail(Long id) {
        if (id == null) {
            throw new BusinessException(400, "反馈ID不能为空");
        }

        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈记录不存在");
        }

        FeedbackType feedbackType = feedbackTypeMapper.selectById(feedback.getFeedbackTypeId());
        Dept dept = feedback.getDeptId() != null ? deptMapper.selectById(feedback.getDeptId()) : null;
        User handler = feedback.getHandlerId() != null ? userMapper.selectById(feedback.getHandlerId()) : null;

        Complainant complainant = null;
        ComplainantDTO complainantDTO = null;
        if (feedback.getComplainantId() != null) {
            complainant = complainantMapper.selectById(feedback.getComplainantId());
            if (complainant != null) {
                complainantDTO = ComplainantDTO.builder()
                        .name(maskName(complainant.getName()))
                        .phone(maskPhone(complainant.getPhone()))
                        .email(maskEmail(complainant.getEmail()))
                        .gender(complainant.getGender())
                        .age(complainant.getAge())
                        .address(complainant.getAddress())
                        .patientId(complainant.getPatientId())
                        .build();
            }
        }

        List<HandleRecord> records = handleRecordMapper.selectByFeedbackId(id);
        List<HandleRecordVO> recordVOs = records.stream()
                .map(r -> {
                    User recordHandler = userMapper.selectById(r.getHandlerId());
                    return HandleRecordVO.builder()
                            .id(r.getId())
                            .feedbackId(r.getFeedbackId())
                            .handlerId(r.getHandlerId())
                            .handlerName(recordHandler != null ? recordHandler.getRealName() : null)
                            .action(r.getAction())
                            .content(r.getContent())
                            .result(r.getResult())
                            .isVisible(r.getIsVisible())
                            .createTime(r.getCreateTime())
                            .build();
                })
                .collect(Collectors.toList());

        List<Attachment> attachments = attachmentMapper.selectByBusinessId(id, BUSINESS_TYPE_FEEDBACK);
        List<AttachmentVO> attachmentVOs = attachments.stream()
                .map(a -> AttachmentVO.builder()
                        .id(a.getId())
                        .fileName(a.getFileName())
                        .filePath(a.getFilePath())
                        .fileSize(a.getFileSize())
                        .fileType(a.getFileType())
                        .createTime(a.getCreateTime())
                        .build())
                .collect(Collectors.toList());

        return FeedbackDetailVO.builder()
                .id(feedback.getId())
                .feedbackNo(feedback.getFeedbackNo())
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .feedbackTypeId(feedback.getFeedbackTypeId())
                .feedbackTypeName(feedbackType != null ? feedbackType.getTypeName() : null)
                .source(feedback.getSource())
                .priority(feedback.getPriority())
                .status(feedback.getStatus())
                .deptId(feedback.getDeptId())
                .deptName(dept != null ? dept.getDeptName() : null)
                .handlerId(feedback.getHandlerId())
                .handlerName(handler != null ? handler.getRealName() : null)
                .slaDeadline(feedback.getSlaDeadline())
                .slaWarningTime(feedback.getSlaWarningTime())
                .isAnonymous(feedback.getIsAnonymous())
                .contactPhone(feedback.getContactPhone())
                .contactEmail(feedback.getContactEmail())
                .rating(feedback.getRating())
                .ratingComment(feedback.getRatingComment())
                .createTime(feedback.getCreateTime())
                .createBy(feedback.getCreateBy())
                .updateTime(feedback.getUpdateTime())
                .remark(feedback.getRemark())
                .complainant(complainantDTO)
                .handleRecords(recordVOs)
                .attachments(attachmentVOs)
                .build();
    }

    @Override
    public HandleRecordVO handleFeedback(Long id, FeedbackHandleDTO dto, Long operatorId) {
        if (id == null) {
            throw new BusinessException(400, "反馈ID不能为空");
        }
        if (dto == null || !StringUtils.hasText(dto.getAction())) {
            throw new BusinessException(400, "操作类型不能为空");
        }

        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈记录不存在");
        }

        String currentStatus = feedback.getStatus();
        if ("DRAFT".equals(currentStatus) || "PENDING".equals(currentStatus) ||
                "PROCESSING".equals(currentStatus) || "REPLIED".equals(currentStatus)) {
        } else {
            throw new BusinessException(400, "当前状态不允许处理操作");
        }

        String newStatus = switch (dto.getAction()) {
            case "PROCESS" -> {
                if (!StringUtils.hasText(dto.getContent())) {
                    throw new BusinessException(400, "处理内容不能为空");
                }
                yield "PROCESSING";
            }
            case "REPLY" -> "REPLIED";
            case "TRANSFER" -> {
                if (dto.getTransferTo() == null && dto.getTransferDept() == null) {
                    throw new BusinessException(400, "转派目标处理人或科室不能为空");
                }
                yield "PENDING";
            }
            case "CLOSE" -> "CLOSED";
            case "REJECT" -> "REJECTED";
            default -> throw new BusinessException(400, "无效的操作类型: " + dto.getAction());
        };

        if ("TRANSFER".equals(dto.getAction())) {
            if (dto.getTransferTo() != null) {
                feedback.setHandlerId(dto.getTransferTo());
            }
            if (dto.getTransferDept() != null) {
                feedback.setDeptId(dto.getTransferDept());
            }
        }

        feedback.setStatus(newStatus);
        feedback.setUpdateTime(new Date());
        feedback.setUpdateBy(String.valueOf(operatorId));
        feedbackMapper.updateById(feedback);

        String attachmentIds = dto.getAttachmentIds() != null && !dto.getAttachmentIds().isEmpty() ?
                String.join(",", dto.getAttachmentIds().stream().map(String::valueOf).toList()) : null;

        HandleRecord record = new HandleRecord();
        record.setFeedbackId(id);
        record.setHandlerId(operatorId);
        record.setAction(dto.getAction());
        record.setContent(dto.getContent());
        record.setAttachmentIds(attachmentIds);
        record.setResult(dto.getResult());
        record.setIsVisible(dto.getIsVisible() != null && !dto.getIsVisible() ? 0 : 1);
        record.setCreateTime(new Date());
        handleRecordMapper.insert(record);

        log.info("处理反馈成功: id={}, action={}, newStatus={}, operatorId={}",
                id, dto.getAction(), newStatus, operatorId);

        User operator = userMapper.selectById(operatorId);
        return HandleRecordVO.builder()
                .id(record.getId())
                .feedbackId(record.getFeedbackId())
                .handlerId(record.getHandlerId())
                .handlerName(operator != null ? operator.getRealName() : null)
                .action(record.getAction())
                .content(record.getContent())
                .result(record.getResult())
                .isVisible(record.getIsVisible())
                .createTime(record.getCreateTime())
                .build();
    }

    @Override
    public void assignFeedback(Long id, FeedbackAssignDTO dto, Long operatorId) {
        if (id == null) {
            throw new BusinessException(400, "反馈ID不能为空");
        }
        if (dto.getHandlerId() == null && dto.getDeptId() == null) {
            throw new BusinessException(400, "处理人或科室至少选一个");
        }

        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈记录不存在");
        }

        if (dto.getHandlerId() != null) {
            feedback.setHandlerId(dto.getHandlerId());
        }
        if (dto.getDeptId() != null) {
            feedback.setDeptId(dto.getDeptId());
        }

        if ("PENDING".equals(feedback.getStatus())) {
            feedback.setStatus("ASSIGNED");
        }

        feedback.setUpdateTime(new Date());
        feedback.setUpdateBy(String.valueOf(operatorId));
        feedbackMapper.updateById(feedback);

        HandleRecord record = new HandleRecord();
        record.setFeedbackId(id);
        record.setHandlerId(operatorId);
        record.setAction("ASSIGN");
        record.setContent(StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "工单已指派");
        record.setResult("PENDING");
        record.setIsVisible(1);
        record.setCreateTime(new Date());
        handleRecordMapper.insert(record);

        log.info("指派反馈成功: id={}, handlerId={}, deptId={}, operatorId={}",
                id, dto.getHandlerId(), dto.getDeptId(), operatorId);
    }

    @Override
    public void autoSaveDraft(Long id, String content, Long operatorId) {
        if (id == null) {
            throw new BusinessException(400, "反馈ID不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(400, "草稿内容不能为空");
        }

        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(404, "反馈记录不存在");
        }

        String currentStatus = feedback.getStatus();
        if (!"DRAFT".equals(currentStatus) && !"PROCESSING".equals(currentStatus)) {
            throw new BusinessException(400, "当前状态不允许保存草稿");
        }

        feedback.setContent(content);
        feedback.setUpdateTime(new Date());
        feedback.setUpdateBy(String.valueOf(operatorId));
        feedbackMapper.updateById(feedback);

        log.info("自动保存草稿成功: id={}, operatorId={}", id, operatorId);
    }

    @Override
    public String generateFeedbackNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String key = FEEDBACK_NO_KEY_PREFIX + dateStr;

        Long sequence = redisTemplate.opsForValue().increment(key);
        if (sequence == null) {
            sequence = 1L;
        }

        redisTemplate.expire(key, REDIS_KEY_EXPIRE_SECONDS, TimeUnit.SECONDS);

        String feedbackNo = "TS-" + dateStr + "-" + String.format("%04d", sequence);

        log.debug("生成反馈工单编号: {}", feedbackNo);

        return feedbackNo;
    }

    @Override
    public void calculateSLA(Feedback feedback) {
        if (feedback == null || feedback.getFeedbackTypeId() == null) {
            return;
        }

        FeedbackType feedbackType = feedbackTypeMapper.selectById(feedback.getFeedbackTypeId());
        if (feedbackType == null) {
            return;
        }

        int slaDays = feedbackType.getSlaDays() != null ? feedbackType.getSlaDays() : 7;
        int slaHours = feedbackType.getSlaHours() != null ? feedbackType.getSlaHours() : 0;

        String priority = feedback.getPriority();
        if ("URGENT".equals(priority)) {
            slaDays = Math.max(1, slaDays / 2);
            slaHours = Math.max(1, slaHours / 2);
        } else if ("HIGH".equals(priority)) {
            slaDays = (int) Math.ceil(slaDays * 0.75);
            slaHours = (int) Math.ceil(slaHours * 0.75);
        }

        Date createTime = feedback.getCreateTime() != null ? feedback.getCreateTime() : new Date();

        long totalMilliseconds = (long) slaDays * 24 * 60 * 60 * 1000L + (long) slaHours * 60 * 60 * 1000L;
        Date deadline = new Date(createTime.getTime() + totalMilliseconds);
        Date warningTime = new Date(deadline.getTime() - 24 * 60 * 60 * 1000L);

        feedback.setSlaDeadline(deadline);
        feedback.setSlaWarningTime(warningTime);

        log.debug("SLA计算完成: feedbackType={}, priority={}, slaDays={}, deadline={}, warningTime={}",
                feedbackType.getTypeName(), priority, slaDays, deadline, warningTime);
    }

    private FeedbackVO convertToFeedbackVO(Feedback feedback) {
        return convertToFeedbackVO(feedback, null);
    }

    private FeedbackVO convertToFeedbackVO(Feedback feedback, FeedbackType feedbackType) {
        if (feedbackType == null && feedback.getFeedbackTypeId() != null) {
            feedbackType = feedbackTypeMapper.selectById(feedback.getFeedbackTypeId());
        }

        Dept dept = feedback.getDeptId() != null ? deptMapper.selectById(feedback.getDeptId()) : null;
        User handler = feedback.getHandlerId() != null ? userMapper.selectById(feedback.getHandlerId()) : null;

        String complainantName = null;
        if (feedback.getComplainantId() != null && feedback.getIsAnonymous() != 1) {
            Complainant complainant = complainantMapper.selectById(feedback.getComplainantId());
            if (complainant != null) {
                complainantName = complainant.getName();
            }
        }

        return FeedbackVO.builder()
                .id(feedback.getId())
                .feedbackNo(feedback.getFeedbackNo())
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .feedbackTypeId(feedback.getFeedbackTypeId())
                .feedbackTypeName(feedbackType != null ? feedbackType.getTypeName() : null)
                .source(feedback.getSource())
                .priority(feedback.getPriority())
                .status(feedback.getStatus())
                .deptId(feedback.getDeptId())
                .deptName(dept != null ? dept.getDeptName() : null)
                .handlerId(feedback.getHandlerId())
                .handlerName(handler != null ? handler.getRealName() : null)
                .complainantId(feedback.getComplainantId())
                .complainantName(complainantName)
                .slaDeadline(feedback.getSlaDeadline())
                .slaWarningTime(feedback.getSlaWarningTime())
                .isAnonymous(feedback.getIsAnonymous())
                .contactPhone(feedback.getContactPhone())
                .contactEmail(feedback.getContactEmail())
                .rating(feedback.getRating())
                .ratingComment(feedback.getRatingComment())
                .createTime(feedback.getCreateTime())
                .createBy(feedback.getCreateBy())
                .remark(feedback.getRemark())
                .build();
    }

    private String maskName(String name) {
        if (!StringUtils.hasText(name) || name.length() < 2) {
            return name;
        }
        return name.charAt(0) + "**";
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex < 2) {
            return email;
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
