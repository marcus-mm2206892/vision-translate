import tensorflow as tf
from tensorflow import keras
from keras.layers import BatchNormalization, Conv2D, Dense, MaxPooling2D, Flatten, GlobalAveragePooling2D, Dropout
from keras.activations import leaky_relu, relu, sigmoid, tanh, softmax
from keras import Sequential
from keras.regularizers import L2
from PIL import Image, ImageCms
import matplotlib.pyplot as plt
import os
import datetime

#--------------------- Defining constants ---------------------
INPUT_SHAPE = (224, 224, 3)
HEIGHT = INPUT_SHAPE[0]
WIDTH = INPUT_SHAPE[1]
CHANNEL_FIRST = INPUT_SHAPE[-1] != 3

DATASET_PATH = "NursingHomeData\\MYNursingHome"
CLASSES = [x for x in os.listdir(DATASET_PATH)] # assuming all files inside the path are classes folders
CLASSES_NUM = len(CLASSES)

#--------------------- Creating Dataset -----------------------
def augment_image(image, label):
    image = tf.image.random_flip_left_right(image)
    image = tf.image.random_flip_up_down(image)
    image = tf.image.random_brightness(image, max_delta=0.1)
    image = tf.image.random_contrast(image, lower=0.9, upper=1.1)
    image = tf.image.random_saturation(image, lower=0.9, upper=1.1)
    image = tf.image.random_hue(image, max_delta=0.05)
    image = image / 255.0
    image = tf.clip_by_value(image, 0, 1)
    return image, label

BATCH_SIZE = 32 # cannot go higher otherwise tensorflow cuda (the gpu) will run out of memory (OOM)
VALIDATION_SPLIT = 0.15
SEED = 3301

#https://github.com/tensorflow/tensorflow/issues/58534
os.environ["XLA_FLAGS"]="--xla_gpu_strict_conv_algorithm_picker=false"
# os.environ["XLA_FLAGS"]="--xla_gpu_autotune_level=0"
# os.environ["TF_ENABLE_ONEDNN_OPTS"]="1"

base_training_set, base_validation_set = keras.preprocessing.image_dataset_from_directory(
                                                directory=DATASET_PATH,
                                                batch_size=BATCH_SIZE,
                                                image_size=(WIDTH, HEIGHT),
                                                seed=SEED,
                                                crop_to_aspect_ratio=True,
                                                subset="both",
                                                validation_split=VALIDATION_SPLIT)

training_dataset = base_training_set.map(augment_image)
validation_dataset = base_validation_set.map(augment_image)

#--------------------- Creating Model -------------------------
base_model = keras.applications.MobileNet(include_top=False, input_shape=INPUT_SHAPE)

Conv2D(16, 3, activation=relu), 
MaxPooling2D((2, 2), strides=2), 
Conv2D(32, 3, activation=relu), 
Conv2D(32, 3, activation=relu),
MaxPooling2D((2, 2), strides=2),

Conv2D(64, 5, activation=leaky_relu, padding="same"), 
Conv2D(64, 5, activation=leaky_relu, padding="same"), 
Conv2D(64, 5, activation=leaky_relu, padding="same"), 
MaxPooling2D((2, 2), strides=2), 
Conv2D(128, 5, strides=2, activation=leaky_relu, padding="same"), 
Conv2D(256, 5, strides=2, activation=leaky_relu, padding="same"), 
Conv2D(512, 5, strides=2, activation=leaky_relu, padding="same"),
MaxPooling2D((2, 2), strides=2),

base_model.trainable = False
model = Sequential([
    base_model,
    GlobalAveragePooling2D(),
    Dense(512, activation=leaky_relu),
    Dense(256, activation=leaky_relu),
    Dense(100, activation=leaky_relu),
    Dense(CLASSES_NUM, activation=softmax)
])

model.build((None, INPUT_SHAPE[0], INPUT_SHAPE[1], INPUT_SHAPE[2]))

#--------------------- Start Training -------------------------
optimizer = optimizer=keras.optimizers.Adam(learning_rate=0.00025)
model.compile(optimizer, loss='sparse_categorical_crossentropy', metrics=['accuracy'])

BACKUP_PATH = "experm_6\\backup" #path to the folder
CHECKPOINT_PATH = "experm_6\\checkpoint\\model_checkpoint" #path to the folder including the files name
LOGS_PATH = "experm_6\\logs" #path to the folder
CVSLOG_PATH = "experm_6\\csv_logs\\logs" #path to the folder including the files name
MODEL_PATH = "experm_6\\final_model\\model" #path to the folder including the files name

def save_model(model):
    tf_model = tf.keras.models.clone_model(model)
    tf_model.set_weights(model.get_weights())

    # Save the TensorFlow model
    tf_model.save_weights(MODEL_PATH)


backup_callback = keras.callbacks.BackupAndRestore(BACKUP_PATH)
checkpoint_callback = keras.callbacks.ModelCheckpoint(CHECKPOINT_PATH, save_best_only=True, save_weights_only=True)
early_stop = keras.callbacks.EarlyStopping(patience=10, restore_best_weights=True)
tensorboard = keras.callbacks.TensorBoard(LOGS_PATH, write_images=True)
cvs_log = keras.callbacks.CSVLogger(CVSLOG_PATH, append=True)

history = model.fit(training_dataset, epochs=200, callbacks=[tensorboard, cvs_log, backup_callback, checkpoint_callback, early_stop], validation_data=validation_dataset)

#------------------------ After Training -----------------------
def plot_history(history): #from https://stackoverflow.com/questions/66785014/how-to-plot-the-accuracy-and-and-loss-from-this-keras-cnn-model
    # summarize history for accuracy
    plt.plot(history.history['accuracy'])
    plt.plot(history.history['val_accuracy'])
    plt.title('model accuracy')
    plt.ylabel('accuracy')
    plt.xlabel('epoch')
    plt.legend(['Train', 'Validation'], loc='upper left')
    plt.show()
    # summarize history for loss
    plt.plot(history.history['loss'])
    plt.plot(history.history['val_loss'])
    plt.title('model loss')
    plt.ylabel('loss')
    plt.xlabel('epoch')
    plt.legend(['Train', 'Validation'], loc='upper left')
    plt.show()

save_model(model)

plot_history(history)