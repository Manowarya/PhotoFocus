#pragma once

#include <opencv2/core.hpp>

using namespace cv;

void myTone(Mat& image, float sigma);
void mySaturation(Mat& image, float sigma);
void myBright(Mat image, float sigma);
void myExposition(Mat& image, float sigma);
void myContrast(Mat& image, float sigma);
void myBlur(Mat& image, float sigma);
void myNoise(Mat image, float sigma);
void myVignette(Mat& image, float sigma);
void myAutocorrect(Mat& image);
