package smartlims.testresultmgtsvc.enums;

import lombok.Getter;

@Getter
public enum ResultEnums {
    
    GET_DATA_FAIL("获取数据失败"),
    LOGIN_INFO_GET_FAIL("登录用户获取信息不正确"),
    LOGIN_BY_NOT_FOUND("登录用户没有注册"),
    LOGIN_SUCCESS("用户登录成功"),
    GET_DATA_EXCEPTION("获取数据异常");

    private String message;

    ResultEnums(String message) {
        this.message = message;
    }
}