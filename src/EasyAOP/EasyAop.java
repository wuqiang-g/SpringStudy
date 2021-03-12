package EasyAOP;

import java.lang.reflect.Proxy;

/**
 * @description: AOP类，主要作用是获取代理对象
 * @author: 吴强
 * @date: 2021/3/12 17:09
**/
public class EasyAop {
    public static Object getProxy(Object bean,Advice advice){
        return Proxy.newProxyInstance(EasyAop.class.getClassLoader(),bean.getClass().getInterfaces(),advice);
    }
}
