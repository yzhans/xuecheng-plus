package com.xuecheng.base.exception;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/4 14:10
 */
public class XueChangException extends RuntimeException {

    private String message;

    public XueChangException() {

    }

    public XueChangException(String message) {
        super(message);
        this.message = message;
    }

    public static void cast(String message) {
        throw new XueChangException(message);
    }

    public static void cast(CommonError commonError) {
        throw new XueChangException(commonError.getErrMessage());
    }
}
