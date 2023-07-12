# ADAS on Android

This is a POC project that tackles a real world problem of making roads safer. It had several iterations but the final product was made using kotlin so that it can deployed on android smartphones to make it more accessible. This entire product uses one singular camera present on an android smartphone.

## Features:

These features were implemented in the final product:
1. Collision Warning
2. Traffic Sign Detection
3. Traffic Light Detection
4. Pedastrian Detection

### 1. Collision Warning:

The system triggered an audio alert for whenever it sensed a collision was going to happen. The algorithm developed for this worked as follows: Whenever the area of the bounding box of a detected car started to change at a greater rate than a certain threshold, it would trigger an alert. It is to be noted that it didn't just give an alert based on the area of the box but rather the change of area of the box. For this I compared the area of the bounding boxes with itself after every 5 frames for the sake of better performance, also, since the threshold was variable we could change it so that it can be used for slow driving applications such as city driving and high speed aplications such as high way driving.

### 2. Traffic Sign Detection:

The system generated alerts based on detected traffic signs. For example, it could generate an alert for updated speed limit, speed bump, pedestrian crossing etc. For this POC I implemented a general alert for traffic signs as a whole.

### 3. Traffic Light Detection:

The system generated alerts for red and green traffic signs for helping out colour imapired indivduals.

### 4. Pedestrain Detection:

The system generated alerts for detected pedestrains.

## Selecting Deep Learning Model:

Several deep learning models were trained and used for this project. The final implemenation used an SSD-MobilenetV1, a tflite based model. In thee arlier python iterations I tested the yolo8n and yolov7n models. The nano versions of these models were specifically used because of hardware constraints as my laptop did not have a GPU. Furthermore, it should be noted that both of these models are based on pytorch framework and altough using tensorflow would have been arguably easier, a newer and more efficient version of yolo was not available on tensorflow. This early implementation of the project was to figure out the algorithms and workings for the implemented features. Later when moving on to deployment on android, I tested out the yolo8n and yolov7n after exporting them to a suitable tflite format. This caused severe performance issues so we moved to an implementation in which I tested the yolov4tiny and the yolov2tiny models. yolov2tiny gave a significant performance increase but the best performance was acheived using SSDMobilenetv1. 

## Project Iterations:

### First Iteration:

The first iteration of the project was done on python. This was done to figure out the basics of how the features will be implemented since I am proficient on Python than other programming languages. Yolov8n and Yolov7n models were trained and used for this project. The models were trained on a dataset based on bangladesh roads: https://www.kaggle.com/datasets/nazmultakbir/vehicle-detection-bangladeshi-roads

### Second iteration:

The second iteration was done on flutter but due to optimization issues, desirable performance was not obtained and thus kotlin was chosen as the platform of choice.

### Third iteration:

The third and final iteration was done on kotlin. This used an SSDMobilenetv1 model and performed all the features mentioned above. The model was not trained in this instance, rather the pretrained model was used as it had all the needed classes for a POC. I filtered out the unneeded classes and produced outputs based on specific classes.



