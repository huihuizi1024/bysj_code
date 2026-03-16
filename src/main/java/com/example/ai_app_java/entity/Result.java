package com.example.ai_app_java.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor//生成全参构造函数
public class Result {
    private int code;   //状态码：200成功，400失败
    private String status;//状态信息描述：“success”或“fail”
    private String msg; //提示消息
    private Object data;//携带的数据
}
