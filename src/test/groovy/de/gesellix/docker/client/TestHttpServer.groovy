package de.gesellix.docker.client

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer

import java.util.concurrent.Executors

class TestHttpServer {

  HttpServer httpServer

  def start() {
    InetSocketAddress addr = new InetSocketAddress(0)
    httpServer = HttpServer.create(addr, 0)
    httpServer.with {
      createContext('/test/', new ReverseHandler())
      setExecutor(Executors.newCachedThreadPool())
      start()
    }
    return httpServer.address
  }

  def stop() {
    if (httpServer) {
      httpServer.stop(0)
    }
  }

  static class ReverseHandler implements HttpHandler {

    @Override
    void handle(HttpExchange httpExchange) {
      if (httpExchange.requestMethod == 'GET') {
        httpExchange.responseHeaders.set('Content-Type', 'text/plain')
        final String query = httpExchange.requestURI.rawQuery

        if (!query || !query.contains('string')) {
          httpExchange.sendResponseHeaders(400, 0)
          return
        }

        final String[] param = query.split('=')
        assert param.length == 2 && param[0] == 'string'

        httpExchange.sendResponseHeaders(200, 0)
        httpExchange.responseBody.write(param[1].reverse().bytes)
        httpExchange.responseBody.close()
      }
    }
  }
}
