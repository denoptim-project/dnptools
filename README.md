# DENOPTIM's Python tools (dnptools)
Repository for python utilities related with DENOPTIM: the software for De Novo OPTimization of In/organic Molecules (see https://github.com/denoptim-project/DENOPTIM).

## Tools
Here is an overview of the tools included here:
### Socket server running a scoring service
`scoringservice` run a socket server that provides scores to DENOPTIM's <a href="https://github.com/denoptim-project/DENOPTIM/blob/93a58661c9b4a7b71393c32986d55e008fa36f85/src/main/java/denoptim/fitness/descriptors/SocketProvidedDescriptor.java">SocketProvidedDescriptor</a>. The sever allows ultra-fast communication between DENOPTIM and cheminformatics tools written in python. 
Example of usage:

``` (python)
from dnptools import scoringservice

def scoring_function(json_msg):
    smiles_string = json_msg[scoringservice.JSON_KEY_SMILES]
    score = ...do domething to get the score from processing smiles_string...
    return score
    
scoringservice.start(scoring_function, 'localhost', 3863)
```
Note that `localhost` and port number 3863 are just parameters that can be choosen freely, but should be consistent with the settings of any client that wants to communicate with wuch server. The server is a threading server that can deal with multiple clients, like parallel threads running a fitness providing task each.
When you do not need the scoring service any more, use the following to stop the server:
```
scoringservice.stop('localhost',3863)
```

## Install
The package is available on <a href="https://pypi.org/project/dnptools/">pypi</a>, so install it with 
```
pip install dnptools
```
If you use a conda package, remember to do `conda install pip` to make sure you are using the pip instance operating within the conda environment.

## Acknowledgments
The Research Council of Norway is acknowledge for funding.

