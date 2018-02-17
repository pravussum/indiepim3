package net.mortalsilence.indiepim.server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
public class LoginController {

    private final HttpServletRequest httpServletRequest;

    @Inject
    public LoginController(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @RequestMapping(value = "/api/login")
    public Principal login(Principal user) {
        return user;
    }

    @RequestMapping(value = "/api/logout")
    public void logout() throws ServletException {
        if(httpServletRequest.getSession() != null) {
            httpServletRequest.getSession().invalidate();
        }
        httpServletRequest.logout();
    }

//	// TODO write fieldVerifier for ServiceInputs , escapeHTML against XS-Vulnerabilities!
//	// encapsulate tx handling
//
//    final GenericLoginService loginService = new GenericLoginService();
//
//
//	public void addUser(String userName, String password) {
//        loginService.addUser(userName, password);
//	}
//
//	public String getSession(final String userName, final String password) {
//        return loginService.getSession(userName, password, getThreadLocalRequest().getSession());
//	}
//
//	public Boolean isSessionValid(final String sessionId) {
//		return loginService.isSessionValid(sessionId);
//	}
//
//    public Boolean isSetupDone() {
//        return loginService.isSetupDone();
//    }
}
