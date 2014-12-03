package avatar.voting;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class StarterVerticle extends Verticle {

    @Override
    public void start() {

        Logger logger = container.logger();

        container.deployWorkerVerticle("avatar.voting.AvatarVotingVerticle", null, 4, false, (AsyncResult<String> asyncResult) -> {
            if (asyncResult.succeeded()) {
                logger.info("AvatarVotingVerticle has been deployed, deployment ID is " + asyncResult.result());
            } else {
                logger.error("Failed to start AvatarVotingVerticle", asyncResult.cause());
            }
        });
    }
}
