package com.iakuil.em.interceptor;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.iakuil.em.util.ReflectUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 逻辑删除SQL语句处理
 *
 * @author Kai
 */
public class DeletedDynamicSqlSource implements SqlSource {

    private final DbType dbType;
    private final SqlSource source;

    private final String logicDeleteField;
    private final String logicDeleteValue;
    private final String logicNotDeleteValue;

    public DeletedDynamicSqlSource(DbType dbType, SqlSource source, String logicDeleteField, String logicDeleteValue, String logicNotDeleteValue) {
        this.dbType = dbType;
        this.source = source;
        this.logicDeleteField = logicDeleteField;
        this.logicDeleteValue = logicDeleteValue;
        this.logicNotDeleteValue = logicNotDeleteValue;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql boundSql = source.getBoundSql(parameterObject);
        ReflectUtils.setFieldValue(boundSql, "sql", logicallyDeleted(boundSql.getSql()));
        return boundSql;
    }

    public String logicallyDeleted(String sql) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        List<SQLStatement> stmtList = parser.parseStatementList();
        SQLStatement stmt = stmtList.get(0);

        // 删除语句
        if (stmt instanceof SQLDeleteStatement) {
            SQLDeleteStatement sstmt = (SQLDeleteStatement) stmt;
            return deleteToUpdate(sstmt);
        }
        // 查询语句
        else if (stmt instanceof SQLSelectStatement) {
            SQLSelectStatement sstmt = (SQLSelectStatement) stmt;
            return selectAddWhere(sstmt);
        }
        return sql;
    }

    /**
     * 删除语句变更新语句
     */
    private String deleteToUpdate(SQLDeleteStatement deleteStatement) {
        StringBuffer buf = new StringBuffer();
        buf.append("UPDATE ");

        deleteStatement.getTableSource().output(buf);
        buf.append(" SET ");
        {
            List<String> list = getAlias(deleteStatement.getTableSource());
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    if (i != 0) {
                        buf.append(", ");
                    }
                    String as = list.get(i);
                    StringBuffer whereSql = new StringBuffer();
                    if (as != null && as.length() > 0) {
                        whereSql.append(as);
                        whereSql.append(".");
                    }
                    whereSql.append(logicDeleteField);
                    whereSql.append(" = ");
                    whereSql.append(logicDeleteValue);
                    buf.append(whereSql);
                }
            }

        }

        if (deleteStatement.getWhere() != null) {
            buf.append(" WHERE ");
            SQLExpr opExpr = deleteStatement.getWhere();
            buf.append(SQLUtils.toSQLString(opExpr));
        }
        return buf.toString();
    }

    /**
     * Select语句加上删除where
     */
    private String selectAddWhere(SQLSelectStatement sstmt) {

        SQLSelect sqlselect = sstmt.getSelect();
        SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();

        if (query != null && query.getFrom() != null) {
            List<String> list = getAlias(query.getFrom());
            if (list != null) {
                for (String as : list) {
                    StringBuffer whereSql = new StringBuffer();
                    if (as != null && as.length() > 0) {
                        whereSql.append(as);
                        whereSql.append(".");
                    }
                    whereSql.append(logicDeleteField);
                    whereSql.append(" = ");
                    whereSql.append(logicNotDeleteValue);
                    SQLExpr expr = new SQLExprParser(whereSql.toString(), dbType).expr();
                    query.addWhere(expr);
                }
            }
        }
        return sstmt.toString();
    }

    /**
     * 查询表alias，没有返回NUll
     */
    private List<String> getAlias(SQLTableSource tableFrom) {
        StringBuffer buffer = new StringBuffer();
        tableFrom.accept(new SQLASTOutputVisitor(buffer));
        String tables = buffer.toString();

        tables = tables.split("\n")[0];

        List<String> list = new ArrayList<String>();
        if (buffer.length() <= 0) {
            return null;
        }

        String[] tabs = tables.split(", ");
        for (String tab : tabs) {
            String[] names = tab.split(" ");
            if (names.length > 1) {
                list.add(names[1]);
            } else {
                list.add(null);
            }
        }
        return list;
    }
}
