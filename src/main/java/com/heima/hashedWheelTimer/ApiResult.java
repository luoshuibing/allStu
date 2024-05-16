package com.heima.hashedWheelTimer;

import lombok.Data;

import java.io.Serializable;

/**
 * @author LoneWalker
 */
@Data
public class ApiResult<T> implements Serializable {
    private static final long serialVersionUID = 411731814484355577L;
    /**
     * 状态码
     */
    private int code;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 相关数据
     */
    private T data;

    public String toString() {
        return "ApiResult(code=" + this.getCode() + ", msg=" + this.getMsg() + ", data=" + this.getData() + ")";
    }

    /**
     * 构造器 自定义响应码与提示信息
     * @param code    响应码
     * @param message 提示信息
     */
    private ApiResult(int code,String message){
        this.code = code;
        this.msg = message;
    }

    /**
     * 构造器 自定义响应码、提示信息、数据
     * @param code    响应码
     * @param message 提示信息
     * @param data    返回数据
     */
    private ApiResult(int code,String message,T data){
        this(code,message);
        this.data = data;
    }

    /**
     * 成功构造器  无返回数据
     */
    public static <T> ApiResult<T> ok(){
        return new ApiResult<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage());
    }

    /**
     * 成功构造器 自定义提示信息 无返回数据
     * @param message 提示信息
     */
    public static <T> ApiResult<T> success(String message){
        return new ApiResult<>(ResultCodeEnum.SUCCESS.getCode(), message);
    }

    /**
     * 成功构造器  有返回数据
     */
    public static <T> ApiResult<T> success(T data){
        return new ApiResult<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(),data);
    }

    /**
     * 失败构造器  无返回数据
     */
    public static <T> ApiResult<T> fail(){
        return new ApiResult<>(ResultCodeEnum.FAIL.getCode(), ResultCodeEnum.FAIL.getMessage());
    }

    /**
     * 失败构造器 自定义提示信息 无返回数据
     * @param message 提示信息
     */
    public static <T> ApiResult<T> fail(String message){
        return new ApiResult<>(ResultCodeEnum.FAIL.getCode(), message);
    }

    /**
     * 失败构造器  有返回数据
     */
    public static <T> ApiResult<T> fail(T data){
        return new ApiResult<>(ResultCodeEnum.FAIL.getCode(), ResultCodeEnum.FAIL.getMessage(),data);
    }
}
