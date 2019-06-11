import numpy as np
import os
import pandas
import tables

from functools import partial
from multiprocessing.dummy import Pool
from util.audio import audiofile_to_input_vector
from util.text import text_to_char_array

def pmap(fun, iterable):
    pool = Pool()
    results = pool.map(fun, iterable)
    pool.close()
    return results


def process_single_file(row, numcep, numcontext, alphabet):
    # row = index, Series
    _, file = row
    features = audiofile_to_input_vector(file.wav_filename, numcep, numcontext)
    features_len = len(features) - 2*numcontext
    transcript = text_to_char_array(file.transcript, alphabet)

    if features_len < len(transcript):
        raise ValueError('Error: Audio file {} is too short for transcription.'.format(file.wav_filename))

    return features, features_len, transcript, len(transcript)


# load samples from CSV, compute features, optionally cache results on disk
def preprocess(csv_files, batch_size, numcep, numcontext, alphabet, hdf5_cache_path=None):
    COLUMNS = ('features', 'features_len', 'transcript', 'transcript_len')

    print('Preprocessing', csv_files)

    source_data = None
    for csv in csv_files:
        file = pandas.read_csv(csv, encoding='utf-8', na_filter=False)
        #FIXME: not cross-platform
        csv_dir = os.path.dirname(os.path.abspath(csv))
        file['wav_filename'] = file['wav_filename'].str.replace(r'(^[^/])', lambda m: os.path.join(csv_dir, m.group(1)))
        if source_data is None:
            source_data = file
        else:
            source_data = source_data.append(file)

    step_fn = partial(process_single_file,
                      numcep=numcep,
                      numcontext=numcontext,
                      alphabet=alphabet)
    out_data = pmap(step_fn, source_data.iterrows())

    print('Preprocessing done')
    return pandas.DataFrame(data=out_data, columns=COLUMNS)
