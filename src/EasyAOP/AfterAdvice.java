package EasyAOP;

import java.lang.reflect.Method;

/**
 * @description: 后置通知类，实现通知接口，其他通知也和这个差不多
 * @author: 吴强
 * @date: 2021/3/12 16:59
**/
public class AfterAdvice implements Advice{
    /**
     * 目标对象
     */
    private Object bean;
    /**
     * 切面逻辑
     */
    private MethodInvocation methodInvocation;

    public AfterAdvice(Object bean,MethodInvocation methodInvocation) {
        this.bean = bean;
        this.methodInvocation = methodInvocation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(bean,args);
        //在目标方法之后调用通知
        methodInvocation.invoke();
        return result;

    }
}
