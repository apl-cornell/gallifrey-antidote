import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.List;

import java.lang.reflect.Method;

abstract class CRDT implements Antidote_interface {
  private static final long serialVersionUID = 1L;

  abstract public Object value();

  public void invoke(GenericFunction obj) {
    try {
      String method_name = obj.getFunctionName();
      List<Object> args = obj.getArguments();
      /* Integer id = obj.getId(); */

      Class<?>[] argTypes = new Class[args.size()];
      for (int i = 0; i < args.size(); i++) {
        argTypes[i] = args.get(i).getClass();
      }

      System.out.println(argTypes);

      Method method;
      try {
        method = this.getClass().getDeclaredMethod(method_name, argTypes);

        method.invoke(this, args.toArray());
      } catch (NoSuchMethodException e) {
        // This is the case where the method has generic args
        // Still doesn't work great
        for (int i = 0; i < args.size(); i++) {
          argTypes[i] = Object.class;
        }

        method = this.getClass().getDeclaredMethod(method_name, argTypes);
        method.invoke(this, args.toArray());
      }

    } catch (Exception e) {
      e.printStackTrace();
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
      return null;
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}