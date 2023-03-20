# An example of a tool using the modular scoring server.

import json
import socket
from denoptim import ScoringServer


HOST = "localhost"
PORT = 0xf17  # 3863


def scoring_function(json_msg):
    try:
        text = json_msg[ScoringServer.JSON_KEY_SMILES]
    except KeyError:
        raise Exception(f"Missing {ScoringServer.JSON_KEY_SMILES} key "
                        f"in JSON object.")

    num = text.count('C')
    if num > 0:
        response = num**2.5
    else:
        raise json_msg("Unable to calculate score.")
    return response


def get_score(smiles):
    jsonRequest = json.dumps({
        ScoringServer.JSON_KEY_SMILES: smiles,
    })
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((HOST, PORT))
        s.sendall(jsonRequest.encode('utf8'))
        s.shutdown(socket.SHUT_WR)
        response = b''
        while True:
            data = s.recv(1024)
            if not data:
                break
            response += data
        s.close()
    jsonResponse = json.loads(response)
    try:
        score = jsonResponse[ScoringServer.JSON_KEY_SCORE]
    except KeyError:
        try:
            score = float('NaN')
            print(jsonResponse[ScoringServer.JSON_KEY_ERROR])
        except KeyError:
            raise Exception(f"Neither {ScoringServer.JSON_KEY_SMILES} "
                            f"nor {ScoringServer.JSON_KEY_ERROR} "
                            f"key in JSON object.")
    return score


if __name__ == "__main__":
    print('Hello, from main')
    ScoringServer.start(scoring_function, HOST, PORT)

    print('Now we use the server to do some work...')
    print('Score for C: ', get_score('C'))
    print('Score for CCO: ', get_score('CCO'))
    print('Score for C(C)CCO: ', get_score('C(C)CCO'))

    print('Finally, we close the server')
    ScoringServer.stop(HOST, PORT)