package com.hkuh.complaint.handler;

import com.hkuh.complaint.annotation.EncryptedField;
import com.hkuh.complaint.exception.DecryptException;
import com.hkuh.complaint.exception.EncryptException;
import com.hkuh.complaint.util.AesEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class EncryptedTypeHandler extends BaseTypeHandler<String> {

    private static AesEncryptUtil aesEncryptUtil;

    @Autowired
    public void setAesEncryptUtil(AesEncryptUtil aesEncryptUtil) {
        EncryptedTypeHandler.aesEncryptUtil = aesEncryptUtil;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            if (parameter == null) {
                ps.setString(i, null);
                return;
            }

            String fieldName = getFieldName(ps, i);
            String encrypted = aesEncryptUtil.encryptField(fieldName, parameter);
            ps.setString(i, encrypted);

        } catch (EncryptException e) {
            log.error("加密字段设置失败：{}", e.getMessage());
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decryptValue(value, columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        String columnName = "column_" + columnIndex;
        return decryptValue(value, columnName);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        String columnName = "column_" + columnIndex;
        return decryptValue(value, columnName);
    }

    private String decryptValue(String value, String columnName) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            if (!aesEncryptUtil.isEncrypted(value)) {
                return value;
            }
            return aesEncryptUtil.decryptField(columnName, value);
        } catch (DecryptException e) {
            log.error("解密字段[{}]失败，返回原始值：{}", columnName, e.getMessage());
            return value;
        }
    }

    private String getFieldName(PreparedStatement ps, int parameterIndex) {
        try {
            Field field = ps.getClass().getDeclaredField("parameters");
            field.setAccessible(true);
            Object parameters = field.get(ps);
            if (parameters instanceof Object[]) {
                Object param = ((Object[]) parameters)[parameterIndex - 1];
                if (param != null) {
                    Field metaDataField = param.getClass().getDeclaredField("metaData");
                    metaDataField.setAccessible(true);
                    Object metaData = metaDataField.get(param);
                    if (metaData != null) {
                        Field columnNameField = metaData.getClass().getDeclaredField("column");
                        columnNameField.setAccessible(true);
                        Object columnName = columnNameField.get(metaData);
                        if (columnName != null) {
                            return columnName.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取字段名失败：{}", e.getMessage());
        }
        return "encrypted_field_" + parameterIndex;
    }

    public static String encryptValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            return aesEncryptUtil.encrypt(value);
        } catch (EncryptException e) {
            log.error("加密值失败：{}", e.getMessage());
            return value;
        }
    }

    public static String decryptValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            return aesEncryptUtil.decrypt(value);
        } catch (DecryptException e) {
            log.error("解密值失败：{}", e.getMessage());
            return value;
        }
    }
}
