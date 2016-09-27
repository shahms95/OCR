//
// Created by maulik on 6/8/16.
//

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>
#include <limits>

using namespace cv;
using namespace std;

Mat rotateImage(const Mat source, double angle, int border=100)
{
    Mat bordered_source;
    int top,bottom,left,right;
    top=bottom=left=right=border;
    copyMakeBorder( source, bordered_source, top, bottom, left, right, BORDER_CONSTANT, Scalar(255));
    Point2f src_center(bordered_source.cols/2.0F, bordered_source.rows/2.0F);
    Mat rot_mat = getRotationMatrix2D(src_center, angle, 1.0);
    Mat dst;
    warpAffine(bordered_source, dst, rot_mat, bordered_source.size());
    return dst;
}

double rowVarianceGrayscale(Mat imgf){
    vector<int> row_sum(imgf.rows);

    double total_sum = 0;

    for(int row = 0; row < imgf.rows; row++){
        for(int col = 0; col < imgf.cols; col++){
            row_sum[row] += (int)imgf.at<uchar>(row, col);
        }
        total_sum += row_sum[row];
    }

    double avg_sum = total_sum/imgf.rows;
    double var = 0;
    for(int row = 0; row < imgf.rows; row++){
        var += (row_sum[row]-avg_sum)*(row_sum[row]-avg_sum);
    }
    return var;
}

double skewAngle(Mat imgf){
    double max_var = numeric_limits<double>::min();
    double best_skew = 0;
    for(double rot = -12; rot < 13;rot=rot+.2){
        Mat imgf_rot = rotateImage(imgf, rot);
        double var = rowVarianceGrayscale(imgf_rot);
        if(var > max_var){
            max_var = var;
            best_skew = rot;
        }
    }
    return best_skew;
}

int rowLength(Mat imgf){
    vector<int> row_sum(imgf.rows);

    double total_sum = 0;

    for(int row = 0; row < imgf.rows; row++){
        for(int col = 0; col < imgf.cols; col++){
            row_sum[row] += (int)imgf.at<uchar>(row, col);
        }
        total_sum += row_sum[row];
    }
    double avg_sum = total_sum/imgf.rows;
    for(int row = 0; row < imgf.rows; row++){
        row_sum[row] -= avg_sum;
    }
}

/*extern "C" JNIEXPORT void JNICALL Java_com_almalence_plugins_processing_ocr_OCR_SkewCorrection
(
	JNIEnv* env,
	jobject thiz,
	jlong input,
	jlong output
)*/
//	jclass inp = env->GetObjectClass(input);
//	jclass out = env->GetObjectClass(output);

//void skewcorrection(Mat inp, Mat &out){

extern "C" JNIEXPORT void JNICALL Java_com_almalence_plugins_processing_ocr_OCR_SkewCorrection
(
	JNIEnv* env,
	jobject thiz,
	jintArray compressed_frame,
	jint imagesAmount,
	jint imageWidth,
	jint imageHeight
)
{
    Mat& inp = *(Mat*) input;
    Mat& out = *(Mat*) output;

    Mat imagerot = rotateImage(inp, skewAngle(inp));
    threshold(imagerot, out, 0, 255, CV_THRESH_BINARY | CV_THRESH_OTSU);
}

int main( int argc, char** argv )
{


    return 0;

}
