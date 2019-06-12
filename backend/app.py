import subprocess
import uuid
import scipy.io.wavfile
from deepspeech import Model
from flask import Flask
from flask import jsonify
from flask import request
from flask_cors import CORS, cross_origin
from pydub import AudioSegment

BEAM_WIDTH = 1024
LM_WEIGHT = 0.75
VALID_WORD_COUNT_WEIGHT = 1.85
N_FEATURES = 26
N_CONTEXT = 9
MODEL_FILE = 'models/output_graph.pbmm'
ALPHABET_FILE = 'models/alphabet.txt'
LANGUAGE_MODEL =  'models/vnlm.binary'
TRIE_FILE =  'models/vntrie'
SUPPORT_AUDIO_FORMATS = ['mp3', 'wav']

ds = Model(MODEL_FILE, N_FEATURES, N_CONTEXT, ALPHABET_FILE, BEAM_WIDTH)
ds.enableDecoderWithLM(ALPHABET_FILE, LANGUAGE_MODEL, TRIE_FILE, LM_WEIGHT, VALID_WORD_COUNT_WEIGHT)

app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

def predict1(voiceData):
    fileName = 'processing.wav'
    with open(fileName, 'wb') as clip:
        clip.write(voiceData)
    try:
        fs, audio = scipy.io.wavfile.read(fileName)
        resultText = ds.stt(audio, fs)
        print('--------Text result---------')
        print(resultText)
        print('----------------------------')
        return {'returncode': 1, 'returnmessage': 'Success 1', 'text': resultText}
    except:
        return {'returncode': -2, 'returnmessage': 'Error when handle data from file wav', 'text': ''}

def predict2(voiceData):
    fileName = 'processing.wav'
    with open(fileName, 'wb') as clip:
        clip.write(voiceData)
    try:
        cmdStr = 'deepspeech --model models/output_graph.pbmm --alphabet models/alphabet.txt --lm models/vnlm.binary --trie models/vntrie --audio ' + fileName
        proc = subprocess.Popen(cmdStr, shell=True, stdout=subprocess.PIPE, )
        resultText = proc.communicate()[0].decode('utf-8')
        print('--------Text result---------')
        print(resultText)
        print('----------------------------')
        return {'returncode': 1, 'returnmessage': 'Success 2', 'text': resultText}
    except:
        return {'returncode': -2, 'returnmessage': 'Error when handle data from file wav', 'text': ''}

def predict3(voiceData, fileFormat):
    fileName = 'processing.wav'
    preprocessAudioResult = preprocessAudio(voiceData, fileFormat)
    if preprocessAudioResult == -1:
        return jsonify({'returncode': -3, 'returnmessage': 'Only support .mp3,.wav format', 'text': ''})
    elif preprocessAudioResult == 1:
        with open(fileName, 'wb') as clip:
            clip.write(voiceData)
    try:
        cmdStr = 'deepspeech --model models/output_graph.pbmm --alphabet models/alphabet.txt --lm models/vnlm.binary --trie models/vntrie --audio ' + fileName
        proc = subprocess.Popen(cmdStr, shell=True, stdout=subprocess.PIPE, )
        resultText = proc.communicate()[0].decode('utf-8') 
        return {'returncode': 1, 'returnmessage': 'Success 3', 'text': resultText}
    except:
        return {'returncode': -2, 'returnmessage': 'Error when handle data from file wav', 'text': ''}

def preprocessAudio(voiceData, fileFormat):
    if SUPPORT_AUDIO_FORMATS.count(fileFormat) < 1:
        return -1
    mp3FileName = 'processing.mp3'
    wavFileName = 'processing.wav'
    if fileFormat == SUPPORT_AUDIO_FORMATS[1]:
        return 1
    with open(mp3FileName, 'wb') as clip:
        clip.write(voiceData)
    sound = AudioSegment.from_mp3(mp3FileName)
    sound.export(wavFileName, format=SUPPORT_AUDIO_FORMATS[1])
    return 2

@app.route('/file', methods=['POST'])
@cross_origin()
def post1():
    responseData = {'returncode': 0, 'returnmessage': '', 'text': ''}
    if request.method != 'POST':
        return jsonify({'returncode': 400, 'returnmessage': 'POST method error', 'text': ''})
    fileFormat = request.values.get('type')
    if request.files.get('voice'):
        voiceData = request.files['voice'].read()
        # responseData = predict3(voiceData, fileFormat)
        responseData = predict2(voiceData)
    return jsonify(responseData)

if __name__ == '__main__':
    app.run()