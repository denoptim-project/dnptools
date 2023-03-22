"""scoringservice

This module provides a shortcut to create socket servers able to provide
scores according to the conventions of DENOPTIM
(https://github.com/denoptim-project/DENOPTIM). Namely,
    * use a JSON formatted string (UTF-8) for both request and response,
    * use conventional JSON member keys (See ``JSON_KEY_*`` attributes).

To use this module, first import it:
    ``from dnptools import scoringservice``
then you can start a server that runs ``some_function`` to calculate the score
for any JSON-formatted request sent to ``hostname:port``. The JSON
request is passed to ``some_function`` so the definition of such function
controls what information is used to calculate the score:
    ``scoringservice.start(some_function, hostname, port)``
Once, you are done using the server, you must shut it down like this:
    ``scoringservice.stop(hostname, port)``

"""
import socket
import sys
import json
import socketserver
import time
from threading import Thread
from typing import Tuple

MY_NAME = "scoringservice"


# NB: the strings defined here are part of a convention.
JSON_KEY_SMILES = 'SMILES'
JSON_KEY_SCORE = 'SCORE'
JSON_KEY_ERROR = 'ERROR'

SERVER_START_MAX_TIME = 5  # seconds


class ScoreError(Exception):
    """Formats and exception as a JSON object adhering to DENOPTIM's convention.

    The JSON format is used to communicate any result to the client waiting for
    an answer, i.e., waiting for a score. If an exception occurs, we must
    communicate that the score cannot be produced and why. This method creates
    the JSON response that conveys this information to DENOPTIM. Such response
    is accessible as ``self.json_errmsg``"""
    def __init__(self, message):
        super().__init__(message)
        self.json_errmsg = {JSON_KEY_ERROR: f"#{MY_NAME}: {message}"}


def start(scoring_function, address: Tuple[str, int]):
    """Starts a separate thread that creates and runs the server.

    Parameters
    ----------
    scoring_function :
        The function the server should use to calculate the score for a given
        request.
    address : Tuple[str, int]
        The hostname and port number where to server should accept requests.
    """
    return start(scoring_function, address[0], address[1])


def start(scoring_function, host: str = 'localhost', port: int = 0):
    """Starts a separate thread that creates and runs the server.

    Parameters
    ----------
    scoring_function :
        The function the server should use to calculate the score for a given
        request.
    host : str
        The identifier of the host running the server that should accept
        requests.
        Either a hostname in internet domain notation like ``host.name.org`` or
        an IPv4 address like ``100.50.200.5``.
    port : int
        the port number where the server should accept requests. By default,
        i.e., with a value of 0, we search for an available port.
    """
    # Find available port: we assume it stays available for as long as it takes
    # to start the server.
    if port == 0:
        sock = socket.socket()
        sock.bind((host, 0))
        port = sock.getsockname()[1]
        sock.close()

    # Try to start the server in another thread
    serverThread = Thread(target=__run_server, args=[scoring_function,
                                                     host, port])
    serverThread.start()

    # Verify the server is up before returning
    sock2 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    door_is_closed = True
    start_time = time.time()
    waited_time = 0
    while door_is_closed and (waited_time < SERVER_START_MAX_TIME):
        response = sock2.connect_ex((host, port))
        if response == 0:
            door_is_closed = False
        else:
            time.sleep(1)
            waited_time = time.time() - start_time

    if door_is_closed:
        # noinspection PyBroadException
        try:
            stop((host, port))
        except Exception:
            pass
        raise Exception('Max time for server startup reached. Abandoning.')

    return host, port


def __run_server(scoring_function, host: str, port: int):
    """Start the server and keeps it running forever.

    Parameters
    ----------
    scoring_function :
        The function the server should use to calculate the score for a given
        request.
    host : str
        Either a hostname in internet domain notation like ``host.name.org`` or
        an IPv4 address like ``100.50.200.5``.
    port : int
        the port number.
    """
    CustomHandler = __make_score_request_handler(scoring_function)
    with socketserver.ThreadingTCPServer(
            (host, port),
            CustomHandler
    ) as server:
        server.serve_forever()


def __make_score_request_handler(scoring_function):
    """Factory creating a customized request handler from the given function.

    Parameters
    ----------
    scoring_function :
        The function the handler should use to calculate the score for a given
        request."""
    class ScoreRequestHandler(socketserver.StreamRequestHandler):
        def handle(self):
            message = self.rfile.read().decode('utf8')
            if 'shutdown' in message:
                self.server.shutdown()
                return
            try:
                json_msg = json.loads(message)
            except json.decoder.JSONDecodeError as e:
                return
                raise ScoreError(f"Invalid JSON: {e}")
            answer = ''
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
    return ScoreRequestHandler


def stop(address: Tuple[str, int]):
    """Sends a shutdown request to the server to close it for good.

    Parameters
    ----------
    address : Tuple[str, int]
        the tuple defining the address of the scoring server to stop. for
        example `(host, port)` where `host` is the hostname and `port` the port
        number as an integer.
    """
    try:
        socket_connection = socket.create_connection(address)
        socket_connection.send('shutdown'.encode('utf8'))
    except Exception as e:
        raise Exception('Could not communicate with socket server.', e)


