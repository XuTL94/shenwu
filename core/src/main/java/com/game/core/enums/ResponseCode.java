package com.game.core.enums;

/**
 * 公共业务错误码
 */
public enum ResponseCode {

    OK(0, "操作成功"),
    ERROR(500, "系统异常"),
    PARAM_VALID_ERROR(501, "参数校验异常"),
    REQUEST_METHOD_ERROR(502, "请求方法异常"),
    API_REQUEST_ERROR(9001, "接口请求失败")
    ;

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}