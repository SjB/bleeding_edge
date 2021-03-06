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

void testServerRequest(void handler(server, request), {int bytes}) {
  HttpServer.bind().then((server) {
    server.listen((request) {
      handler(server, request);
    });

    var client = new HttpClient();
    // We only close the client on either
    // - Bad response headers
    // - Response done (with optional errors in between).
    client.get("127.0.0.1", server.port, "/")
      .then((request) => request.close())
      .then((response) {
        int received = 0;
        response.listen(
            (data) => received += data.length,
            onDone: () {
              if (bytes != null) Expect.equals(received, bytes);
              client.close();
            },
            onError: (error) {
              Expect.isTrue(error.error is HttpParserException);
            });
      })
      .catchError((error) {
         client.close();
      }, test: (e) => e is HttpParserException);
  });
}

void testResponseDone() {
  testServerRequest((server, request) {
    request.response.close();
    request.response.done.then((response) {
      Expect.equals(request.response, response);
      server.close();
    });
  });
}

void testResponseAddStream() {
  int bytes = new File(new Options().script).lengthSync();

  testServerRequest((server, request) {
    request.response.writeStream(new File(new Options().script).openRead())
        .then((response) {
          response.close();
          response.done.then((_) => server.close());
        });
  }, bytes: bytes);

  testServerRequest((server, request) {
    request.response.writeStream(new File(new Options().script).openRead())
        .then((response) {
          request.response.writeStream(new File(new Options().script).openRead())
              .then((response) {
                response.close();
                response.done.then((_) => server.close());
              });
        });
  }, bytes: bytes * 2);

  testServerRequest((server, request) {
    var controller = new StreamController();
    request.response.writeStream(controller.stream)
        .then((response) {
          response.close();
          response.done.then((_) => server.close());
        });
    controller.close();
  }, bytes: 0);
}

void testBadResponseAdd() {
  testServerRequest((server, request) {
    request.response.contentLength = 0;
    request.response.writeBytes([0]);
    request.response.close();
    request.response.done.catchError((error) {
      server.close();
    }, test: (e) => e is HttpException);
  });

  testServerRequest((server, request) {
    request.response.contentLength = 5;
    request.response.writeBytes([0, 0, 0]);
    request.response.writeBytes([0, 0, 0]);
    request.response.close();
    request.response.done.catchError((error) {
      server.close();
    }, test: (e) => e is HttpException);
  });

  testServerRequest((server, request) {
    request.response.contentLength = 0;
    request.response.writeBytes(new Uint8List(64 * 1024));
    request.response.writeBytes(new Uint8List(64 * 1024));
    request.response.writeBytes(new Uint8List(64 * 1024));
    request.response.close();
    request.response.done.catchError((error) {
      server.close();
    }, test: (e) => e is HttpException);
  });
}

void testBadResponseClose() {
  testServerRequest((server, request) {
    request.response.contentLength = 5;
    request.response.close();
    request.response.done.catchError((error) {
      server.close();
    }, test: (e) => e is HttpException);
  });

  testServerRequest((server, request) {
    request.response.contentLength = 5;
    request.response.writeBytes([0]);
    request.response.close();
    request.response.done.catchError((error) {
      server.close();
    }, test: (e) => e is HttpException);
  });
}

void main() {
  testResponseDone();
  testResponseAddStream();
  testBadResponseAdd();
  testBadResponseClose();
}
