package com.rekoe.module;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.view.ForwardView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;

import com.rekoe.exception.IncorrectCaptchaException;
import com.rekoe.filter.CaptchaFormAuthenticationFilter;

@IocBean
public class SystemModule {
	private static final Log logger = Logs.get();

	@At("/login")
	@Filters(@By(type = CaptchaFormAuthenticationFilter.class))
	public View login(@Attr("loginToken") AuthenticationToken token) {
		try {
			Subject subject = SecurityUtils.getSubject();
			ThreadContext.bind(subject);
			subject.login(token);
			return new ServerRedirectView("/admin/index");
		} catch (IncorrectCaptchaException e) {
			return new ViewWrapper(new ForwardView("/"), e.getMessage());
		} catch (LockedAccountException e) {
			Map<String, Object> msgs = Mvcs.getLocaleMessage("zh_CN");
			String errMsg = msgs.get("admin.login.lockedAccount").toString();
			return new ViewWrapper(new ForwardView("/"), errMsg);
		} catch (AuthenticationException e) {
			Map<String, Object> msgs = Mvcs.getLocaleMessage("zh_CN");
			String errMsg = msgs.get("login_error").toString();
			return new ViewWrapper(new ForwardView("/"), errMsg);
		} catch (Exception e) {
			return new ViewWrapper(new ForwardView("/"), e.getMessage());
		}
	}

	@At("/logout")
	@Ok(">>:/")
	@RequiresAuthentication
	public void logout() {
		Subject currentUser = SecurityUtils.getSubject();
		try {
			currentUser.logout();
		} catch (SessionException ise) {
			logger.debug("Encountered session exception during logout.  This can generally safely be ignored.", ise);
		} catch (Exception e) {
			logger.debug("登出发生错误", e);
		}
	}

	@At("/")
	@Ok("fm:template.login.login")
	public String index() {
		return null;
	}

	/**
	 * 验证码
	 */
	@At("/captcha")
	@Ok("captcha:captcha")
	@Fail("jsp:error.404")
	@Filters()
	public void captcha() {
	}
}
