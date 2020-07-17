package webservicemockserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class WebserviceVirtualServiceApplication extends AbstractVerticle {

	@Override
	public void start() {
		vertx.deployVerticle("webservicemockserver.MobileWebVirtualService");
	}
}
