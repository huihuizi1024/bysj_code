package com.example.ai_app_java.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor//生成全参构造函数
public class Result {
    private int code;   //状态码：200成功，400失败
    private String status;//状态信息描述：“success”或“fail”
    private String msg; //提示消息
    private Object data;//携带的数据
    //添加success静态方法
    public static Result success(String msg, Object data) {
        return new Result(200, "success", msg, data);
    }
    //添加fail静态方法
    public static Result fail(Integer code, String msg) {
        return new Result(code, "fail",msg, null);
    }
}
