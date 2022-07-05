# Table of Contents
* [Overview](#overview)
* [Installation](#installation)
* [Configuration for New images](#configuration-for-new-Images)
* [API](#api)


# Overview
ImageScan is an open-source library for developers who are interested in creating Android apps that support the digital curation and interpretation of [tests (Images)]ImageScan provides the following functionality:

**1. Real-Time Quality Checking During Image Capture**  
ImageScan uses image processing to check the quality of images intercepted from the smartphone's camera while the user moves their smartphone over the Image. ImageScan provides functions for checking the blurriness and lighting of incoming camera frames. If the image is in the camera's field-of-view, ImageScan also checks the scale and orientation of the Image in the image. To help end-users capture the clearest image possible, ImageScan intelligently generates instructions based on these quality checks.

**2. Robust Result Interpretation**  
Assuming a suitable image has been captured, ImageScan can post-process the image to emphasize any faint lines that may appear on the immunoassay. The end-user can view the post-processed image for themselves to make a more informed decision about the Image results. Alternatively, ImageScan provides a lightweight algorithm that interprets the test results on the end-user's behalf.

ImageScan uses a feature-matching approach for Image recognition, allowing ImageScan to be quickly adapted to new Image designs. Unlike model-driven approaches that require a dataset of example images for model training, ImageScan only requires a single example image and some additional metadata (e.g., relative position and meaning of each line) to detect and interpret Images. ImageScan is intentially designed so that all of the algorithms can run on the smartphone, which means that there is no need to upload confidential photos to a server to use this library.

**Disclaimers:** 
* Although ImageScan has been tested through multiple in-test studies and real-world deployments, this library has not been FDA-approved.
* This library requires that target smartphone's support Android's [Camera2 API](https://developer.android.com/reference/android/hardware/camera2/package-summary), which supports control over the camera's hardware.

# Installation
ImageScan utilizes [OpenCV for Android](https://opencv.org/android/) for many of the image processing steps, which in turn relies on Android's [Native Development Kit (NDK)](https://developer.android.com/ndk/). Therefore, it is beyond the scope of the current project to export ImageScan's functionality as a `.jar` file. Instead, there are two options for getting started with ImageScan:
* **No existing project:** If you are making a smartphone app from scratch, you can simply clone the repository directly and build your app on top of what has already been provided. This repository has all of the dependencies properly configured along with a fully-functioning app that developers can use to get started.
* **Existing project:** If you have a smartphone app that has already been made and you are looking to add ImageScan to it, you will still need to add OpenCV for Android to your project. The [official tutorial](https://docs.opencv.org/2.4/doc/tutorials/introduction/android_binary_package/O4A_SDK.html) for doing this is fairly outdated, but there are plenty of other tutorials out there depending on your environment. Once you have done that, copy the following folders and files to your project (at the same path):
  * [`src/.../core/*`](app/src/main/java/edu/washington/cs/ubicomplab/Image_reader/core)
  * [`src/.../utils/*`](app/src/main/java/edu/washington/cs/ubicomplab/Image_reader/utils)
  * [`src/.../views/*`](app/src/main/java/edu/washington/cs/ubicomplab/Image_reader/views)
  * [`assets/config.json`](app/src/main/assets/config.json)

### Troubleshooting:
* **Unable to locate NDK installation** If you have not already installed NDK, follow the instructions at this [link](https://developer.android.com/studio/projects/install-ndk) to do so. Once that is done, NDK should be installed at a path that either looks like `C:/Users/username/AppData/Local/Android/ndk/xx.x.xxxxxxx` (Windows) or `/Users/username/Library/Android/sdk/ndk/xx.x.xxxxxxx` (OSX). Referring to this filepath as `NDK_HOME`, there are two ways to point your project to this filepath: 
  1. Go to **File > Project Structure > SDK Location** and then set the path variable in **Android NDK Location** to `NDK_HOME`.
  2. Open the `local.properties` file and add the following line: `ndk.dir=NDK_HOME`
  

Extending ImageScan to accommodate a new Image is a matter of three steps: (1) adding a clean photo of the Image, (2) identifying some regions-of-interest using an image-editing program (e.g., Photoshop, GIMP), and (3) adding that information and other metadata to a configuration file. For detailed instructions on how to extend ImageScan for a new Image, visit this [link](readme_assets/Image_configuration.md).

**Note:** ImageScan uses a feature-matching approach to locate the target Image's design. A detailed explanation of this approach can be found at this [article](https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_feature2d/py_features_meaning/py_features_meaning.html) by the OpenCV Foundation. As that article explains, feature-matching is less compatable with certain visual characteristics. In the context of Images, this includes:
* Blank cassettes with little or no lettering
* Inconsistent patterns (e.g., QR code, bar code)





