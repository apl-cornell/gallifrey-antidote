import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.List;
import java.util.Random;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

abstract class CRDT implements Antidote_interface {
  private static final long serialVersionUID = new Random().nextLong();

  abstract public Object value();

  @Override
  public void invoke(GenericFunction obj) {
    String method_name = obj.getFunctionName();
    List<Object> args = obj.getArguments();

    try {
      Class<?>[] argTypes = (Class[]) this.getClass().getField(method_name).get(this);

      Method method = this.getClass().getDeclaredMethod(method_name, argTypes);

      method.invoke(this, args.toArray());
    } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
      // If this throws then the field contains the wrong types, the field is not
      // declared, or there is something malformed about this object
      throw new RuntimeException(e);
    } catch ( InvocationTargetException e) {
      // The method returned some exception so it is now a runtime exception
      throw new RuntimeException(e);
    }
  }

  public CRDT deepClone() {
    /*
     * Shamelessly based on code from the below link. Altered to be more of a
     * function than an object method
     * https://www.avajava.com/tutorials/lessons/how-do-i-perform-a-deep-clone-using
     * -serializable.html
     */

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(this);

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      return (CRDT) ois.readObject();
    } catch (IOException e) {
      // Is fatal
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      // Is fatal
      throw new RuntimeException(e);
    }
  }
}