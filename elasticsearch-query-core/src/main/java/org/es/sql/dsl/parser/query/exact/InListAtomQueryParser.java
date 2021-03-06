package org.es.sql.dsl.parser.query.exact;

import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import org.apache.commons.collections.CollectionUtils;
import org.es.sql.dsl.bean.AtomQuery;
import org.es.sql.dsl.enums.SQLConditionOperator;
import org.es.sql.dsl.exception.ElasticSql2DslException;
import org.es.sql.dsl.helper.ElasticSqlArgTransferHelper;
import org.es.sql.dsl.listener.ParseActionListener;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class InListAtomQueryParser extends AbstractAtomExactQueryParser {

    public InListAtomQueryParser(ParseActionListener parseActionListener) {
        super(parseActionListener);
    }

    public AtomQuery parseInListQuery(SQLInListExpr inListQueryExpr, String queryAs, Object[] sqlArgs) {
        if (CollectionUtils.isEmpty(inListQueryExpr.getTargetList())) {
            throw new ElasticSql2DslException("[syntax error] In list expr target list cannot be blank");
        }

        Object[] targetInList = ElasticSqlArgTransferHelper.transferSqlArgs(inListQueryExpr.getTargetList(), sqlArgs);
        SQLConditionOperator operator = inListQueryExpr.isNot() ? SQLConditionOperator.NotIn : SQLConditionOperator.In;

        return parseCondition(inListQueryExpr.getExpr(), operator, targetInList, queryAs, new IConditionExactQueryBuilder() {
            @Override
            public QueryBuilder buildQuery(String queryFieldName, SQLConditionOperator operator, Object[] rightParamValues) {
                if (SQLConditionOperator.NotIn == operator) {
                    return QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery(queryFieldName, rightParamValues));
                }
                else {
                    return QueryBuilders.termsQuery(queryFieldName, rightParamValues);
                }
            }
        });
    }
}
