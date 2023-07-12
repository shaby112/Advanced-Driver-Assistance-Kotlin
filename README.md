# ADAS on Android

This is a POC project that tackles a real world problem of making roads safer. It had several iterations but the final product was made using kotlin so that it can deployed on android smartphones to make it more accessible. 

## Features:

These features were implemented in the final product:
1. Collision Warning
2. Traffic Sign Detection
3. Traffic Light Detection
4. Pedastrian Detection

## Selecting Deep Learning Model:

Several deep learning models were trained and used for this project. The final implemenation used an SSD-MobilenetV1, a tflite based model. In thee arlier python iterations I tested the yolo8n and yolov7n models. The nano versions of these models were specifically used because of hardware constraints as my laptop did not have a GPU. Furthermore, it should be noted that both of these models are based on pytorch framework and altough using tensorflow would have been arguably easier, a newer and more efficient version of yolo was not available on tensorflow. This early implementation of the project was to figure out the algorithms and workings for the implemented features. Later when moving on to deployment on android, I tested out the yolo8n and yolov7n after exporting them to a suitable tflite format. This caused severe performance issues so we moved to an implementation of 
