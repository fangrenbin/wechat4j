package wechat4j.handler;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import wechat4j.WechatTestBase;
import wechat4j.handler.IMenuHandler;
import wechat4j.handler.impl.MenuHandler;
import wechat4j.bean.menu.Button;
import wechat4j.bean.menu.Menu;

/**
 * ButtonHandlerTest
 *
 * @author renbin.fang.
 * @date 2014/9/1.
 */
@Test(enabled = true)
public class ButtonHandlerTest extends WechatTestBase {
    private IMenuHandler menuHandler = (IMenuHandler) handlerMap.get(MenuHandler.class.getName());

    @Test
    public void createMenuTest() {
        String jsonData = gnerateMenuJson();

        System.out.println(menuHandler.createMenu(jsonData));
    }

    @Test
    public void queryMenuTest() {
        String jsonData = menuHandler.queryMenu();

        System.out.println(jsonData);

        JSONObject jsonObject = new JSONObject(jsonData);
        JSONObject menuObject = jsonObject.getJSONObject("menu");
        JSONArray buttonArray = menuObject.getJSONArray("button");

        Menu menu = new Menu();
        int cur = 0;
        Button[] firstLevelButtons = new Button[3];
        for (int i = 0; i < buttonArray.length(); i++) {
            JSONObject button = (JSONObject) buttonArray.get(i);
            JSONArray subButtonsJson = button.getJSONArray("sub_button");

            // 解析一级菜单
            if (subButtonsJson.length() <= 0) {
                String type = button.get("type").toString();
                if (StringUtils.equals(type, Button.ButtonType.CLICK.getValue())) {
                    firstLevelButtons[cur] = getClickButton(button);
                    cur++;
                    continue;
                }

                if (StringUtils.equals(type, Button.ButtonType.VIEW.getValue())) {
                    firstLevelButtons[cur] = getViewButton(button);
                    cur++;
                    continue;
                }
            } else {
                // 有二级菜单
                firstLevelButtons[cur] = getSubButtons(subButtonsJson);
                firstLevelButtons[cur].setName(button.getString("name"));
                cur++;
            }
        }

        menu.setMenu(firstLevelButtons);
        System.out.println(menu.toString());
    }

    private Button.ViewButton getViewButton(JSONObject jsonObject) {
        String name = jsonObject.get("name").toString();
        String url = jsonObject.get("url").toString();

        return new Button.ViewButton(name, url);
    }

    private Button.ClickButton getClickButton(JSONObject jsonObject) {
        String name = jsonObject.get("name").toString();
        String key = jsonObject.get("key").toString();

        return new Button.ClickButton(name, key);
    }

    private Button.SubButton getSubButtons(JSONArray jsonArray) {
        Button.SubButton subButton = new Button.SubButton();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String type = jsonObject.get("type").toString();

            if (StringUtils.equals(type, Button.ButtonType.VIEW.getValue())) {
                subButton.add(getViewButton(jsonObject));
                continue;
            }

            if (StringUtils.equals(type, Button.ButtonType.CLICK.getValue())) {
                subButton.add(getClickButton(jsonObject));
            }
        }

        return subButton;
    }

    /**
     * 生成创建菜单的JSON数据
     *
     * @return
     */
    private String gnerateMenuJson() {
        Button.ClickButton clickButton = new Button.ClickButton();
        clickButton.setKey("V1001_TODAY_MUSIC");
        clickButton.setName("今日歌曲");

        Button.ClickButton clickButton1 = new Button.ClickButton();
        clickButton1.setKey("V1001_TODAY_MUSIC");
        clickButton1.setName("歌手简介");


        Button.ViewButton subviewButton1 = new Button.ViewButton();
        Button.ViewButton subviewButton2 = new Button.ViewButton();

        subviewButton1.setName("搜索");
        subviewButton1.setUrl("http://www.soso.com/");

        subviewButton2.setName("视频");
        subviewButton2.setUrl("http://v.qq.com/");

        Button.ClickButton subclickButton = new Button.ClickButton();
        subclickButton.setName("赞一下我们");
        subclickButton.setKey("V1001_GOOD");

        Button.SubButton subButton = new Button.SubButton();
        subButton.setName("菜单");
        subButton.setSub_button(new Button[]{subviewButton1, subviewButton2, subclickButton});

        Button.MainButton mainButton = new Button.MainButton();
        mainButton.setButton(new Button[]{clickButton, clickButton1, subButton});

        return new JSONObject(mainButton).toString();
    }
}