# SpringStudy
关于Spring IOC/AOP的学习心得以及简单的仿写

## 背景：

由于本人是某大学网络中心java组组长，在和学弟学妹们传授完spring的相关知识之后，看到一脸茫然的他们，仿佛看到自己大一的时候，所以为了帮助学弟学妹更好的了解IOC 以及 AOP 两大原理，并且帮助自己更加深层次的学习spring,决定自己简单的仿写这两个功能。

## 一 ：实现简单的IOC  

回顾做过的项目，记得spring一个比较基础并且重要的功能就是读取xml文件，并且将的得到数据装配成bean，并且方法IOC容器当中储存管理。

由于大学选修课程当中有xml应用编程，所以我对xml文件的读取以及增删改查还是比较了解的，还没有学过的同学可以先去简单了解一下。

### 1.主要步骤：

1. 加载 xml 文件，获取其中的数据
2. 将得到的数据(class)创建成 JavaBean
3. 将Bean注册到IOC容器当中

### 2.开始实现吧

先编写一个IOC容器类：

1. 主要功能有读取xml文件
2. 获取bean的全路径名，通过反射Class.forName（全路径名）获取到bean的class对象
3. 通过class对象（有类的所有信息），实例化bean
4. 利用反射将bean的相关字段的访问权限设置为可访问
5. 然后将读取到的属性填入到bean对象实例当中
6. 最后将单例的bean对象注册到bean容器当中

### 3.代码实现（所有的代码在EasyIOC包下）

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

#### 4. 小结

​	开心！！！ 花了一天的时间，成功实现简单的ioc容器，虽然很简单，但是其实这是一次难忘的学习历程，从看源码，然后脑补基本原理图，再到spring官网查看相关信息，再去论坛社区搜集相关文章，最终独立实现一个简单ioc容器，并且复习了一遍反射。

​	OMG！！！我居然做到了这个之前想象就头皮发麻的仿写，并且可以就这这个小Demo去给学弟学妹上上课啦！



## 二：Spring AOP的简单实现

### 1.前序

由于上次仿写Spring IOC之后，帮学弟学妹讲了一下，收获赞许，说这样一下就好理解多了，于是乎Spring仿写第二弹来了，仿写Spring AOP。

当然：仿写之前还是需要对AOP的原理、机制有一定的了解，比如说代理模式呀，切点呀，切面呀等前缀知识

### 2.前缀知识

#### 通知（Advice）：主要是用来定义方法增强执行的时机以及如何增强

通知类型：

- 前置通知（Before）：在目标方法执行前，执行通知
- 后置通知（After）：在目标方法执行后，执行通知，此时不关系目标方法返回的结果是什么
- 返回通知（After-returning）：在目标方法执行后，执行通知
- 异常通知（After-throwing）：在目标方法抛出异常后执行通知
- 环绕通知（Around）: 目标方法被通知包裹，通知在目标方法执行前和执行后都被会调用

#### 切点（Pointcut）

切点的作用主要是告诉系统我的那些方法需要增强；

通过匹配规则查找合适的连接点（Joinpoint），AOP 会在这些连接点上织入通知

#### 切面（Aspect）

切面其实算是一个概念，他其实就是切点+通知的组合



### 3.实现步骤

(1) 确定都有哪些角色

- 目标对象（被代理对象）
- 目标对象接口（被代理对象实现的接口）
- AOP类（通过代理创建代理对象的主战场）
- 当然还有一些辅助类下面会一一解释

(2)代码实现（所有代码咋EasyAOP包下）

首先：得有一个目标对象实现的接口，这个接口具有需要增强的方法

```java
/**
 * @description: 目标对象实现的接口
 * @author: 吴强
 * @date: 2021/3/12 16:49
**/
public interface Study {
    /**
     * 阅读方法
     */
    void read();
}
```

然后就是目标对象，也就是被代理对象

```java
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
```

然后我们需要定义一个接口，接口中的方法提供增强逻辑，也就是切面逻辑

```java
/**
 * @description: 切面增强逻辑接口，是一个函数式接口
 * @author: 吴强
 * @date: 2021/3/12 16:55
**/
public interface MethodInvocation {
    void invoke();
}
```

有了切面逻辑之后，我们就得定义通知接口,这里继承了InvocationHandler接口，因为我们使用的是jdk的动态代理形式

```java
/**
 * @description: 通知接口
 * @author: 吴强
 * @date: 2021/3/12 16:58
**/
public interface Advice extends InvocationHandler {
}
```

然后就是简单定义具体的某种通知类型，这里我定义的是后置通知类型

```java
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
        //method.invoke就是代理对象调用目标对象的read方法
        Object result = method.invoke(bean,args);
        //在目标方法之后调用通知
        methodInvocation.invoke();
        return result;

    }
}
```

最后当然就是我们的主角EasyAop类了

```java
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

```

来个小小的单元测试吧

```java
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
```

来看看结果

![image-20210312173718450](https://raw.githubusercontent.com/wuqiang-g/picture/main/img/20210312173725.png)



### 4.小结

看看上面的结果，perfect! 这次AOP的仿写也是实现的比较简单，当然其中的思路还是对的，在动手之前需要先去了解代理模式，然后手动写写JDK的动态代理，再来实现AOP就会简单很多。哈哈，又是一次勇敢的尝试，嗯。。。好像还有点小上瘾，下次该仿写哪个小玩意呢？[坏笑]