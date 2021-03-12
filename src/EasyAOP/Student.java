package EasyAOP;
/**
 * @description: 目标对象学生类，实现了学习接口
 * @author: 吴强
 * @date: 2021/3/12 16:52
**/
public class Student implements Study{
    @Override
    public void read() {
        System.out.println("具有汉语阅读的能力！");
    }
}
