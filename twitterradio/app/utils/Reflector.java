package utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


//import net.sf.oval.exception.MethodNotFoundException;

public abstract class Reflector {
	public static <T> T instantiate(final String className, final Class<T> type){
		boolean isSingleton = Reflector.assertIsSingleton(className);
		return Reflector.instantiate(className, type, isSingleton);
	}
	
	public static <T> T instantiate(final String className, final Class<T> type, boolean isSingleton){
		try{
			Class<?> c = Class.forName(className);
			Class[] argTypes = new Class[] {  };
			if(isSingleton) {
				U.out("getting instance");
				//If class is a singleton we want to return the getInstance() result
				Method getInstanceMethod = c.getDeclaredMethod("getInstance", argTypes);
				return type.cast(getInstanceMethod.invoke(null));
			} else {
				//else initialize normally throught constructor
				U.out("Constructing");
				return type.cast(c.newInstance());
			}
	    } catch(final InstantiationException e) {
	        throw new IllegalStateException(e);
	    } catch(final IllegalAccessException e) {
	        throw new IllegalStateException(e);
	    } catch(final ClassNotFoundException e) {
	        throw new IllegalStateException(e);
	    } catch(final NoSuchMethodException e) {
	        return null;
	    } catch(final InvocationTargetException e) {
	    	return null;
	    }
	}
	
	public static boolean assertIsSingleton(final String className)
	{
		try{
			Field f = Class.forName(className).getField("isSingleton");
			return f.getBoolean(null);
		} catch(Exception pokemon) {
			//gotta catch 'em all
			return false;
		}
	}
}
