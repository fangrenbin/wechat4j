package wechat4j.support;

import wechat4j.support.bean.AccessToken;

/**
 * IAccessTokenGetter
 *
 * @author renbin.fang.
 * @date 2014/9/5.
 */
public interface IAccessTokenGetter {
    public AccessToken getAccessToken();

    public AccessToken getAccessToken(String appId, String appSecret);
}
