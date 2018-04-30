package utopia.sphnx.core.listeners;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITest;
import org.testng.ITestContext;
import utopia.sphnx.core.support.xmlmapping.testcases.TestCase;

import java.util.List;

public class TestSortInterceptor implements IMethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(utopia.sphnx.core.listeners.TestSortInterceptor.class);

    private static final Object lockObject = new Object();

    public TestSortInterceptor() {
        super();
    }

    @Override
    public synchronized List<IMethodInstance> intercept(List<IMethodInstance> list, ITestContext iTestContext) {
        List<IMethodInstance> newList = Lists.newLinkedList();

        List<Object> tests = (List<Object>)iTestContext.getAttribute("tests");

        for (Object etc : tests) {
            String name = ((TestCase)etc).getTestCase();

            list.stream()
                    .filter(mi -> ((ITest) mi.getInstance()).getTestName().equalsIgnoreCase(name))
                    .findFirst()
                    .ifPresent(newList::add);

        }

        return newList;
    }
}