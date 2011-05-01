package vivae.util;

import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: mosquit
 * Date: 4/22/11
 * Time: 5:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeyboardLayout {

    public final int LAY_LEFT;
    public final int LAY_RIGHT;
    public final int LAY_UP;
    public final int LAY_DOWN;

    public KeyboardLayout(int left, int right, int up, int down) {
        this.LAY_LEFT = left;
        this.LAY_RIGHT = right;
        this.LAY_UP = up;
        this.LAY_DOWN = down;
    }

    public static KeyboardLayout ArrowsLayout() {
        return new KeyboardLayout(KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
    }

    public static KeyboardLayout AwdsLayout(){
        return new KeyboardLayout(KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S);
    }

}
