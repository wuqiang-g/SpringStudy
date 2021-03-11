package EasyIOC.entity;
/**
 * @description: Teacher的bean
 * @author: 吴强
 * @date: 2021/3/11 16:52
**/
public class Teacher {
    private String name;
    private String sex;
    private String width;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", width='" + width + '\'' +
                '}';
    }
}
