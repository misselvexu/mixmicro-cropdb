package xyz.vopen.framework.cropdb.migration;

/**
 * Represents a type converter.
 *
 * @param <S> the type parameter
 * @param <T> the type parameter
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 4.0
 */
public interface TypeConverter<S, T> {
    /**
     * Converts an object of type <code>S</code> to an object of type <code>T</code>.
     *
     * @param source the source
     * @return the target object
     */
    T convert(S source);
}
