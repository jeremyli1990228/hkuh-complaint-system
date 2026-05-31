package com.hkuh.complaint.service;

import com.hkuh.complaint.dto.*;

public interface FeedbackService {

    FeedbackVO createFeedback(FeedbackCreateDTO dto, Long operatorId);

    PageResult<FeedbackVO> listFeedback(FeedbackQueryDTO query);

    FeedbackDetailVO getFeedbackDetail(Long id);

    HandleRecordVO handleFeedback(Long id, FeedbackHandleDTO dto, Long operatorId);

    void assignFeedback(Long id, FeedbackAssignDTO dto, Long operatorId);

    void autoSaveDraft(Long id, String content, Long operatorId);

    String generateFeedbackNo();

    void calculateSLA(Feedback feedback);
}
