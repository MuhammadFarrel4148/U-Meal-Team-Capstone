from flask import Flask, request, jsonify, Response
from flask_cors import CORS
import tensorflow as tf
import numpy as np
import cv2
import io
from pathlib import Path
from tensorflow.lite.python.interpreter import Interpreter

# Disable oneDNN optimizations
import os
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'

app = Flask(__name__)
CORS(app)

# Use pathlib to define paths
model_dir = Path("model")
tflite_model_path = model_dir / "detect.tflite"
label_map_path = model_dir / "labelmap.txt"

# Load the TFLite model into memory
interpreter = Interpreter(model_path=str(tflite_model_path))
interpreter.allocate_tensors()

# Get model details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
height = input_details[0]['shape'][1]
width = input_details[0]['shape'][2]
float_input = (input_details[0]['dtype'] == np.float32)
input_mean = 127.5
input_std = 127.5

# Load label map into memory
with label_map_path.open('r') as f:
    labels = [line.strip() for line in f.readlines()]

@app.route('/scan', methods=['POST'])
def predict_image():
    try:
        if 'image' not in request.files:
            return jsonify({'error': 'Image data not found'}), 400

        image_data = request.files['image'].read()
        image = np.frombuffer(image_data, dtype=np.uint8)
        image = cv2.imdecode(image, cv2.IMREAD_COLOR)

        # Preprocess the image
        image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        image_resized = cv2.resize(image_rgb, (input_details[0]['shape'][2], input_details[0]['shape'][1]))
        input_data = np.expand_dims(image_resized, axis=0)

        if float_input:
            input_data = (np.float32(input_data) - input_mean) / input_std

        # Run inference
        interpreter.set_tensor(input_details[0]['index'], input_data)
        interpreter.invoke()

        # Retrieve detection results
        boxes = interpreter.get_tensor(output_details[1]['index'])[0]
        classes = interpreter.get_tensor(output_details[3]['index'])[0]
        scores = interpreter.get_tensor(output_details[0]['index'])[0]

        min_conf = 0.5  # Minimum confidence threshold for displaying results

        detected_labels = []
        for i in range(len(scores)):
            if (scores[i] > min_conf) and (scores[i] <= 1.0):
                object_name = labels[int(classes[i])]
                detected_labels.append(object_name)
        
        return jsonify(detected_labels)

    except Exception as e:
        return jsonify({'error': str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
