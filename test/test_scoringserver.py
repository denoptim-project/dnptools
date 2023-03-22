import json
import math
import socket
import pytest
from dnptools import scoringservice


def scoring_function(json_msg):
    """
    Dummy scoring function that counts the 'C' characters and rises them to
    the 2.5th power.

    Parameters
    ----------
    json_msg : str
        The JSON formatted string that serialized the object to evaluate.
        We assume this JSON object contains member 'SMILES' the value of which
        is the string where we count the 'C' characters.

    Returns
    -------
    The numerical score, i.e., N**2.5 where N is the number of 'C' characters.
    """
    try:
        text = json_msg[scoringservice.JSON_KEY_SMILES]
    except KeyError:
        raise Exception(f"Missing {scoringservice.JSON_KEY_SMILES} key "
                        f"in JSON object.")

    num = text.count('C')
    if num > 0:
        response = num**2.5
    else:
        raise json_msg("Unable to calculate score.")
    return response


def __get_score(smiles, address):
    """
    Wrapper that takes a string and puts it as the value of the JSON member
    'SMILES'.

    Parameters
    ----------
    smiles : str
        the string to use as SMILES.
    address : tuple (host : str, port : int)
        the identifier of the host and port number.

    Returns
    -------
    The numerical value of the score.
    """
    jsonRequest = json.dumps({
        scoringservice.JSON_KEY_SMILES: smiles,
    })
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect(address)
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
        score = jsonResponse[scoringservice.JSON_KEY_SCORE]
    except KeyError:
        try:
            score = float('NaN')
            print(jsonResponse[scoringservice.JSON_KEY_ERROR])
        except KeyError:
            raise Exception(f"Neither {scoringservice.JSON_KEY_SMILES} "
                            f"nor {scoringservice.JSON_KEY_ERROR} "
                            f"key in JSON object.")
    return score


def test_scoring_service():
    address = scoringservice.start(scoring_function)
    score = __get_score('C=C', address)
    assert math.isclose(2**2.5, score)
    scoringservice.stop(address)


def test_busy_address():
    address = scoringservice.start(scoring_function)
    with pytest.raises(Exception):
        scoringservice.start(scoring_function, address)
    scoringservice.stop(address)


