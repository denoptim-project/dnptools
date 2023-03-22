# DENOPTIM's Python tools (dnptools)
Repository for python utilities related with DENOPTIM: the software for De Novo OPTimization of In/organic Molecules (see https://github.com/denoptim-project/DENOPTIM).

## Tools
Here is an overview of the tools included here:
### Socket server running a scoring service
`scoringservice` run a socket server that provides scores to DENOPTIM's <a href="https://github.com/denoptim-project/DENOPTIM/blob/93a58661c9b4a7b71393c32986d55e008fa36f85/src/main/java/denoptim/fitness/descriptors/SocketProvidedDescriptor.java">SocketProvidedDescriptor</a>. The sever allows low-latency communication between DENOPTIM and scoring functions written in python. In particular, it allows to by-pass the overhead needed to startup a python process from within DENOPTIM's <a href="https://denoptim-project.github.io/DENOPTIM/#FitnessEvaluation">fitness providers</a>. 
Example of usage:

``` (python)
from dnptools import scoringservice

def scoring_function(json_msg):
    smiles_string = json_msg[scoringservice.JSON_KEY_SMILES]
    score = ...do something to get the score from processing smiles_string...
    return score
    
scoringservice.start(scoring_function, 'localhost', 3863)
```
Note that `localhost` and port number 3863 are just parameters that can be choosen freely, but should be consistent with the settings of any client that wants to communicate with such server. The server is a threading server that can deal with multiple clients, like parallel threads running a fitness providing task each.
When you do not need the scoring service any more, use the following to stop the server:
```
scoringservice.stop('localhost',3863)
```

## Install
The package is available on <a href="https://pypi.org/project/dnptools/">pypi</a> and <a href="https://anaconda.org/denoptim-project/dnptools">anaconda</a>, so install it with 
```
pip install dnptools
```
or
```
conda install -c denoptim-project dnptools
```

## License
GNU Affero General Public License v3 or later (AGPLv3+)

## Acknowledgments
The Research Council of Norway is acknowledge for funding.

