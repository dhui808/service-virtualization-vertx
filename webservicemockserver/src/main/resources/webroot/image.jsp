<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%

	// erase cookies
	Cookie[] cookies = request.getCookies();
	if(cookies!=null) {
		for (int i = 0; i < cookies.length; i++) {
			cookies[i].setMaxAge(0);
			cookies[i].setPath("/");
			response.addCookie(cookies[i]);
		}
	}

	String flow = request.getParameter("flow");
	String scenario = request.getParameter("scenario");
	
	if (null != flow) {
	    javax.servlet.http.Cookie cookie 
	           = new javax.servlet.http.Cookie("flow", flow);
	
	    cookie.setPath("/");
	    response.addCookie(cookie);
	}

	if (null != scenario) {
	    javax.servlet.http.Cookie cookie 
	           = new javax.servlet.http.Cookie("scenario", scenario);
	
	    cookie.setPath("/");
	    response.addCookie(cookie);
	}
%>