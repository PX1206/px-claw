package com.claw.common.exception;

import com.claw.common.api.ApiCode;

/**
 * 业务异常
 */
public class BusinessException extends BaseException {
	private static final long serialVersionUID = -2303357122330162359L;

	public BusinessException(String message) {
        super(500, message);
    }

    public BusinessException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ApiCode apiCode) {
        super(apiCode);
    }

}
