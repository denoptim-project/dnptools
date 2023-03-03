import json
import socketserver


def calc_fitness(text):
    try:
        num = text.count('c')
        if num > 0:
            json_str = json.dumps({
                'FITNESS': f"{num**2.5}"
            })
        else:
            json_str = json.dumps({
                'MOL_ERROR': '#FitnessProvider: unable to calculate fitness.'
            })
    except:
        json_str = json.dumps({
            'FATAL_ERROR': 'Fitness provider is broken!'
        })
    return f'{json_str}\n'


#def calc_fitness(text):
#    try:
#        score = text.count('c')**2.5
#        return f"{score}\n"
#    except:
#        return "0.0\n"

ENC = 'utf8' # enough with ascii?

class FitnessHandler(socketserver.StreamRequestHandler):
    def handle(self):
        for line in self.rfile:
            print("String:",line.strip().decode(ENC))
            json_obj = json.loads(line)
            print(f"=== {self.client_address[0]} wrote:", end=" ")
            print(json_obj, end=" ")
            print("===")
            smiles = json_obj['SMILES']
            score = calc_fitness(smiles)
            print(f"--- for SMILES {smiles} I reply: {score.strip()} ---")
            self.wfile.write(score.encode(ENC))

if __name__ == "__main__":
    HOST, PORT = "localhost", 0xf17  # 3863
    #with socketserver.TCPServer((HOST,PORT), FitnessHandler) as server:
    #    server.serve_forever()
    with socketserver.ThreadingTCPServer((HOST,PORT), FitnessHandler) as server:
        server.serve_forever()
