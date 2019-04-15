import subprocess
import uuid
import scipy.io.wavfile
from deepspeech import Model
from flask import Flask
from flask import jsonify
from flask import request
from flask_cors import CORS, cross_origin
import io
import scipy.io.wavfile as wav

BEAM_WIDTH = 1024
LM_WEIGHT = 1.5
VALID_WORD_COUNT_WEIGHT = 2.25
N_FEATURES = 26
N_CONTEXT = 9
MODEL_FILE = 'models/output_graph.pb'
ALPHABET_FILE = 'models/alphabet.txt'
LANGUAGE_MODEL =  'models/lm.binary'
TRIE_FILE =  'models/dungtrie'

# "features": {
#       "n_features": 26,
#       "n_context": 9,
#       "beam_width": 1024,
#       "lm_weight": 1.5,
#       "vwc_weight": 2.25
#     }

# TODO: read from config.json

ds = Model(MODEL_FILE, N_FEATURES, N_CONTEXT, ALPHABET_FILE, BEAM_WIDTH)
ds.enableDecoderWithLM(ALPHABET_FILE, LANGUAGE_MODEL, TRIE_FILE, LM_WEIGHT, VALID_WORD_COUNT_WEIGHT)

app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

def predict0(voiceData):
    try:
        fs, audio = wav.read(io.BytesIO(voiceData))
        resultText = ds.stt(audio, fs)
        return {'returncode': 1, 'returnmessage': 'Success 1', 'text': resultText}
    except:
        return {'returncode': -2, 'returnmessage': 'Error when handle data from file wav', 'text': ''}

def predict1(voiceData):
    fileName = 'processing.wav'
    with open(fileName, "wb") as clip:
        clip.write(voiceData)
    try:
        fs, audio = scipy.io.wavfile.read(fileName)
        resultText = ds.stt(audio, fs)
        return {'returncode': 1, 'returnmessage': 'Success 1', 'text': resultText}
    except:
        return {'returncode': -2, 'returnmessage': 'Error when handle data from file wav', 'text': ''}

def predict2(voiceData):
    fileName = 'processing.wav'
    with open(fileName, "wb") as clip:
        clip.write(voiceData)
    try:
        cmdStr = 'deepspeech --model models/output_graph.pb --alphabet models/alphabet.txt --lm models/lm.binary --trie models/dungtrie --audio ' + fileName
        proc = subprocess.Popen(cmdStr, shell=True, stdout=subprocess.PIPE, )
        resultText = proc.communicate()[0].decode("utf-8") 
        return {'returncode': 1, 'returnmessage': 'Success 2', 'text': resultText}
    except:
        return {'returncode': -2, 'returnmessage': 'Error when handle data from file wav', 'text': ''}

@app.route('/file', methods=['POST'])
@cross_origin()
def post1():
    responseData = {'returncode': 0, 'returnmessage': '', 'text': ''}
    if request.method != "POST":
        return jsonify({'returncode': 400, 'returnmessage': 'POST method error', 'text': ''})
    if request.files.get("voice"):
        voiceData = request.files["voice"].read()
        responseData = predict0(voiceData)
    
    return jsonify(responseData)

if __name__ == '__main__':
    app.run()