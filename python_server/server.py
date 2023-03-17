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

import json
import socketserver


def calc_fitness(json_obj):
    # NB: the use of the string `SMILES` is part of a convention.
    text = json_obj['SMILES']
    try:
        num = text.count('C')
        if num > 0:
            # NB: the use of the string `SCORE` is part of a convention.
            json_str = json.dumps({
                'SCORE': num**2.5,
            })
        else:
            # NB: the use of the string `ERROR` is part of a convention.
            json_str = json.dumps({
                'ERROR': '#SocketServer: unable to calculate fitness.',
            })
    except:
        # NB: the use of the string `ERROR` is part of a convention.
        json_str = json.dumps({
            'ERROR': '#SocketServer: scoring function is broken!',
        })
    return f'{json_str}\n'


class FitnessHandler(socketserver.StreamRequestHandler):
    def handle(self):
        line = self.rfile.readlines(1)[0].strip().decode('utf8')
        try:
            json_obj = json.loads(line)
            answer = calc_fitness(json_obj)
            # NB: the use of the string `SMILES` is part of a convention.
            print(f"--- for SMILES {json_obj['SMILES']} I reply: {answer.strip()} ---")
        except:
            print(f"ERROR: could not load JSON from {line}")
            json_str = json.dumps({
                'ERROR': '#SocketServer: could not load JSON',
            })
            answer = f'{json_str}\n'
        self.wfile.write(answer.encode('utf8'))

if __name__ == "__main__":
    HOST, PORT = "localhost", 0xf17  # 3863
    with socketserver.ThreadingTCPServer((HOST,PORT), FitnessHandler) as server:
        server.serve_forever()
