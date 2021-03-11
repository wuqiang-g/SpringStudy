package EasyIOC;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

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
