/*
 * Created on Dec 17, 2010
 */

public class Pojo {
	public void overrideMe() {
		System.out.println("override me");
	}

	public void overrideYou() {
		overrideMe();
	}

}
