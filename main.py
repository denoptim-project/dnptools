import json
import socketserver


def calc_fitness(json_obj):
    text = json_obj['SMILES']
    try:
        num = text.count('c')
        if num > 0:
            json_str = json.dumps({
                'FITNESS': num**2.5,
                'Client': json_obj['Client']
            })
        else:
            json_str = json.dumps({
                'MOL_ERROR': '#FitnessProvider: unable to calculate fitness.',
                'Client': json_obj['Client']
            })
    except:
        json_str = json.dumps({
            'FATAL_ERROR': 'Fitness provider is broken!',
            'Client': json_obj['Client']
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
            #print("String:",line.strip().decode(ENC))
            json_obj = json.loads(line)
            #print(f"=== {self.client_address[0]} wrote:", end=" ")
            #print(json_obj, end=" ")
            #print("===")
            answer = calc_fitness(json_obj)
            print(f"--- for SMILES {json_obj['SMILES']} I reply: {answer.strip()} ---")
            self.wfile.write(answer.encode(ENC))

if __name__ == "__main__":
    HOST, PORT = "localhost", 0xf17  # 3863
    #with socketserver.TCPServer((HOST,PORT), FitnessHandler) as server:
    #    server.serve_forever()
    with socketserver.ThreadingTCPServer((HOST,PORT), FitnessHandler) as server:
        server.serve_forever()
