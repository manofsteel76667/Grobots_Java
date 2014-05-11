package sides;

/*Common interface for all hardware items
 */
public interface Hardware<T> {
	public double Mass();

	public double Cost();

	public T clone();
}
