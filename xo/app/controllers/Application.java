package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Tic Tac Toe"));
    }

    // Websocket interface
    public static WebSocket<String> wsInterface() {
        return new WebSocket<String>() {
            // called when websocket handshake is done
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                SimpleXO.start(in, out);
            }
        };
    }

}
