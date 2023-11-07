package grails.plugin.json.builder;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import groovy.json.JsonException;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GString;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Writable;

/**
 * The delegate used when invoking closures
 */
public class StreamingJsonDelegate extends GroovyObjectSupport {

    protected final Writer writer;
    protected boolean first;
    protected State state;

    private final JsonGenerator generator;

    public StreamingJsonDelegate(Writer w, boolean first) {
        this(w, first, null);
    }

    public StreamingJsonDelegate(Writer w, boolean first, JsonGenerator generator) {
        this.writer = w;
        this.first = first;
        this.generator = (generator != null) ? generator : JsonOutput.DEFAULT_GENERATOR;
    }

    /**
     * @return Obtains the current writer
     */
    public Writer getWriter() {
        return writer;
    }

    public Object invokeMethod(String name, Object args) {
        if (args != null && Object[].class.isAssignableFrom(args.getClass())) {
            try {
                Object[] arr = (Object[]) args;

                final int len = arr.length;
                switch (len) {
                    case 1:
                        final Object value = arr[0];
                        if(value instanceof Closure) {
                            call(name, (Closure)value);
                        }
                        else if(value instanceof Writable) {
                            call(name, (Writable) value);
                        }
                        else {
                            call(name, value);
                        }
                        return null;
                    case 2:
                        if(arr[len -1] instanceof Closure) {
                            final Object obj = arr[0];
                            final Closure callable = (Closure) arr[1];
                            if(obj instanceof Iterable) {
                                call(name, (Iterable)obj, callable);
                                return null;
                            }
                            else if(obj.getClass().isArray()) {
                                call(name, Arrays.asList( (Object[])obj), callable);
                                return null;
                            }
                            else {
                                call(name, obj, callable);
                                return null;
                            }
                        }
                    default:
                        final List<Object> list = Arrays.asList(arr);
                        call(name, list);

                }
            } catch (IOException ioe) {
                throw new JsonException(ioe);
            }
        }

        return this;
    }

    /**
     * Writes the name and a JSON array
     * @param name The name of the JSON attribute
     * @param list The list representing the array
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name, List<Object> list) throws IOException {
        if (generator.isExcludingFieldsNamed(name)) {
            return;
        }
        writeName(name);
        writeArray(list);
    }

    /**
     * Writes the name and a JSON array
     * @param name The name of the JSON attribute
     * @param array The list representing the array
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name, Object...array) throws IOException {
        if (generator.isExcludingFieldsNamed(name)) {
            return;
        }
        writeName(name);
        writeArray(Arrays.asList(array));
    }

    /**
     * A collection and closure passed to a JSON builder will create a root JSON array applying
     * the closure to each object in the collection
     * <p>
     * Example:
     * <pre class="groovyTestCase">
     * class Author {
     *      String name
     * }
     * def authorList = [new Author (name: "Guillaume"), new Author (name: "Jochen"), new Author (name: "Paul")]
     *
     * new StringWriter().with { w -&gt;
     *     def json = new groovy.json.StreamingJsonBuilder(w)
     *     json.book {
     *        authors authorList, { Author author -&gt;
     *         name author.name
     *       }
     *     }
     *
     *     assert w.toString() == '{"book":{"authors":[{"name":"Guillaume"},{"name":"Jochen"},{"name":"Paul"}]}}'
     * }
     * </pre>
     * @param name The name of the JSON attribute
     * @param coll a collection
     * @param c a closure used to convert the objects of coll
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name, Iterable coll, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST)  Closure c) throws IOException {
        if (generator.isExcludingFieldsNamed(name)) {
            return;
        }
        writeName(name);
        writeObjects(coll, c);
    }

    /**
     * Delegates to {@link #call(String, Iterable, Closure)}
     *
     * @param name The name of the JSON attribute
     * @param coll a collection
     * @param c a closure used to convert the objects of coll
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name, Collection coll, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST)  Closure c) throws IOException {
        call(name, (Iterable)coll, c);
    }

    /**
     * Writes the name and value of a JSON attribute
     *
     * @param name The attribute name
     * @param value The value
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name, Object value) throws IOException {
        if (generator.isExcludingFieldsNamed(name) || generator.isExcludingValues(value)) {
            return;
        }
        writeName(name);
        writeValue(value);
    }

    /**
     * Writes the name and value of a JSON attribute
     *
     * @param name The attribute name
     * @param value The value
     * @param callable a closure used to convert the objects of coll
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name, Object value, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure callable) throws IOException {
        if (generator.isExcludingFieldsNamed(name)) {
            return;
        }
        writeName(name);
        verifyValue();
        writeObject(writer, value, callable, generator);
    }

    /**
     * Writes the name and another JSON object
     *
     * @param name The attribute name
     * @param value The value
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name,@DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure value) throws IOException {
        if (generator.isExcludingFieldsNamed(name)) {
            return;
        }
        writeName(name);
        verifyValue();
        writer.write(JsonOutput.OPEN_BRACE);
        StreamingJsonDelegate.cloneDelegateAndGetContent(writer, value, true, generator);
        writer.write(JsonOutput.CLOSE_BRACE);
    }

    /**
     * Writes an unescaped value. Note: can cause invalid JSON if passed JSON is invalid
     *
     * @param name The attribute name
     * @param json The value
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name, JsonOutput.JsonUnescaped json) throws IOException {
        if (generator.isExcludingFieldsNamed(name)) {
            return;
        }
        writeName(name);
        verifyValue();
        writer.write(json.toString());
    }

    /**
     * Writes an unescaped value. Note: can cause invalid JSON if passed JSON is invalid
     *
     * @param name The attribute name
     * @param json The value
     * @throws IOException
     *         If an I/O error occurs
     */
    public void call(String name, Writable json) throws IOException {
        writeName(name);
        verifyValue();
        if(json instanceof GString) {
            writer.write(generator.toJson(json.toString()));
        }
        else {
            json.writeTo(writer);
        }
    }

