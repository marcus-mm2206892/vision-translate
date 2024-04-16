# VisionTranslate: Enhancing Visual Accessibility

## Introduction
VisionTranslate is an Android application designed to empower visually impaired individuals with the ability to recognize objects and translate text into speech. By leveraging advanced machine learning and image processing technologies, VisionTranslate offers a user-friendly interface to interact with their environment more independently.

## Features
### Image Classification: 
Utilize your phone's camera or gallery to identify objects with our trained CNN model or a pre-trained CNN model of your liking.
### Text Recognition: 
Instantly convert text from images to speech in your preferred language.
Language Translation: Easily translate the recognized text into multiple languages.
Accessible UI: Large buttons and voice feedback ensure the app is easy to navigate.

## Project Demo

Note: The videos are laggy due to the limitations of my computer, which struggles to run the program and record simultaneously. However, it's important to note that the lag seen in the video is not representative of the performance of the application itself. In practice, the application runs smoothly and efficiently.

### Application Demo (Using ImageNet Dataset)
[!(https://i.ytimg.com/vi/Hc79sDi3f0U/maxresdefault.jpg)](https://github.com/Verayzon/vision-translate/assets/118662867/1026042b-4a54-484e-9c10-8875e0d34ed3)

### Image Classification (Using Our Custom Model [MyNursingHome Dataset])
[!(https://i.ytimg.com/vi/Hc79sDi3f0U/maxresdefault.jpg)](https://github.com/Verayzon/vision-translate/assets/118662867/6aef3089-7852-4fa9-83eb-e86130601a4d)

### Character Recognition
[!(https://i.ytimg.com/vi/Hc79sDi3f0U/maxresdefault.jpg)](https://github.com/Verayzon/vision-translate/assets/118662867/e47eef33-0817-4072-b321-c20177ef63e6)

## Prerequisites

Android Studio

An Android device or emulator

## Installation
Open the project in Android Studio.
Build the project to resolve dependencies.
Run the application on your Android device or emulator.

## IMPORTANT NOTES
In the current file for the image recognition, we are using a custom model that we made (also attached in the repository) MyNursingHome CNN Model. The "MYNursingHome" dataset is a vital resource for the development of assistive technologies through computer vision, particularly for disabled individuals. It offers a substantial collection of fully labeled images of everyday objects found in elderly care environments. With 37,500 images across 25 categories, researchers can use this dataset to create object detection systems that facilitate greater independence for the visually impaired, helping them navigate indoor spaces with confidence. Its practical focus on items surrounding the elderly makes it a unique and valuable tool for innovating assistive devices and enhancing the quality of life for those without vision. The dataset can be downloaded here: [MyNursingHome](https://data.mendeley.com/datasets/fpctx3svzd/1).

`If you wish to use another dataset with another CNN model for the image recognition, it is possible. We have also tried using the MobileNet model with the ImageNet dataset that can classify about 1000 classes. To use it, uncomment the classifyImage function and the getClasses function for the ImageNet, and then comment the classifyImage function and the getClasses function for the MyNursingHome.`

## If you want to use the ImageNet dataset, please follow this inside imageClassifierPage.java:
![Uncomment the classifyImage function for Imagenet](https://github.com/Verayzon/vision-translate/assets/118662867/0bb780ec-1257-464a-bc1b-46d7ac88d01b)

![Uncomment the getClasses function for Imagenet](https://github.com/Verayzon/vision-translate/assets/118662867/54997507-ca77-4a83-91aa-328f96e122c2)

## Usage
Select the desired function on the main screen. Point your device's camera towards an object or text, and the app will process and vocalize the information. Double-tap the screen to select options.

## Contributing
We welcome contributions and suggestions!

## Acknowledgements
MyNursingHome dataset

Google ML Kit

TensorFlow Community

## Creators:
Marcus Wein Monteiro

Mohammed Laith Alfaiad
