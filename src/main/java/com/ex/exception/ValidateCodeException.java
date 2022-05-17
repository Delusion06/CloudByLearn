package com.ex.exception;

import org.springframework.security.core.AuthenticationException;

public class ValidateCodeException extends AuthenticationException {

    private static final long serialVersionUID = 979417670957380699L;

    public ValidateCodeException(String msg) {
        super(msg);
    }
}
