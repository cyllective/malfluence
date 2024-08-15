import http.server
import socketserver
import base64


class HTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        self.end_headers()

    def do_POST(self):
        content_length = int(self.headers["Content-Length"])
        post_data = self.rfile.read(content_length)

        print(post_data.decode("utf-8"))

        self.send_response(200)
        self.end_headers()
        self.wfile.write(b"OK")


def run(
    server_class=http.server.HTTPServer, handler_class=HTTPRequestHandler, port=9999
):
    server_address = ("", port)
    httpd = server_class(server_address, handler_class)
    httpd.serve_forever()


if __name__ == "__main__":
    run()
