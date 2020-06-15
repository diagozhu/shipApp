package com.diago.ship;

/**
 * 该类存放所有错误的句柄，代码，描述.
 *
 * @author Diago diago@yeah.net
 */
public enum Err {

    eOk(0, "操作成功"),
    eEmptyResultSet(-1403, "未找到数据"),
    eArgument(-10010, "参数验证错误"),
    eReturn(-10020, "返回错误"),
    eMissArgument(-10030, "缺少参数"),
    eBadArgument(-10040, "错误的参数"),

    //认证授权错误
    eLoginFailed(-15010, "登录失败"),
    eNoSessionIdFound(-15020, "未找到SESSIONID"),
    eCrcWrong(-15030, "校验码出错"),
    eNoPermissionChangePassword(-15040, "无权修改密码"),
    ePermission(-15050, "无此权限"),

    //技术错误
    eUnsupportedEncoding(-16010, "URL编码不支持"),

    //业务错误
    eNoSession(-17010, "用户不在线"),

    //缺省错误编码
    eStandard(-10000, "操作失败");

    public Integer errcod;
    public String errmsg;

    private Err(int errcod, String errmsg) {
        this.errcod = errcod;
        this.errmsg = errmsg;
    }

    public int getCode() {
        return errcod;
    }

    public String getMessage() {
        return errmsg;
    }

    public static Err getErrorByCode(Integer errcod) {
        Err defaultError = Err.eStandard;
        for (Err error : Err.values()) {
            if (error.errcod.equals(errcod)) {
                return error;
            }
        }
        return defaultError;
    }

    public static String getMessageByCode(Integer code) {
        return getErrorByCode(code).errmsg;
    }

}