package org.aoju.bus.pager.dialect.rowbounds;

import org.aoju.bus.pager.dialect.AbstractRowBoundsDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;

/**
 * informix 基于 RowBounds 的分页
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InformixRowBoundsDialect extends AbstractRowBoundsDialect {

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 40);
        sqlBuilder.append("SELECT ");
        if (rowBounds.getOffset() > 0) {
            sqlBuilder.append(" SKIP ");
            sqlBuilder.append(rowBounds.getOffset());
            pageKey.update(rowBounds.getOffset());
        }
        if (rowBounds.getLimit() > 0) {
            sqlBuilder.append(" FIRST ");
            sqlBuilder.append(rowBounds.getLimit());
            pageKey.update(rowBounds.getLimit());
        }
        sqlBuilder.append(" * FROM ( ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" ) TEMP_T");
        return sqlBuilder.toString();
    }

}
