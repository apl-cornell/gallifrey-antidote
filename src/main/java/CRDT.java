import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.List;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import eu.antidotedb.client.Key;
import eu.antidotedb.client.GenericKey;

import java.util.Random;

import com.google.protobuf.ByteString;

abstract class CRDT implements Antidote_interface {
  /*
   * Object has to be an instance of CRDT
   * Make sure objects are able to use serializable(serialVersionUID)
   * Class fields that specify the types of arguments
   * Command query seperation
   * Generics Handling
   */

  private static final long serialVersionUID = 2L;

  public final GenericKey key;

  public CRDT(){
    Random rd = new Random();
    byte[] random_bytes = new byte[10];
    rd.nextBytes(random_bytes);
    ByteString random_key = ByteString.copyFrom(random_bytes);
    this.key = Key.generic(random_key);
  }

  abstract public Object value();

  @Override
  public Object invoke(GenericFunction obj) {
    String method_name = obj.getFunctionName();
    List<Object> args = obj.getArguments();

    try {
      Class<?>[] argTypes = (Class[]) this.getClass().getField(method_name).get(this);

      Method method = this.getClass().getDeclaredMethod(method_name, argTypes);

      return method.invoke(this, args.toArray());
    } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
      // If this throws then the field contains the wrong types, the field is not
      // declared, or there is something malformed about this object
      e.printStackTrace();
      System.exit(47);
      return null;
    } catch (InvocationTargetException e) {
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
      e.printStackTrace();
      System.exit(41);
    } catch (ClassNotFoundException e) {
      // Is fatal
      e.printStackTrace();
      System.exit(48);
    }
    // It shouldn't get to this but apparently the compiler says its possible because of System.exit() so we exit and hopefully don't return null
    System.out.println("Something went horribly wrong with CRDT's deepclone implementation");
    System.exit(43);
    return null;
  }
}