# SpringStudy
关于Spring IOC/AOP的学习心得以及简单的仿写

## 背景：

由于本人是某大学网络中心java组组长，在和学弟学妹们传授完spring的相关知识之后，看到一脸茫然的他们，仿佛看到自己大一的时候，所以为了帮助学弟学妹更好的了解IOC 以及 AOP 两大原理，并且帮助自己更加深层次的学习spring,决定自己简单的仿写这两个功能。

## 一 ：实现简单的IOC  2021年3月11日16:01:28

回顾做过的项目，记得spring一个比较基础并且重要的功能就是读取xml文件，并且将的得到数据装配成bean，并且方法IOC容器当中储存管理。

由于大学选修课程当中有xml应用编程，所以我对xml文件的读取以及增删改查还是比较了解的，还没有学过的同学可以先去简单了解一下。

### 主要步骤：

1. 加载 xml 文件，获取其中的数据
2. 将得到的数据(class)创建成 JavaBean
3. 将Bean注册到IOC容器当中

### 开始实现吧

先编写一个IOC容器类：

1. 主要功能有读取xml文件
2. 获取bean的全路径名，通过反射Class.forName（全路径名）获取到bean的class对象
3. 通过class对象（有类的所有信息），实例化bean
4. 利用反射将bean的相关字段的访问权限设置为可访问
5. 然后将读取到的属性填入到bean对象实例当中
6. 最后将单例的bean对象注册到bean容器当中

### 代码实现（所有的代码在EasyIOC包下）

#### IOC类编写

```java
/**
 * @description: IOC容器类
 * @author: 吴强
 * @date: 2021/3/11 16:14
**/
public class EasyIOCDemo {
    /**
     * Ioc容器
     */
    HashMap<String,Object> beanMap = new HashMap<>();

    public EasyIOCDemo(String BeanId) throws Exception{
        load(BeanId);
    }

    /**
     * 加载xml文件
     * @param XMLUrl
     */
    private void load(String XMLUrl) throws Exception{
        //加载xml文件
        FileInputStream fileInputStream = new FileInputStream(XMLUrl);
        //创建工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document parse = documentBuilder.parse(fileInputStream);
        //获取xml节点树的
        Element documentRoot = parse.getDocumentElement();
        NodeList nodes  = documentRoot.getChildNodes();
        //加载注册bean
        loadBean(nodes);
    }

    /**
     * 根据name获取bean
     * @param name
     * @return
     */
    public Object getBean(String name) {
        Object bean = beanMap.get(name);
        if (bean == null) {
            throw new IllegalArgumentException("No bean with name " + name);
        }
        return bean;
    }

    /**
     * 根据获得的xml的nodes遍历出bean标签，并且创建bean，然后注册进beanMap
     * @param nodes
     * @throws Exception
     */
    private void loadBean(NodeList nodes)throws Exception{
        // 遍历 <bean> 标签
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                String id = ele.getAttribute("id");
                String className = ele.getAttribute("class");

                // 加载 beanClass
                Class beanClass = null;
                try {
                    //这里利用了反射反射的一种实现方式获取Bean的Class对象
                    beanClass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return;
                }

                // 创建 bean
                Object bean = beanClass.newInstance();

                // 遍历 <property> 标签
                NodeList propertyNodes = ele.getElementsByTagName("property");
                for (int j = 0; j < propertyNodes.getLength(); j++) {
                    Node propertyNode = propertyNodes.item(j);
                    if (propertyNode instanceof Element) {
                        Element propertyElement = (Element) propertyNode;
                        String name = propertyElement.getAttribute("name");
                        String value = propertyElement.getAttribute("value");

                        // 利用反射将 bean 相关字段访问权限设为可访问
                        Field declaredField = bean.getClass().getDeclaredField(name);
                        declaredField.setAccessible(true);

                        if (value != null && value.length() > 0) {
                            // 将属性值填充到相关字段中
                            declaredField.set(bean, value);
                        } else {
                            String ref = propertyElement.getAttribute("ref");
                            if (ref == null || ref.length() == 0) {
                                throw new IllegalArgumentException("ref config error");
                            }
                            // 将引用填充到相关字段中
                            declaredField.set(bean,getBean(ref));
                        }
                        // 将 bean 注册到 bean 容器中
                        registerBean(id, bean);
                    }
                }
            }
        }
    }

    /**
     * 注册bean到IOC容器中
     * @param id
     * @param bean
     */
    private void registerBean(String id, Object bean) {
        beanMap.put(id, bean);
    }
}
```

#### 两个Bean类Student & Teacher

```java
package EasyIOC.entity;
/**
 * @description: Teacher的bean
 * @author: 吴强
 * @date: 2021/3/11 16:52
**/
public class Teacher {
    //名字
    private String name;
    //性别
    private String sex;
    //体重
    private String width;
    ...
}
```

```java
package EasyIOC.entity;
/**
 * @description: Student 的Bean
 * @author: 吴强
 * @date: 2021/3/11 16:51
**/
public class Student {
    //名字
    private String name;
    //性别
    private String sex;
    //身高
    private String height;
    ...
}
```

#### ioc.xml

```xml
<beans>
    <bean id="Student" class="EasyIOC.entity.Student">
        <property name="name" value="xiaowu" />
        <property name="sex" value="man" />
        <property name="height" value="175" />
    </bean>

    <bean id="Teacher" class="EasyIOC.entity.Teacher">
        <property name="name" value="hongtao"/>
        <property name="sex" value="man"/>
        <property name="width" value="65"/>
    </bean>
</beans>
```

#### 单元测试类

```java
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
```

#### 最终结果

![image-20210311194835633](https://raw.githubusercontent.com/wuqiang-g/picture/main/img/20210311194835.png)

#### 小结

​	开心！！！ 花了一天的时间，成功实现简单的ioc容器，虽然很简单，但是其实这是一次难忘的学习历程，从看源码，然后脑补基本原理图，再到spring官网查看相关信息，再去论坛社区搜集相关文章，最终独立实现一个简单ioc容器，并且复习了一遍反射。

​	OMG！！！我居然做到了这个之前想象就头皮发麻的仿写，并且可以就这这个小Demo去给学弟学妹上上课啦！

