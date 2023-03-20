# An example of a tool using the modular scoring server.

import json
import socket
import denoptim.ScoringServer

HOST = "localhost"
PORT = 0xf17  # 3863

# NB: the strings defined here are part of a convention.
JSON_KEY_SMILES = 'SMILES'
JSON_KEY_SCORE = 'SCORE'
JSON_KEY_ERROR = 'ERROR'


def calc_fitness(json_msg):
    try:
        text = json_msg[JSON_KEY_SMILES]
    except KeyError:
        raise Exception(f"Missing {JSON_KEY_SMILES} key in JSON object.")

    num = text.count('C')
    if num > 0:
        response = num**2.5
    else:
        raise json_msg("Unable to calculate score.")
    return response


def get_score(smiles):
    jsonRequest = json.dumps({
        JSON_KEY_SMILES: smiles,
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
    #print(f"Received {jsonResponse}")
    try:
        score = jsonResponse[JSON_KEY_SCORE]
    except KeyError:
        try:
            score = float('NaN')
            print(jsonResponse[JSON_KEY_ERROR])
        except KeyError:
            raise Exception(f"Neither {JSON_KEY_SMILES} nor {JSON_KEY_ERROR} "
                            f"key in JSON object.")
    return score


if __name__ == "__main__":
    print('Hello, from main')
    denoptim.ScoringServer.start(calc_fitness, HOST, PORT)

    print('Now we use the server to do some work...')
    print('Score for C: ', get_score('C'))
    print('Score for CCO: ', get_score('CCO'))
    print('Score for CCCO: ', get_score('CCCO'))

    print('Finally, we close the server')
    denoptim.ScoringServer.stop(HOST, PORT)



