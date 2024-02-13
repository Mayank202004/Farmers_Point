from flask import Flask, render_template, request
from tensorflow.keras.models import load_model
import numpy as np
from PIL import Image

app = Flask(__name__)


# Specifying Paths for model 
MODEL_PATHS = {
    "potato": "potato.h5",
    "wheat": "wheat.h5",
    "cotton": "cotton.h5",
    "grapes": "grapes.h5",
    "guava": "guava.h5",
    "rice": "rice.h5",
    "cucumber": "cucumber.h5",
    "tomato": "tomato.h5",
    "sugarcane": "sugarcane.h5",
}

# Specifying Class labels for each model
CLASS_LABELS = {
    "potato": ["Early Blight", "Healthy", "Late Blight"],
    "wheat": ["Wheat Brown Rust", "Wheat Healthy", "Wheat Yellow Rust"],
    "cotton": ["Cotton Bacterial Blight", "Cotton Curl Virus", "Cotton Fusarium Wilt", "Cotton Healthy"],
    "grapes": ["Grape Black Measles", "Grape Black Rot", "Grape Healthy"],
    "guava": ["Healthy Guava","Guava Phytopthora","Guava Red Rust,","Guava Scab","Guava Styler and Root"],
    "rice": ["Rice Bacterial blight","Rice Blast","Rice Brownspot","Rice Tungro"],
    "cucumber": ["Cucumber Anthracnose","Cucumber Bacterial Wilt","Cucumber Belly Rot","Cucumber Downy Mildew","Cucumber Gummy Stem Blight","Healthy Cucumber","Healthy Cucumber Leaf","Cucumber Pythium Fruit Rot"],
    "tomato": ["Tomato Bacterial spot","Tomato Early blight","Tomato Late Blight","Tomato Leaf Mold","Septoria leaf spot","Tomato Target Spot","Tomato Yellow Leaf Curl Virus","Tomato mosaic virus","Two-spotted spider mite","Tomato Healthy"],
    "sugarcane": ["Healthy Sugarcane","Sugarcane Mosaic","Sugarcane RedRot","Sugarcane Rust","Sugarcane Yellow"],
}

# Load the models and class labels outside of the route function
loaded_models = {}
model_class_labels = {}

for model_name, model_path in MODEL_PATHS.items():
    loaded_models[model_name] = load_model(model_path)
    model_class_labels[model_name] = CLASS_LABELS[model_name]

@app.route('/', methods=['GET', 'POST'])
def index():
    result = None
    selected_model = "potato"  # Default model
    # load required model based on drop down
    if request.method == 'POST':
        if 'image' in request.files:
            image_file = request.files['image']
            selected_model = request.form['plant_type']

            if image_file.filename != '':
                if selected_model and selected_model != "select from plant":
                    img = Image.open(image_file)
                    img = img.convert('RGB')
                    img = img.resize((256, 256))
                    img = np.array(img)

                    model = loaded_models.get(selected_model)
                    class_labels = model_class_labels.get(selected_model)

                    if model and class_labels:
                        prediction = model.predict(np.expand_dims(img, axis=0))
                        predicted_class_index = np.argmax(prediction[0])
                        predicted_class = class_labels[predicted_class_index]
                        result = predicted_class
                else:
                    result = "Please select plant type first"
            else:
                result = "Insert an Image first"
    return render_template('index.html', result=result, selected_model=selected_model)

if __name__ == '__main__':
    app.run(debug=True)