    public void setFirst(boolean first) {
        this.first = first;
    }


    private void writeObjects(Iterable coll, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure c) throws IOException {
        verifyValue();
        writeCollectionWithClosure(writer, coll, c, generator);
    }

    protected void verifyValue() {
        if(state == State.VALUE) {
            throw new IllegalStateException("Cannot write value when value has just been written. Write a name first!");
        }
        else {
            state = State.VALUE;
        }
    }


    protected void writeName(String name) throws IOException {
        if (generator.isExcludingFieldsNamed(name)) {
            return;
        }
        if(state == State.NAME) {
            throw new IllegalStateException("Cannot write a name when a name has just been written. Write a value first!");
        }
        else {
            this.state = State.NAME;
        }
        if (!first) {
            writer.write(JsonOutput.COMMA);
        } else {
            first = false;
        }
        writer.write(generator.toJson(name));
        writer.write(JsonOutput.COLON);
    }

    protected void writeValue(Object value) throws IOException {
        verifyValue();
        writer.write(generator.toJson(value));
    }

    protected void writeArray(List<Object> list) throws IOException {
        verifyValue();
        writer.write(generator.toJson(list));
    }

    public static boolean isCollectionWithClosure(Object[] args) {
        return args.length == 2 && args[0] instanceof Iterable && args[1] instanceof Closure;
    }

    public static Object writeCollectionWithClosure(Writer writer, Collection coll, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST)   Closure closure) throws IOException {
        return writeCollectionWithClosure(writer, (Iterable)coll, closure, JsonOutput.DEFAULT_GENERATOR);
    }

    public static Object writeCollectionWithClosure(Writer writer, Iterable coll, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure closure, JsonGenerator generator) throws IOException {
        writer.write(JsonOutput.OPEN_BRACKET);
        boolean first = true;
        for (Object it : coll) {
            if (!first) {
                writer.write(JsonOutput.COMMA);
            } else {
                first = false;
            }

            writeObject(writer, it, closure, generator);
        }
        writer.write(JsonOutput.CLOSE_BRACKET);

        return writer;
    }

    private static void writeObject(Writer writer, Object object, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure closure, JsonGenerator generator) throws IOException {
        writer.write(JsonOutput.OPEN_BRACE);
        curryDelegateAndGetContent(writer, closure, object, true, generator);
        writer.write(JsonOutput.CLOSE_BRACE);
    }

    public static void cloneDelegateAndGetContent(Writer w, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure c)
    {
        cloneDelegateAndGetContent(w, c, true);
    }

    public static void cloneDelegateAndGetContent(Writer w, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure c, boolean first) {
        cloneDelegateAndGetContent(w, c, first, JsonOutput.DEFAULT_GENERATOR);
    }

    public static void cloneDelegateAndGetContent(Writer w, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure c, boolean first, JsonGenerator generator) {
        StreamingJsonDelegate delegate = new StreamingJsonDelegate(w, first, generator);
        Closure cloned = (Closure) c.clone();
        cloned.setDelegate(delegate);
        cloned.setResolveStrategy(Closure.DELEGATE_FIRST);
        cloned.call();
    }

    public static void curryDelegateAndGetContent(Writer w, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure c, Object o) {
        curryDelegateAndGetContent(w, c, o, true);
    }

    public static void curryDelegateAndGetContent(Writer w, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure c, Object o, boolean first) {
        curryDelegateAndGetContent(w, c, o, first, JsonOutput.DEFAULT_GENERATOR);
    }

    private static void curryDelegateAndGetContent(Writer w, @DelegatesTo(value = StreamingJsonDelegate.class, strategy = Closure.DELEGATE_FIRST) Closure c, Object o, boolean first, JsonGenerator generator) {
        StreamingJsonDelegate delegate = new StreamingJsonDelegate(w, first, generator);
        Closure curried = c.curry(o);
        curried.setDelegate(delegate);
        curried.setResolveStrategy(Closure.DELEGATE_FIRST);
        curried.call();
    }

    private enum State {
        NAME, VALUE
    }
}