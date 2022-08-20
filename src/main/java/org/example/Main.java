package org.example;

import cool.scx.config.ScxEnvironment;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;

public class Main {

    public static void main(String[] args) {

        //get html root
        var environment = new ScxEnvironment(Main.class);
        var htmlRoot = environment.getPathByAppRoot("AppRoot:c");
        var tempUploadPath = environment.getTempPath("file-uploads");

        //create http server
        var vertx = Vertx.vertx();
        var httpServer = vertx.createHttpServer();
        var router = Router.router(vertx);

        // BodyHandler
        router.route().handler(BodyHandler.create(tempUploadPath.toString()).setDeleteUploadedFilesOnEnd(true));

        // StaticHandler
        router.route("/*").last().handler(StaticHandler.create(FileSystemAccess.ROOT, htmlRoot.toString()));

        // There's a problem here
        router.post("/upload").handler(ctx -> {
            var entries = ctx.request().formAttributes();
            var fileMD5 = entries.get("fileMD5");
            // The field fileMD5 value will has redundant /r
            // Should be     -> "908ed92175ce3c62e37c004422dcda09"
            // But i got -> "908ed92175ce3c62e37c004422dcda09/r"
            // It seems that only 1.jpg has this bug
            // There are no bug with other file
            // Is it a Netty bug ? Or is there a problem with my code ?
            System.err.println("fileMD5 : [" + fileMD5 + "]");
            ctx.response().end("ok");
        });

        // listen
        httpServer.requestHandler(router)
                .listen(8888)
                .onComplete(r -> System.out.println("listen : " + r.result().actualPort()));

    }

}