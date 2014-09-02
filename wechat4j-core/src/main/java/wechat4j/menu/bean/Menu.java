package wechat4j.menu.bean;

import java.util.Arrays;

/**
 * Menu
 *
 * @author renbin.fang.
 * @date 2014/9/2.
 */
public class Menu {
    private Button[] menu;

    public Menu() {
    }

    public Menu(Button[] menu) {
        this.menu = menu;
    }

    public Button[] getMenu() {
        return menu;
    }

    public void setMenu(Button[] menu) {
        this.menu = menu;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Menu{");
        sb.append("menu=").append(menu == null ? "null" : Arrays.asList(menu).toString());
        sb.append('}');
        return sb.toString();
    }
}
