package com.iakuil.em.core.handler;

import com.iakuil.em.core.DictEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * MyBatis字典枚举处理器
 *
 * @author Kai
 */
public class DictEnumTypeHandler<E extends Enum<?> & DictEnum> extends BaseTypeHandler<DictEnum> {

    private final Class<E> type;

    public DictEnumTypeHandler(Class<E> type) {
        Objects.requireNonNull(type, "Type argument cannot be null");
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, DictEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.getValue());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return rs.wasNull() ? null : codeOf(value);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        return rs.wasNull() ? null : codeOf(value);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object value = cs.getObject(columnIndex);
        return cs.wasNull() ? null : codeOf(value);
    }

    private E codeOf(Object value) {
        E[] enumConstants = type.getEnumConstants();
        for (E e : enumConstants) {
            if (value.equals(e.getValue())) {
                return e;
            }
        }
        return null;
    }
}
