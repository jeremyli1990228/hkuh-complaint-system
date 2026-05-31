package com.hkuh.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkuh.complaint.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    Feedback selectById(@Param("id") Long id);

    Feedback selectByFeedbackNo(@Param("feedbackNo") String feedbackNo);

    List<Feedback> selectList(@Param("feedbackNo") String feedbackNo,
                              @Param("title") String title,
                              @Param("feedbackTypeId") Long feedbackTypeId,
                              @Param("source") String source,
                              @Param("priority") String priority,
                              @Param("status") String status,
                              @Param("deptId") Long deptId,
                              @Param("handlerId") Long handlerId,
                              @Param("startDate") String startDate,
                              @Param("endDate") String endDate,
                              @Param("keyword") String keyword,
                              @Param("startRow") int startRow,
                              @Param("endRow") int endRow);

    Feedback selectDetailById(@Param("id") Long id);

    List<Map<String, Object>> countByStatus(@Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    List<Map<String, Object>> countByType(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    List<Map<String, Object>> countBySource(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    int insert(Feedback feedback);

    int update(Feedback feedback);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("updateBy") String updateBy);

    List<Feedback> selectSlaWarningList();

    List<Feedback> selectByQuery(@Param("query") String query);

    Long countByQuery(@Param("feedbackNo") String feedbackNo,
                       @Param("title") String title,
                       @Param("feedbackTypeId") Long feedbackTypeId,
                       @Param("source") String source,
                       @Param("priority") String priority,
                       @Param("status") String status,
                       @Param("deptId") Long deptId,
                       @Param("handlerId") Long handlerId,
                       @Param("startDate") String startDate,
                       @Param("endDate") String endDate,
                       @Param("keyword") String keyword);

    int insertFeedback(Feedback feedback);

    int updateFeedback(Feedback feedback);
}
