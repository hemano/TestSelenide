package utopia.sphnx.core.support;

import java.util.function.Consumer;

/**
 * Created by jitendrajogeshwar on 19/05/17.
 */
public interface Action {

    boolean execute() throws Exception;
    
    default <T> void execute(Consumer<T> consumer){
        /* no-op */
    }

}
