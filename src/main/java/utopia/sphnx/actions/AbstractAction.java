package utopia.sphnx.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.core.AttributeAccessorSupport;
import utopia.sphnx.core.support.Action;
import utopia.sphnx.core.support.xmlmapping.controlmap.Control;
import utopia.sphnx.core.support.xmlmapping.testcases.Parameter;
import utopia.sphnx.dataconversion.parsing.Parser;

import java.util.List;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by jitendrajogeshwar on 22/05/17.
 */
public abstract class AbstractAction extends AttributeAccessorSupport implements Action {

    protected WebDriver webDriver;

    protected By locator;

    protected String controlType;

    protected String controlDescriptor;

    protected ApplicationContext applicationContext;

    protected List<Parameter> parameters;

    private static String ABSTRACT_ACTION = utopia.sphnx.actions.AbstractAction.class.getCanonicalName();

    public AbstractAction(List<Parameter> parameters, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.parameters = parameters;
        this.webDriver = (WebDriver) applicationContext.getBean("createWebDriverInstance");
        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
        try {
            parameters.forEach(p -> setAttribute(p.getName(), p.getValue()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public AbstractAction(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.webDriver = (WebDriver) applicationContext.getBean("createWebDriverInstance");
    }

    public AbstractAction(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    protected By getLocator(String fullName) {
        //clear the locator as it holds value from the previous call to getLocator
    this.locator = null;
        this.webDriver = (WebDriver) applicationContext.getBean("createWebDriverInstance");
        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
//            try {
        controls.forEach(control -> {
            String controlFullName = control.getContext() + "." + control.getLogicalName();
            if (controlFullName.equalsIgnoreCase(fullName)) {
                controlType = control.getObjectType();
                Parser parser = new Parser();
                controlDescriptor = parser.replaceDataReferenceInString(control.getDescriptor(), false);
                this.locator = (By) applicationContext.getBean("lookUp", control.getDescriptor());

            }
        });
        //            throw new DriverException(ABSTRACT_ACTION, "Locator: " + fullName + " was not found in the UIMap!");
        if (this.locator == null) {
            LOGNREPORT.sphnxError(ABSTRACT_ACTION, "Locator: " + fullName + " was not found in the UIMap!");
            throw new RuntimeException("Locator: " + fullName + " was not found in the UIMap!");
        }
        return this.locator;


//            }
//            catch(Exception e){
//                LOGNREPORT.sphnxError(ABSTRACT_ACTION, "Locator: " + locator + " was not found in the UIMap!");
//                return null;
//            }
    }

    protected String getLocatorAsString(String fullName) {
        this.webDriver = (WebDriver) applicationContext.getBean("createWebDriverInstance");
        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
        String controlDescriptor = null;

        for(Control control : controls) {
            String controlFullName = control.getContext() + "." + control.getLogicalName();
            if (controlFullName.equalsIgnoreCase(fullName)) {
                controlDescriptor = control.getDescriptor();
                break;
            }
        }
        if(controlDescriptor == null) {
            LOGNREPORT.sphnxError(ABSTRACT_ACTION, "Locator: " + fullName + " was not found in the UIMap!");
            throw new RuntimeException("Locator: " + fullName + " was not found in the UIMap!");
        } else {
            controlDescriptor = controlDescriptor.substring(controlDescriptor.indexOf("=") + 1, controlDescriptor.length());
            return controlDescriptor.trim();
        }


//            }
//            catch(Exception e){
//                LOGNREPORT.sphnxError(ABSTRACT_ACTION, "Locator: " + locator + " was not found in the UIMap!");
//                return null;
//            }
    }

    protected By getLocatorByDescriptor(String controlDescriptor) {
        try {
            return (By) applicationContext.getBean("lookUp", controlDescriptor);
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ABSTRACT_ACTION, "Locator: " + locator + " was not found in the UIMap!");
            return null;
        }
    }

    protected void setLocator(String fullName, String locator) {
        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
        try {
            controls.forEach(control -> {
                String controlFullName = control.getContext() + "." + control.getLogicalName();
                if (controlFullName.equalsIgnoreCase(fullName)) {
                    control.setDescriptor(locator);
                }
            });
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ABSTRACT_ACTION, "Locator: " + locator + " was not found in the UIMap!");
        }
    }


    protected String getParent(String locator) {
        boolean isControlFound = false;
        String parent = null;
        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
        try {
            for (Control control : controls) {
                String controlFullName = control.getContext() + "." + control.getLogicalName();
                if (controlFullName.equalsIgnoreCase(locator)) {
                    parent = control.getParent();
                    isControlFound = true;
                    break;
                }
            }
            if (isControlFound) {
                return parent;
            } else {
                LOGNREPORT.sphnxError(ABSTRACT_ACTION, "Control " + locator + " was not found in the UIMap");
                throw new Exception("Control " + locator + " was not found in the UIMap");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
