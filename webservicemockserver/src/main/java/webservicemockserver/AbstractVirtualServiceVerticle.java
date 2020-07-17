package webservicemockserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import webservicemockutil.MockData;

public abstract class AbstractVirtualServiceVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(AbstractVirtualServiceVerticle.class);
	
	protected MockData mockData;
	private String mockDataHome = (String)PropertiesLoader.loadProperties("application.properties")
			.get("servicevirtualizationdata.home");
	private String mockServerName;
	public static final String HTTP_SERVER_PORT = "http.server.port";
	public static final String HTTP_CONTEXT_ROOT = "http.context.root";
	protected String contextRoot;
	protected String restPath = "/rest";
	
	protected AbstractVirtualServiceVerticle(String mockServerName) {

		this.mockServerName = mockServerName;
	}

	@Override
	public void start(Promise<Void> promise) throws Exception {

		logger.debug("mockDataHome " + mockDataHome);
		
		HttpServer server = vertx.createHttpServer();

		contextRoot = config().getString(HTTP_CONTEXT_ROOT, "/banking");
		
		//do this async?
		createMockData();
		
	    Router router = Router.router(vertx);

	    router.route().handler(BodyHandler.create());
	    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

	    router.get(contextRoot + restPath + "/retrieveEntryPageURL").handler(this::retrieveEntryPageURLHandler);
		router.get(contextRoot + restPath + "/*").handler(this::selectFlowScenarioHandler);
		
	    // tag::static-assets[]
	    router.get(contextRoot + "/*").handler(StaticHandler.create().setCachingEnabled(false)); // <1> <2>
	    router.get("/favicon.ico").handler(FaviconHandler.create("webroot/favicon.ico"));
	    router.get("/").handler(context -> context.reroute(contextRoot + "/index.html"));
	    router.get(contextRoot).handler(context -> context.reroute(contextRoot + "/index.html"));
	    // end::static-assets[]
	    
		router.options().handler(this::optionsHandler);
		router.post().handler(this::postHandler);
		router.put().handler(this::putHandler);

		int portNumber = config().getInteger(HTTP_SERVER_PORT, 8080);
		
		server.requestHandler(router).listen(portNumber, ar -> {
			if (ar.succeeded()) {
				logger.info("HTTP server running on port " + portNumber);
				promise.complete();
			} else {
				logger.error("Could not start a HTTP server", ar.cause());
				promise.fail(ar.cause());
			}
		});
	}
	
	private void optionsHandler(RoutingContext context) {
		
		populateResponseHeader(context);
		
		context.response().end();
	}

	private void retrieveEntryPageURLHandler(RoutingContext context) {

		logger.debug("retrieveEntryPageURLHandler is called.");

		HttpServerResponse resp = context.response();
		
		resp.putHeader("Content-Type", "application/json; charset=utf-8");
		
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("entryPageUrl", mockData.getEntryPageUrl());

		logger.debug("entryPageUrl " + payload.get("entryPageUrl"));
		
		String json = null;
		
		try {
			json = new ObjectMapper().writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		resp.end(json);
	}

	private void selectFlowScenarioHandler(RoutingContext context) {
		
		logger.debug("selectFlowScenarioHandler IN");

		// Handle other requests - by default, it is the selecting flow and scenario.
		boolean isSetFlow = addFlowScenarioCookies(context);
		if (!isSetFlow) {
			// normal rest GET request
			String respFilePath = findResponseFilePath(context, "_GET");
			logger.debug("respFilePath:" + respFilePath);
			populateResponse(context, respFilePath);
		}
		
		context.response().end();
	}

	private void postHandler(RoutingContext context) {

		String respFilePath = findResponseFilePath(context, "");

		populateResponse(context, respFilePath);
		
		context.response().end();
	}

	private void putHandler(RoutingContext context) {

		String respFilePath = findResponseFilePath(context, "_PUT");

		populateResponse(context, respFilePath);
		
		context.response().end();

	}

	private void createMockData() {
		
		logger.debug("createMockData " + mockData);

		if (null != mockData) {
			return;
		}

		synchronized (this) {

			logger.debug("createMockData examining " + mockData);
			
			if (null != mockData) {
				return;
			}

			mockData = MockData.getMockData(mockServerName, mockDataHome);
		}
		
		logger.debug("createMockData now: " + mockData);
	}

	protected Map<String, String> getFlowScenarioMap(HttpServerRequest req) {

		Map<String, Cookie> cookies = req.cookieMap();

		Map<String, String> flowScenarioMap = new HashMap<String, String>(2);

		if (null == cookies)
			return flowScenarioMap;

		for (Cookie cookie : cookies.values()) {
			String name = cookie.getName();
			if (name.equals("flow") || name.equals("scenario")) {
				String value = cookie.getValue();
				flowScenarioMap.put(name, value);
			}
		}

		return flowScenarioMap;
	}

	protected String populateResponse(RoutingContext context, String responseFilePath) {

		logger.debug("populateResponse IN ");
		
		populateResponseHeader(context);
		HttpServerResponse resp = context.response();
		resp.putHeader("Content-Type", getContentType());

		// InputStream is = getClass().getResourceAsStream(responseFilePath);
		InputStream is = null;
		String respStr = null;
		try {
			is = new FileInputStream(responseFilePath);
			Scanner s = new Scanner(is).useDelimiter("\\A");
			respStr = s.hasNext() ? s.next() : "";
		} catch (FileNotFoundException e) {
			logger.error("populateResponse FileNotFoundException");
			e.printStackTrace();
		}
		
		resp.end(respStr);
		handleException(resp, respStr);
		return respStr;
	}

	private void populateResponseHeader(RoutingContext context) {

		HttpServerResponse resp = context.response();

		resp.putHeader("Allow", "GET, PUT, POST, HEAD, TRACE, OPTIONS");
		resp.putHeader("Access-Control-Allow-Origin", context.request().getHeader("Origin"));
		resp.putHeader("Access-Control-Allow-Credentials", "true");
		resp.putHeader("Access-Control-Allow-Headers", "Content-Type");

		resp.setStatusCode(200);
	}

	private boolean addFlowScenarioCookies(RoutingContext context) {

		HttpServerRequest req = context.request();
		HttpServerResponse resp = context.response();
		
		String flow = req.getParam("flow");
		String scenario = req.getParam("scenario");

		logger.debug(mockServerName + " flow:" + flow + " Scenario:" + scenario);

		if (null == flow) {
			return false;
		}
		
		eraseCookies(req, resp);

		addCookie("flow", flow, resp);
		addCookie("scenario", scenario, resp);

		return true;
	}
	
	private void eraseCookies(HttpServerRequest req, HttpServerResponse resp) {
		Map<String, Cookie> cookies = req.cookieMap();
		if (MapUtils.isNotEmpty(cookies)) {
			Iterator<Map.Entry<String, Cookie>> it = cookies.entrySet().iterator();
			
			 while (it.hasNext()) {
				 Cookie cookie = it.next().getValue();
				 cookie.setMaxAge(0);
				 cookie.setPath("/");
				 resp.addCookie(cookie);
			}
		}
		
		logger.debug("eraseCookies exit.");
	}
	
	private void addCookie(String key, String value, HttpServerResponse resp) {
		
		logger.debug("addCookie " + key + " = " + value);
		
		Cookie cookie = Cookie.cookie(key, value);
		cookie.setPath("/");
		resp.addCookie(cookie);
	}

	protected String adjustResponseFile(String responseFile, HttpServerRequest req, HttpServerResponse resp,
			String method) {
		// By default, the responseFile in the alternateResponseFileMap will alternate
		// with the 2nd version;
		List<String> alternateResponseFiles = mockData.getAlternateResponseFiles();
		String filePathNoSuffix = responseFile.substring(0, responseFile.indexOf(".json"));
		String responseFileName = filePathNoSuffix.substring(filePathNoSuffix.lastIndexOf("/") + 1);

		if (alternateResponseFiles.contains(req.path() + method)) {
			boolean sendAlternate = changeCountCookie(responseFileName, req, resp);
			if (sendAlternate) {
				responseFile = filePathNoSuffix + "2.json";
			}
		}

		return responseFile;
	}

	private boolean changeCountCookie(String responseFileName, HttpServerRequest req, HttpServerResponse resp) {
		boolean sendAlternate = false;
		boolean foundCookie = false;
		Map<String, Cookie> cookies = req.cookieMap();

		if (null == cookies)
			cookies = new HashMap<String, Cookie>();

		for (Cookie cookie : cookies.values()) {
			String name = cookie.getName();
			if (name.equals(responseFileName)) {
				foundCookie = true;
				String count = cookie.getValue();

				if ("0".equals(count)) {
					cookie.setValue("1");
					sendAlternate = true;
				} else {
					cookie.setValue("0");
				}

				resp.addCookie(cookie);
				break;
			}
		}

		if (!foundCookie) {
			resp.addCookie(Cookie.cookie(responseFileName, "0"));
		}

		return sendAlternate;
	}

	protected void handleException(HttpServerResponse resp, String respStr) {
		// By default, do nothing
	}
	
	/**
	 * Removes the context + restPath from the request path and returns the equivalent of Servlet path info.  
	 * @param vertxPath
	 * @return
	 */
	protected String getPathInfo(String vertxPath) {
		
		String pathPrefix = contextRoot + restPath;
		
		return vertxPath.replace(pathPrefix, "");
	}
	
	protected abstract String getContentType();

	protected abstract String findResponseFilePath(RoutingContext context, String method);

	public static abstract class SoapMockServer extends AbstractVirtualServiceVerticle {
		
		public SoapMockServer(String mockServerName) {
			super(mockServerName);
		}

		@Override
		protected String getContentType() {
			return "application/xml";
		}

		@Override
		protected String findResponseFilePath(RoutingContext context, String method) {

			HttpServerRequest req = context.request();
			HttpServerResponse resp = context.response();
			Map<String, String> flowScenarioMap = getFlowScenarioMap(req);
			String flow = flowScenarioMap.get("flow");
			String scenario = flowScenarioMap.get("scenario");
			String soapAction = req.getHeader("SOAPAction");
			logger.debug("SOAPAction path:" + soapAction);

			String path = soapAction.replace("\"", "");
			String responseFile = mockData.findFilePath(path, flow, scenario);

			return responseFile;
		}
	}

	public static abstract class RestVirtualServiceVerticle extends AbstractVirtualServiceVerticle {

		public RestVirtualServiceVerticle(String mockServerName) {
			super(mockServerName);
		}

		@Override
		protected String getContentType() {
			return "application/json";
		}

		// PUT and GET request have the _PUT and _GET suffix being added to pathInfo for
		// matching.
		// The purpose of this approach is to support the scenarios where PUT/GET/POST
		// request have the same
		// path in the RESTful services. For suffix POST requests, "" suffix is added.
		@Override
		protected String findResponseFilePath(RoutingContext context, String method) {

			logger.debug("findResponseFilePath IN");
			
			HttpServerRequest req = context.request();
			HttpServerResponse resp = context.response();
			Map<String, String> flowScenarioMap = getFlowScenarioMap(req);
			String flow = flowScenarioMap.get("flow");
			String scenario = flowScenarioMap.get("scenario");
			String path = req.path();
			path = getPathInfo(path);
			String responseFile = mockData.findFilePath(path + method, flow, scenario);

			responseFile = adjustResponseFile(responseFile, req, resp, method);
			
			logger.debug("response file after adjustment;" + responseFile);
			
			return responseFile;
		}
	}
}