#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "iostream"

using namespace cv;
using namespace std;

int main( )
{
    
    Mat image;
    image = imread("text.jpg", CV_LOAD_IMAGE_COLOR);
    
    if(! image.data )
    {
        cout <<  "Could not open or find the image" << std::endl ;
        return -1;
    }
    
    // Create a new matrix to hold the gray image
    Mat gray;
    
    // convert RGB image to gray
    cvtColor(image, gray, CV_BGR2GRAY);
    
    //namedWindow( "Display window", CV_WINDOW_AUTOSIZE );
    //imshow( "Display window", image );
    
//    namedWindow( "Result window", CV_WINDOW_AUTOSIZE );
//    imshow( "Result window", gray );
    
    cv::Mat binaryMat(gray.size(), gray.type());
    
    //Apply thresholding
    cv::threshold(gray, binaryMat, 100, 255, cv::THRESH_BINARY);
    
    imwrite( "gray.jpg", binaryMat );
    
    //waitKey(0);
    return 0;
}