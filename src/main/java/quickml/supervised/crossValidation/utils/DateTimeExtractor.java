package quickml.supervised.crossValidation.utils;
import org.joda.time.DateTime;

/**
 * Created by alexanderhawk on 5/6/14.
 */
public interface DateTimeExtractor <I> {
    DateTime extractDateTime(I instance);
}
