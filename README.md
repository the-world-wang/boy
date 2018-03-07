感谢`lombok`, 我根据他的原理写了这个项目。

## Get Started
```java
@Data
@Constantable
public class User {

    private String name;
    private String firstName;
    private String lastName;
    
}
```
以上代码会生成如下 `ClassName`+ Constants的类
```java
public final class UserConstants {
    public static final String NAME = "name";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";

    public UserConstants() {
    }
}
```