package webservicemockserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpServerResponse;

public class MobileWebVirtualService extends AbstractVirtualServiceVerticle.RestVirtualServiceVerticle {

	private static final long serialVersionUID = 3724936760788768773L;
	private static final Logger logger = LoggerFactory.getLogger(MobileWebVirtualService.class);
	
	public MobileWebVirtualService() {
		super("mobilewebmockserver");
	}
	
	@Override
	protected void handleException(HttpServerResponse resp, String respStr) {
		if (respStr.contains("exception")) {
	    	logger.debug("There is exception item in response string.");
	    }
	}
}