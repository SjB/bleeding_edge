// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
//
// VMOptions=
// VMOptions=--short_socket_read
// VMOptions=--short_socket_write
// VMOptions=--short_socket_read --short_socket_write

import "dart:async";
import "dart:io";
import "dart:scalarlist";


void testClientAndServerCloseNoListen(int connections) {
  HttpServer.bind().then((server) {
    int closed = 0;
    server.listen((request) {
      request.response.close();
      request.response.done.then((_) {
        closed++;
        if (closed == connections) {
          Expect.equals(0, server.connectionsInfo().active);
          Expect.equals(server.connectionsInfo().total,
                        server.connectionsInfo().idle);
          server.close();
        }
      });
    });
    var client = new HttpClient();
    for (int i = 0; i < connections; i++) {
      client.get("localhost", server.port, "/")
          .then((request) => request.close())
          .then((response) {
          });
    }
  });
}


void testClientCloseServerListen(int connections) {
  HttpServer.bind().then((server) {
    int closed = 0;
    void check() {
      closed++;
      if (closed == connections * 2) {
        Expect.equals(0, server.connectionsInfo().active);
        Expect.equals(server.connectionsInfo().total,
                      server.connectionsInfo().idle);
        server.close();
      }
    }
    server.listen((request) {
      request.listen(
          (_) {},
          onDone: () {
            request.response.close();
            request.response.done.then((_) => check());
          });
    });
    var client = new HttpClient();
    for (int i = 0; i < connections; i++) {
      client.get("localhost", server.port, "/")
          .then((request) => request.close())
          .then((response) => check());
    }
  });
}


void testClientCloseDelayed(int connections) {
  HttpServer.bind().then((server) {
    int closed = 0;
    void check() {
      closed++;
      // Wait for both server and client to see the connections as closed.
      if (closed == connections * 2) {
        Expect.equals(0, server.connectionsInfo().active);
        Expect.equals(server.connectionsInfo().total,
                      server.connectionsInfo().idle);
        server.close();
      }
    }
    server.listen((request) {
      request.pipe(request.response)
          .then((_) => check());
    });
    var client = new HttpClient();
    for (int i = 0; i < connections; i++) {
      var req;
      client.post("localhost", server.port, "/")
          .then((request) {
            req = request;
            request.writeBytes(new Uint8List(1024));
            return request.response;
          })
          .then((response) {
            req.close();
            // Ensure we don't accept the response until we have send the entire
            // request.
            response.listen(
                (_) {},
                onDone: () {
                  check();
                });
          });
    }
  });
}


void testClientCloseSendingResponse(int connections) {
  HttpServer.bind().then((server) {
    int closed = 0;
    void check() {
      closed++;
      // Wait for both server and client to see the connections as closed.
      if (closed == connections * 2) {
        Expect.equals(0, server.connectionsInfo().active);
        Expect.equals(server.connectionsInfo().total,
                      server.connectionsInfo().idle);
        server.close();
      }
    }
    server.listen((request) {
      var timer = new Timer.periodic(const Duration(milliseconds: 20), (_) {
        request.response.writeBytes(new Uint8List(16 * 1024));
      });
      request.response.done
          .catchError((_) {})
          .whenComplete(() {
            check();
            timer.cancel();
          });
    });
    var client = new HttpClient();
    for (int i = 0; i < connections; i++) {
      client.get("localhost", server.port, "/")
          .then((request) => request.close())
          .then((response) {
            // Ensure we don't accept the response until we have send the entire
            // request.
            var subscription = response.listen((_) {});
            new Timer(const Duration(milliseconds: 200), () {
              subscription.cancel();
              check();
            });
          });
    }
  });
}


void main() {
  testClientAndServerCloseNoListen(10);
  testClientCloseServerListen(10);
  testClientCloseDelayed(10);
  testClientCloseSendingResponse(10);
}

