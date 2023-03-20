import socket
import sys
import json
import socketserver
from threading import Thread

MY_NAME = "ScoringServer"

# NB: the strings defined here are part of a convention.
JSON_KEY_SMILES = 'SMILES'
JSON_KEY_SCORE = 'SCORE'
JSON_KEY_ERROR = 'ERROR'


class ScoreError(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.json_errmsg = { JSON_KEY_ERROR : f"#{MY_NAME}: {message}" }


def make_score_request_handler(scoring_function):
    class ScoreRequestHandler(socketserver.StreamRequestHandler):
        def handle(self):
            message = self.rfile.read().decode('utf8')
            if 'shutdown' in message:
                self.server.shutdown()
                return
            try:
                json_msg = json.loads(message)
            except json.decoder.JSONDecodeError as e:
                raise ScoreError(f"Invalid JSON: {e}")
            try:
                score = scoring_function(json_msg)
                answer = json.dumps({
                    JSON_KEY_SCORE: score,
                })
            except ScoreError as e:
                print('Error:', e, file=sys.stderr)
                answer = json.dumps(e.json_errmsg)
            finally:
                answer += '\n'
                self.wfile.write(answer.encode('utf8'))
                self.wfile.close()
    return ScoreRequestHandler


def start(scoring_function, host: str = "localhost", port: int = 0xf17):
    serverThread = Thread(target=run_server, args=[scoring_function, host, port])
    serverThread.start()


def stop(host: str = "localhost", port: int = 0xf17):
    socket_connection = socket.create_connection((host, port))
    socket_connection.send('shutdown'.encode('utf8'))


def run_server(scoring_function, host: str = "localhost", port: int = 0xf17):
    CustomHandler = make_score_request_handler(scoring_function)
    with socketserver.ThreadingTCPServer(
            (host,port),
            CustomHandler
    ) as server:
        server.serve_forever()
