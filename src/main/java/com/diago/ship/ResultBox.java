package com.diago.ship;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全自动打包，按照标准格式输出。
 *
 * @author Diago diago@yeah.net
 */
@Slf4j
public class ResultBox {
    private Integer errCod = null;
    private String errMsg = null;
    private Integer total = null;
    private List<Object> rows = null;

    /**
     * 用于insert语句，成功后返回插入的数据，以及last_insert_id
     *
     * @param affectedRowCount
     * @param lastInsertId
     * @return
     */
    public static ResultBox buildBy(Integer affectedRowCount, Integer lastInsertId) {
        ResultBox rf = buildByInt(affectedRowCount);

        // 插入成功则返回刚已插入的id。
        if (null != rf.getErrCod() && rf.getErrCod() == 0) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("id", lastInsertId);

            List<Object> rows = new ArrayList<Object>();
            rows.add(row);

            rf.setRows(rows);
            rf.setTotal(affectedRowCount); // 被影响的行数
        }

        return rf;
    }

    /**
     * 用于count语句，成功后返回总行数.
     *
     * @param count
     * @return
     */
    public static ResultBox buildByCount(Integer count) {
        if (count < 0) {
            // 失败
            return ResultBox.newOneByCode(-10000);
        } else {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("count", count);

            List<Object> rows = new ArrayList<Object>();
            rows.add(row);

            ResultBox rf = ResultBox.newOneByCode(0);
            rf.setRows(rows);

            return rf;
        }
    }

    /**
     * 将input（List, Int, BindingResult, Other）转换为标准输出格式.
     *
     * @param input 是查询结果
     * @return 标准化的输出
     */
    @SuppressWarnings("unchecked")
    public static ResultBox buildBy(Object input) {
        if (input instanceof List<?>) {
            return buildByRows((List<Object>) input);
        } else if (input instanceof Integer) {
            return buildByInt((Integer) input);
        } else if (input instanceof BindingResult) {
            return buildArgumentError((BindingResult) input);
        } else {
            return buildByRow(input);
        }
    }

    /**
     * 将Int转换为标准输出格式
     *
     * @param result Integer
     * @return 标准化的输出
     */
    public static ResultBox buildByInt(Integer result) {
        if (result <= 0) {
            // 失败
            return ResultBox.newOneByCode(-10000);
        } else {
            ResultBox rf = ResultBox.newOneByCode(0);
            return rf;
        }
    }

    /**
     * 将List转换为JSON输出
     *
     * @param rows ResultSet
     * @return JSON标准化的输出
     */
    public static ResultBox buildByRows(List<Object> rows) {
        if (rows.size() <= 0) {
            // 返回空结果集
            return ResultBox.getNoRowFound();
        } else {
            ResultBox rf = ResultBox.newOneByCode(0);
            rf.setTotal(rows.size());
            rf.setRows(rows);
            return rf;
        }
    }

    /**
     * 将Object转换为JSON输出
     *
     * @param row entry
     * @return JSON标准化的输出
     */
    public static ResultBox buildByRow(Object row) {
        if (null == row) {
            // 返回空结果集
            return ResultBox.getNoRowFound();
        } else {
            ResultBox rf = ResultBox.newOneByCode(0);
            rf.setTotal(1);
            rf.getRows().add(row);
            return rf;
        }
    }

    /**
     * 将Controller的参数错误集合打包，以ResultBox格式输出。
     *
     * @param bindingResult
     * @return ResultBox
     */
    public static ResultBox buildArgumentError(BindingResult bindingResult) {
        // 集合错误信息
        String errMsgs = "";
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errMsgs += fieldError.getField() + ":" + fieldError.getDefaultMessage() + "^|";
        }
        errMsgs = errMsgs.substring(0, errMsgs.length() - 2);
        // 报错
        ResultBox rf = ResultBox.newOneByCode(Err.eArgument.getCode());
        rf.setErrMsg(errMsgs);
        log.warn("Ctl Arg Errors: {} / {}", bindingResult.getErrorCount(), errMsgs);
        return rf;
    }

    /**
     * 取结果格式 - 数据没找到。
     *
     * @return
     */
    public static ResultBox getNoRowFound() {
        return new ResultBox(Err.eEmptyResultSet.getCode(), Err.eEmptyResultSet.getMessage(), 0,
                new ArrayList<Object>());
    }

    ;

    /**
     * 取结果格式 - 通过errcod。
     *
     * @return
     */
    public static ResultBox newOneByCode(int errcod) {
        return new ResultBox(errcod, Err.getMessageByCode(errcod), 0, new ArrayList<Object>());
    }

    ;

    public ResultBox() {
        super();
    }

    public ResultBox(Integer errCod, String errMsg, Integer total, List<Object> rows) {
        this.setErrCod(errCod);
        this.setErrMsg(errMsg);
        this.setTotal(total);
        this.setRows(rows);
    }

    public Integer getErrCod() {
        return errCod;
    }

    public void setErrCod(Integer errCod) {
        this.errCod = errCod;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Object> getRows() {
        return rows;
    }

    public void setRows(List<Object> rows) {
        this.rows = rows;
    }
}
