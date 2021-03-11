package EasyIOC.entity;
/**
 * @description: Student 的Bean
 * @author: 吴强
 * @date: 2021/3/11 16:51
**/
public class Student {
    private String name;
    private String sex;
    private String height;

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

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", height='" + height + '\'' +
                '}';
    }
}
