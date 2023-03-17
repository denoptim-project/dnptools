# An example of socket server accepting only requests in JSON format (string)
# and replying with the same format.
#
# Usage:
# 1) start this server with `python main.py`
# 2) test it using a dummy client with
#    `echo "{\"SMILES\": \"CCO\"}" | nc localhost 3863`
#    Be careful about escaping the double quotation marks properly.
# 3) use any other client sending requests to localhost 3863
# 4) CTRL+C to kill the server
#

import sys
import json
import socketserver

MY_NAME = "SocketServer"
HOST = "localhost"
PORT = 0xf17  # 3863

# NB: the strings defined here are part of a convention.
JSON_KEY_SMILES = 'SMILES'
JSON_KEY_SCORE = 'SCORE'
JSON_KEY_ERROR = 'ERROR'


class FitnessError(Exception):
    def __init__(self, message):
        super().__init__(message)
        self.json_errmsg = { JSON_KEY_ERROR : f"#{MY_NAME}: {message}" }


def calc_fitness(message):
    try:
        json_msg = json.loads(message)
        text = json_msg[JSON_KEY_SMILES]
    except json.decoder.JSONDecodeError as e:
        raise FitnessError(f"Invalid JSON: {e}")
    except KeyError:
        raise FitnessError(f"Missing {JSON_KEY_SMILES} key in JSON object.")

    num = text.count('C')
    if num > 0:
        response = json.dumps({ 
            JSON_KEY_SCORE : num**2.5,
        })
    else:
        raise FitnessError("Unable to calculate fitness.")

    # server logging
    print(f"for {JSON_KEY_SMILES} {text} I reply: {response.strip()}")
    return response


class FitnessHandler(socketserver.StreamRequestHandler):
    def handle(self):
        message = self.rfile.read().decode('utf8')
        try:
            answer = calc_fitness(message)
        except FitnessError as e:
            print('Error:', e, file=sys.stderr)
            answer = json.dumps(e.json_errmsg)
        finally:
            answer += '\n'
            self.wfile.write(answer.encode('utf8'))


if __name__ == "__main__":
    with socketserver.ThreadingTCPServer(
        (HOST,PORT), 
        FitnessHandler
    ) as server:
        server.serve_forever()
