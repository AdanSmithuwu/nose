package com.comercialvalerio.infrastructure.transaction;

import com.comercialvalerio.common.transaction.Transactional;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@Transactional
@Priority(Interceptor.Priority.APPLICATION)
public class TransactionalInterceptor {
    @AroundInvoke
    public Object manage(InvocationContext ctx) throws Exception {
        if (TransactionManager.isActive()) {
            return ctx.proceed();
        }
        try (TransactionManager.Tx tx = TransactionManager.begin()) {
            Object result = ctx.proceed();
            tx.commit();
            return result;
        }
    }
}
