/**
 * 功能说明:
 * 功能作者:
 * 创建日期:
 * 版权归属:每特教育|蚂蚁课堂所有 www.itmayiedu.com
 */
package com.itmayiedu.oauth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.itmayiedu.base.BaseApiService;
import com.itmayiedu.utils.HttpClientUtils;
import com.itmayiedu.utils.WeiXinUtils;

@Controller
public class OauthController extends BaseApiService {

	@Autowired
	private WeiXinUtils weiXinUtils;
	private String errorPage = "errorPage";

	// 生成授权链接
	@RequestMapping("/authorizedUrl")
	public String authorizedUrl() {
		return "redirect:" + weiXinUtils.getAuthorizedUrl();
	}

	// 微信授权回调地址
	@RequestMapping("/callback")
	public String callback(String code, HttpServletRequest request) {
		// 1.使用Code 获取 access_token（调用微信接口有权限）; code是微信返回；
		String accessTokenUrl = weiXinUtils.getAccessTokenUrl(code);
		JSONObject resultAccessToken = HttpClientUtils.httpGet(accessTokenUrl);
		//根据微信api返回内容，如果有errcode则出错（具体查看微信api文档）
		boolean containsKey = resultAccessToken.containsKey("errcode");

		if (containsKey) {
			request.setAttribute("errorMsg", "系统错误!");
			return errorPage;
		}
		// 2.使用access_token获取用户信息，根据微信api，可以获取access_token和openid
		String accessToken = resultAccessToken.getString("access_token");
		String openid = resultAccessToken.getString("openid");
		// 3.使用access_token和openid拉取用户信息(需scope为 snsapi_userinfo)
		String userInfoUrl = weiXinUtils.getUserInfo(accessToken, openid);
		JSONObject userInfoResult = HttpClientUtils.httpGet(userInfoUrl);
		System.out.println("userInfoResult:" + userInfoResult);
		//获取的用户信息，nickname, city, headimgurl，具体查看微信api文档
		request.setAttribute("nickname", userInfoResult.getString("nickname"));
		request.setAttribute("city", userInfoResult.getString("city"));
		request.setAttribute("headimgurl", userInfoResult.getString("headimgurl"));
		return "info";
	}

}
