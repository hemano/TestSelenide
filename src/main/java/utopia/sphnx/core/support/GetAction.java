package utopia.sphnx.core.support;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import utopia.sphnx.core.support.xmlmapping.testcases.Parameter;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by jitendrajogeshwar on 22/05/17.
 */
public class GetAction implements Supplier<Action> {

    private Action action;

    public GetAction(String actionName, List<Parameter> parameters) throws ClassNotFoundException {
        this.action = getActionInstance(actionName + "Action",null,null, new Class[]{List.class},parameters);
    }

//    public GetAction(String actionName, List<Parameter> parameters, WebDriver driver, String controlMap) throws ClassNotFoundException {
//        this.action = getActionInstance(actionName + "Action",null, new Class[]{List.class,WebDriver.class,String.class},parameters,driver,controlMap);
//    }

    public GetAction(String actionName, List<Parameter> parameters, ApplicationContext applicationContext) throws ClassNotFoundException {
        this.action = getActionInstance(actionName + "Action",null, new Class[]{List.class, ApplicationContext.class},parameters,applicationContext);
    }

    public GetAction(String actionName, List<Parameter> parameters, ClassLoader classLoader) throws ClassNotFoundException {
        this.action = getActionInstance(actionName + "Action",classLoader, new Class[]{List.class},parameters);
    }


    @Override
    public Action get() {
        return action;
    }

    private Action getActionInstance(String name, ClassLoader classLoader, Class<?>[] parameterTypes, Object... args) {
        classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
        return this.createActionInstance(name,parameterTypes,classLoader, args);
    }

    private Action createActionInstance(String name, Class<?>[] parameterTypes, ClassLoader classLoader, Object[] args) {
        try {
            String fullName = getActionClass(name);
            Class<?> instanceClass = ClassUtils.forName(fullName, classLoader);
            Assert.isAssignable(Action.class, instanceClass);
            Constructor<?> constructor = instanceClass.getDeclaredConstructor(parameterTypes);
            return BeanUtils.instantiateClass((Constructor<Action>) constructor, args);

        } catch (Throwable var12) {
            throw new IllegalArgumentException("Cannot instantiate " + Action.class + " : " + name, var12);
        }
    }

    private String getActionClass(String name){
        BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
        ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr);

        TypeFilter tf = new AssignableTypeFilter(Action.class);
        s.addIncludeFilter(tf);
        s.scan("actions");
        int dotIndex = name.lastIndexOf(46);
        name = dotIndex != -1?name.substring(dotIndex + 1):name;
        String className = Introspector.decapitalize(name);
        return  bdr.getBeanDefinition(className).getBeanClassName();

    }

}
