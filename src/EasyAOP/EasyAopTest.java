package EasyAOP;
import org.junit.Test;
/**
 * @description: Aop单元测试类
 * @author: 吴强
 * @date: 2021/3/12 17:14
**/
public class EasyAopTest {
    @Test
    public void getProxy(){
        //目标对象
        Student student = new Student();
        //切面逻辑对象,
        MethodInvocation strentMethod = () -> {
            //在这里书写增强逻辑，因为MethodInvocation是函数式接口，所以用来lambda表达式
            System.out.println("阅读之后，具有理解的能力");
        };
        //后置通知对象
        AfterAdvice afterAdvice = new AfterAdvice(student, strentMethod);
        //生成代理对象
        Study proxy = (Study) EasyAop.getProxy(student, afterAdvice);
        proxy.read();
    }
}
