package utopia.sphnx.parser;

/**
 * Created by jitendrajogeshwar on 19/05/17.
 */
public interface Lookup {

    Object lookup(String variable);

    String[] getPrefix();

}
