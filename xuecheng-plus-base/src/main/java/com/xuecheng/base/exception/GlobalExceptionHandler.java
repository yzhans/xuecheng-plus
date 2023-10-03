package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/4 14:33
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    //处理XueChengPlusException异常 此类异常是程序员主动抛出的,可预知异常
    @ResponseBody//返回json
    @ExceptionHandler(XueChangException.class)//此方法捕获XueChangException异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//返回状态码
    public RestErrorResponse handleXueChengPlusException(XueChangException e) {
        String eMessage = e.getMessage();
        //打印异常
        log.error("捕获异常：{}",eMessage);
        e.printStackTrace();
        return new RestErrorResponse(eMessage);
    }

    //捕获MethodArgumentNotValidException异常
    @ResponseBody//返回json
    @ExceptionHandler(MethodArgumentNotValidException.class)//此方法捕获MethodArgumentNotValidException异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//返回状态码
    public RestErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult eBindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = eBindingResult.getFieldErrors();
        StringBuffer stringBuffer = new StringBuffer();
        fieldErrors.forEach(error ->{
            stringBuffer.append(error.getDefaultMessage()).append(",");
        });
        return new RestErrorResponse(stringBuffer.toString());
    }

    //捕获不可预知的异常 Exception
    @ResponseBody//返回json
    @ExceptionHandler(Exception.class)//此方法捕获XueChangException异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//返回状态码
    public RestErrorResponse handleException(Exception e) {
        String eMessage = e.getMessage();
        //打印异常
        log.error("捕获异常：{}", eMessage);
        if (e.getMessage().equals("不允许访问")) {
            return new RestErrorResponse("你没有权限操作此功能");
        }

        e.printStackTrace();
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

}
