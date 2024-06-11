import os
import tensorflow as tf

# Define the directory where the model is stored
model_dir = "model"

# Load the TensorFlow SavedModel from the specified directory
model = tf.saved_model.load(export_dir=model_dir)

# Print the model details to verify it's loaded
print("Model loaded successfully.")
print(model)
