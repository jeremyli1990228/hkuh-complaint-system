package com.hkuh.complaint.controller;

import com.hkuh.complaint.entity.Department;
import com.hkuh.complaint.entity.Feedback;
import com.hkuh.complaint.service.DepartmentService;
import com.hkuh.complaint.service.FeedbackService;
import com.hkuh.complaint.util.QrCodeUtil;
import com.hkuh.complaint.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/api/qrcode")
@Tag(name = "二维码管理", description = "二维码生成和下载接口")
public class QrCodeController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private DepartmentService departmentService;

    @Value("${app.base-url:https://m.hkuh.hk}")
    private String baseUrl;

    @Value("${app.admin-url:https://admin.hkuh.hk}")
    private String adminUrl;

    @GetMapping("/feedback/{feedbackNo}")
    @Operation(summary = "下载投诉查询二维码", description = "根据投诉工单号生成并下载二维码")
    public void downloadFeedbackQrCode(
            @Parameter(description = "投诉工单号") @PathVariable String feedbackNo,
            HttpServletResponse response) {

        try {
            Feedback feedback = feedbackService.getByFeedbackNo(feedbackNo);
            if (feedback == null) {
                log.warn("投诉工单不存在: {}", feedbackNo);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"code\":404,\"message\":\"投诉工单不存在\"}");
                return;
            }

            byte[] qrCodeBytes = QrCodeUtil.generateFeedbackQrCode(feedbackNo, baseUrl);

            String fileName = "feedback_" + feedbackNo + ".png";
            sendQrCodeResponse(response, qrCodeBytes, fileName);

            log.info("投诉查询二维码下载成功: {}", feedbackNo);

        } catch (Exception e) {
            log.error("生成投诉二维码失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/feedback/{feedbackNo}/base64")
    @Operation(summary = "获取投诉查询二维码(Base64)", description = "返回Base64编码的二维码图片")
    public Result<String> getFeedbackQrCodeBase64(
            @Parameter(description = "投诉工单号") @PathVariable String feedbackNo) {

        Feedback feedback = feedbackService.getByFeedbackNo(feedbackNo);
        if (feedback == null) {
            return Result.fail("投诉工单不存在");
        }

        String base64 = QrCodeUtil.generateQrCodeBase64(
                QrCodeUtil.buildFeedbackUrl(baseUrl, feedbackNo));

        return Result.success("data:image/png;base64," + base64);
    }

    @GetMapping("/dept/{deptId}")
    @Operation(summary = "下载科室投诉二维码", description = "根据科室ID生成并下载投诉二维码")
    public void downloadDeptQrCode(
            @Parameter(description = "科室ID") @PathVariable Long deptId,
            HttpServletResponse response) {

        try {
            Department dept = departmentService.getById(deptId);
            if (dept == null) {
                log.warn("科室不存在: {}", deptId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"code\":404,\"message\":\"科室不存在\"}");
                return;
            }

            byte[] qrCodeBytes = QrCodeUtil.generateDeptQrCode(
                    deptId, dept.getName(), baseUrl);

            String fileName = "dept_" + deptId + "_" + dept.getName() + ".png";
            fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            sendQrCodeResponse(response, qrCodeBytes, fileName);

            log.info("科室投诉二维码下载成功: deptId={}", deptId);

        } catch (Exception e) {
            log.error("生成科室二维码失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dept/{deptId}/base64")
    @Operation(summary = "获取科室投诉二维码(Base64)", description = "返回Base64编码的科室二维码图片")
    public Result<String> getDeptQrCodeBase64(
            @Parameter(description = "科室ID") @PathVariable Long deptId) {

        Department dept = departmentService.getById(deptId);
        if (dept == null) {
            return Result.fail("科室不存在");
        }

        String base64 = QrCodeUtil.generateQrCodeBase64(
                QrCodeUtil.buildSubmitUrl(baseUrl, deptId, null));

        return Result.success("data:image/png;base64," + base64);
    }

    @GetMapping("/ward/{wardId}")
    @Operation(summary = "下载病区投诉二维码", description = "根据病区ID生成并下载投诉二维码")
    public void downloadWardQrCode(
            @Parameter(description = "病区ID") @PathVariable Long wardId,
            @RequestParam(required = false) String wardName,
            HttpServletResponse response) {

        try {
            byte[] qrCodeBytes = QrCodeUtil.generateWardQrCode(
                    wardId, wardName != null ? wardName : "", baseUrl);

            String fileName = "ward_" + wardId + ".png";
            if (wardName != null) {
                fileName = "ward_" + wardId + "_" + wardName + ".png";
                fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            }
            sendQrCodeResponse(response, qrCodeBytes, fileName);

            log.info("病区投诉二维码下载成功: wardId={}", wardId);

        } catch (Exception e) {
            log.error("生成病区二维码失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ward/{wardId}/base64")
    @Operation(summary = "获取病区投诉二维码(Base64)", description = "返回Base64编码的病区二维码图片")
    public Result<String> getWardQrCodeBase64(
            @Parameter(description = "病区ID") @PathVariable Long wardId) {

        String base64 = QrCodeUtil.generateQrCodeBase64(
                QrCodeUtil.buildSubmitUrl(baseUrl, null, wardId));

        return Result.success("data:image/png;base64," + base64);
    }

    @GetMapping("/batch")
    @Operation(summary = "批量生成二维码", description = "批量生成科室或病区投诉二维码，返回ZIP文件")
    public void downloadBatchQrCodes(
            @Parameter(description = "类型：dept-科室, ward-病区") @RequestParam String type,
            @Parameter(description = "ID列表，逗号分隔") @RequestParam String ids,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            HttpServletResponse response) {

        if (width == null) width = 300;
        if (height == null) height = 300;

        try {
            List<String> idList = List.of(ids.split(","));
            List<Map<String, String>> qrCodeDataList = new ArrayList<>();

            if ("dept".equalsIgnoreCase(type)) {
                for (String idStr : idList) {
                    Long deptId = Long.parseLong(idStr.trim());
                    Department dept = departmentService.getById(deptId);
                    if (dept != null) {
                        String content = QrCodeUtil.buildSubmitUrl(baseUrl, deptId, null);
                        String fileName = "dept_" + deptId + "_" + dept.getName().replaceAll("[^a-zA-Z0-9]", "") + ".png";
                        qrCodeDataList.add(Map.of(
                                "content", content,
                                "fileName", fileName
                        ));
                    }
                }
            } else if ("ward".equalsIgnoreCase(type)) {
                for (String idStr : idList) {
                    Long wardId = Long.parseLong(idStr.trim());
                    String content = QrCodeUtil.buildSubmitUrl(baseUrl, null, wardId);
                    String fileName = "ward_" + wardId + ".png";
                    qrCodeDataList.add(Map.of(
                            "content", content,
                            "fileName", fileName
                    ));
                }
            }

            if (qrCodeDataList.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"code\":400,\"message\":\"没有找到有效的数据\"}");
                return;
            }

            String zipFileName = type + "_qrcodes_" + System.currentTimeMillis() + ".zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                for (Map<String, String> data : qrCodeDataList) {
                    byte[] qrCodeBytes = QrCodeUtil.generateQrCode(data.get("content"), width, height);
                    zos.putNextEntry(new ZipEntry(data.get("fileName")));
                    zos.write(qrCodeBytes);
                    zos.closeEntry();
                }
            }

            log.info("批量二维码生成成功: type={}, count={}", type, qrCodeDataList.size());

        } catch (NumberFormatException e) {
            log.error("ID格式错误: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            log.error("批量生成二维码失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/custom")
    @Operation(summary = "生成自定义内容二维码", description = "根据自定义内容生成二维码")
    public Result<String> generateCustomQrCode(
            @Parameter(description = "二维码内容") @RequestParam String content,
            @RequestParam(required = false, defaultValue = "300") Integer width,
            @RequestParam(required = false, defaultValue = "300") Integer height) {

        if (!QrCodeUtil.isValidQrCodeContent(content)) {
            return Result.fail("二维码内容无效");
        }

        String base64 = QrCodeUtil.generateQrCodeBase64(content, width, height);
        return Result.success("data:image/png;base64," + base64);
    }

    private void sendQrCodeResponse(HttpServletResponse response, byte[] qrCodeBytes, String fileName) throws IOException {
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLength(qrCodeBytes.length);
        response.getOutputStream().write(qrCodeBytes);
        response.getOutputStream().flush();
    }
}
