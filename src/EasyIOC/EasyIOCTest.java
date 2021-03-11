package EasyIOC;

import EasyIOC.entity.Student;
import EasyIOC.entity.Teacher;
import org.junit.Test;

/**
 * @description: 单元测试getBean方法
 * @author: 吴强
 * @date: 2021/3/11 17:01
**/
public class EasyIOCTest {
    @Test
    public void getBean() throws Exception {
        //获取xml文件的URL
        String Url = EasyIOCDemo.class.getClassLoader().getResource("EasyIOC/ioc.xml").getFile();

        System.out.println(Url);

        EasyIOCDemo IOC = new EasyIOCDemo(Url);
        Student student = (Student) IOC.getBean("Student");
        System.out.println(student);
        Teacher teacher = (Teacher) IOC.getBean("Teacher");
        System.out.println(teacher);
    }
}
