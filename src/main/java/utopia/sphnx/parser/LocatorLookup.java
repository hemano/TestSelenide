package utopia.sphnx.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.openqa.selenium.By;
import utopia.sphnx.actions.Actions;

import java.util.List;
import java.util.Map;

/**
 * Created by jitendrajogeshwar on 19/05/17.
 */
public class LocatorLookup {

    /** Constant for the prefix separator. */
    private static final char PREFIX_SEPARATOR = '=';

    private char separator = PREFIX_SEPARATOR;

    public char getSeparator() {
        return separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    private static List<Lookup> lookups;

    /** A map with the currently registered lookup objects. */
    private static final Map<String, Lookup> prefixLookups = Maps.newHashMap();


    static
    {
        lookups = ImmutableList.of(
                new ClassNameLookUp(),
                new IdLookUp(),
                new CssSelectorLookUp(),
                new XpathLookUp(),
                new LinkTextLookUp(),
                new PartialLinkTextLookUp(),
                new NameLookUp(),
                new TagNameLookUp()
        );

        for (Lookup l: lookups)
        {
            for(String s : l.getPrefix())
            prefixLookups.put(s,l);
        }

    }

    public Object resolve(String var)
    {
        if(var.equalsIgnoreCase("")){
            return By.cssSelector("");
        }

        if(var.equalsIgnoreCase(Actions.class.getCanonicalName())){
            return By.cssSelector("");
        }

        Preconditions.checkNotNull(var);

        int prefixPos = var.indexOf(separator);
        if (prefixPos >= 0)
        {
            String prefix = var.substring(0, prefixPos);
            String name = var.substring(prefixPos + 1);
            Object value = fetchLookupForPrefix(prefix).lookup(name);
            if (value != null)
            {
                return value;
            }
        }

        throw new IllegalArgumentException("Cannot resolve " + var);
    }

    private Lookup fetchLookupForPrefix(String prefix)
    {
        return prefixLookups.get(prefix);
    }

    private static class CssSelectorLookUp implements Lookup {

        @Override
        public Object lookup(String variable) {
            return By.cssSelector(variable);
        }

        @Override
        public String[] getPrefix() {
            return new String[]{"css","cssSelector"};
        }
    }

    private static class XpathLookUp implements Lookup {

        @Override
        public Object lookup(String variable) {
            return By.xpath(variable);
        }

        @Override
        public String[] getPrefix() {
            return new String[]{"xpath"};
        }
    }

    private static class IdLookUp implements Lookup {

        @Override
        public Object lookup(String variable) {
            return By.id(variable);
        }

        @Override
        public String[] getPrefix() {
            return new String[]{"id"};
        }
    }

    private static class ClassNameLookUp implements Lookup {
        @Override
        public Object lookup(String variable) {
            return By.className(variable);
        }

        @Override
        public String[] getPrefix() {
            return new String[]{"class","className"};
        }
    }

    private static class NameLookUp implements Lookup {

        @Override
        public Object lookup(String variable) {
            return By.name(variable);
        }

        @Override
        public String[] getPrefix() {
            return new String[]{"name"};
        }
    }

    private static class LinkTextLookUp implements Lookup {
        @Override
        public Object lookup(String variable) {
            return By.linkText(variable);
        }

        @Override
        public String[] getPrefix() {
            return new String[]{"link","linkText"};
        }
    }

    private static class PartialLinkTextLookUp implements Lookup {

        @Override
        public Object lookup(String variable) {
            return By.partialLinkText(variable);
        }

        @Override
        public String[] getPrefix() {
            return new String[]{"partialLink","partialLinkText"};
        }
    }

    private static class TagNameLookUp implements Lookup {

        @Override
        public Object lookup(String variable) {
            return By.tagName(variable);
        }

        @Override
        public String[] getPrefix() {
            return new String[]{"tagName","tag"};
        }
    }


}
