/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details. 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA  
 */
 
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <qpixmap.h>
#include <qimage.h>
#include <qcanvas.h>
#include <qapplication.h>
#include <qpainter.h>
#include <qmainwindow.h>

/**
* CustomCanvas
**/
class CustomCanvas : public QWidget {
	public:
		CustomCanvas (QWidget *parent, const char *name = 0, int w = 0, int h = 0);
		QImage *qimage;

	protected:
		/**
 		* Repaint the window using an off-screen buffer (a QPixmap). 
 		**/
		void paintEvent (QPaintEvent *);
		void mousePressEvent(QMouseEvent *);
		void mouseReleaseEvent(QMouseEvent *);
		void mouseMoveEvent(QMouseEvent *);
		void keyPressEvent(QKeyEvent *);
		void keyReleaseEvent(QKeyEvent *);
		void closeEvent(QCloseEvent *);
  
};

static jobject javaQtCanvasObject;
static JavaVM *vm = NULL;

CustomCanvas::CustomCanvas (QWidget *parent, const char *name, int w, int h) : QWidget (parent, name) {
	resize(w, h);
	qimage = new QImage (w, h, 32, QImage::BigEndian);
   	setBackgroundMode (NoBackground);
}
 
void CustomCanvas::paintEvent (QPaintEvent *) {
	/* bitBlt (this, 0, 0, qpixmap); // Qt 3.x only */
   	QPainter p(this);
	p.drawImage(0, 0, *qimage, 0, 0, -1, -1, 0);
}

void CustomCanvas::mousePressEvent( QMouseEvent *e ) {
	
	//printf("mousePressEvent/n");			

  	JNIEnv *env;
    jclass clazz;
    jmethodID callback;
 

    if (vm->AttachCurrentThread((void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return;
    }

    clazz = env->GetObjectClass(javaQtCanvasObject);
    callback = env->GetMethodID(clazz, "onMouseButtonEvent", "(III)V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return;
    }
    
    env->CallVoidMethod(javaQtCanvasObject, callback, e->x(), e->y(), 1);
    
     /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
				
}
	
void CustomCanvas::mouseReleaseEvent( QMouseEvent *e ) {
		
	//printf("mouseReleaseEvent/n");
		
	JNIEnv *env;
    jclass clazz;
    jmethodID callback;
    
    /*printf("Button pressed\n");*/

    if (vm->AttachCurrentThread((void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return;
    }

    clazz = env->GetObjectClass(javaQtCanvasObject);
    callback = env->GetMethodID(clazz, "onMouseButtonEvent", "(III)V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return;
    }
    
    env->CallVoidMethod(javaQtCanvasObject, callback, e->x(), e->y(), 0);
    
     /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
}
	
void CustomCanvas::mouseMoveEvent(QMouseEvent *e ) {
		
	//printf("mouseMoveEvent/n");
	
	JNIEnv *env;
    jclass clazz;
    jmethodID callback;
    
    /*printf("Button pressed\n");*/

    if (vm->AttachCurrentThread((void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return;
    }

    clazz = env->GetObjectClass(javaQtCanvasObject);
    callback = env->GetMethodID(clazz, "onMouseMoveEvent", "(II)V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return;
    }
    
    env->CallVoidMethod(javaQtCanvasObject, callback, e->x(), e->y());
    
     /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
		

}

void CustomCanvas::keyPressEvent(QKeyEvent *e) {
	
	//printf("keyPressEvent/n");
	
	JNIEnv *env;
    jclass clazz;
    jmethodID callback;
    
    /*printf("Button pressed\n");*/

    if (vm->AttachCurrentThread((void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return;
    }

    clazz = env->GetObjectClass(javaQtCanvasObject);
    callback = env->GetMethodID(clazz, "onKeyEvent", "(III)V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return;
    }
    
    env->CallVoidMethod(javaQtCanvasObject, callback, 1, e->key(), (int)(e->text().unicode()->unicode()));
    
     /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
}

void CustomCanvas::keyReleaseEvent(QKeyEvent *e) {
	
	//printf("keyReleaseEvent/n");
	
	JNIEnv *env;
    jclass clazz;
    jmethodID callback;
    
    /*printf("Button pressed\n");*/

    if (vm->AttachCurrentThread((void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return;
    }

    clazz = env->GetObjectClass(javaQtCanvasObject);
    callback = env->GetMethodID(clazz, "onKeyEvent", "(III)V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return;
    }
    
    env->CallVoidMethod(javaQtCanvasObject, callback, 0, e->key(), (int)(e->text().unicode()->unicode()));
    
     /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
	
} 

void CustomCanvas::closeEvent(QCloseEvent * e) {
	
	JNIEnv *env;
    jclass clazz;
    jmethodID callback;

    if (vm->AttachCurrentThread((void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return;
    }

    clazz = env->GetObjectClass(javaQtCanvasObject);
    callback = env->GetMethodID(clazz, "onCloseEvent", "()V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return;
    }
    
    env->CallVoidMethod(javaQtCanvasObject, callback);
    
     /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
    
    // Finally, accept to close the widget
    e->accept();
   
}

#ifdef __cplusplus
extern "C" {
#endif

static jint imageWidth = 0;
static jint imageHeight = 0;
static CustomCanvas *nativeQtCanvas;
static QApplication *app;

/*
 * Class:     org_thenesis_midpath_ui_backend_qt_QTCanvas
 * Method:    initialize
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_midpath_ui_backend_qt_QTCanvas_initialize(JNIEnv * env, jobject obj, jint width, jint height) {

  	/* Get VM and current object pointers for use in callbacks */
	if (env->GetJavaVM (&vm) < 0)
		return FALSE;
	javaQtCanvasObject = env->NewGlobalRef(obj);
    if (javaQtCanvasObject == NULL)
    	return FALSE;

	/* Create image buffer */
	imageWidth = width;
	imageHeight = height;

	/* Initialize QT */
	int argc = 1;
    char **argv = (char**) malloc((argc + 1) * sizeof(char *));
    if (argv == NULL) env->ThrowNew(env->FindClass("java/lang/OutOfMemoryError"), "malloc");
    argv[0] = "java";
    argv[argc] = NULL;
  	app = new QApplication(argc, argv);
  	
  	nativeQtCanvas = new CustomCanvas (NULL, "Canvas", imageWidth, imageHeight);
  	nativeQtCanvas->setMouseTracking(TRUE);
	app->setMainWidget(nativeQtCanvas);
  	nativeQtCanvas->show();
  	
  	free(argv);
  	return TRUE;
  	
}

/*
 * Class:     org_thenesis_midpath_ui_backend_qt_QTCanvas
 * Method:    writeARGB
 * Signature: ([IIIII)V
 */
JNIEXPORT void JNICALL Java_org_thenesis_midpath_ui_backend_qt_QTCanvas_writeARGB(JNIEnv * env, jobject obj, jintArray intBuffer, jint x_src, jint y_src, jint width, jint height) {
	
	QRgb *srcBuffer;
  	jint *jarr = env->GetIntArrayElements(intBuffer, 0);
  	srcBuffer = (QRgb*)jarr;
 	
 	int src_offset = y_src * imageWidth + x_src;
 	int src_pos = 0;

	int x, y;
	for (y = 0; y < height; y++) {
		src_pos = src_offset + y * imageWidth;
		QRgb *destBuffer = (QRgb*)nativeQtCanvas->qimage->scanLine(y_src + y) + x_src;
      	for (x = 0; x < width; x++) {
      		//printf("%x\n", *srcBuffer); 
      		destBuffer[x] = srcBuffer[src_pos + x];
		}
  	}
  
  	// Release the array and clean context
  	env->ReleaseIntArrayElements(intBuffer, jarr, 0);
	
	// Request an update of the canvas
  	nativeQtCanvas->update();

}

/*
 * Class:     org_thenesis_midpath_ui_backend_qt_QTCanvas
 * Method:    startMainLoop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_midpath_ui_backend_qt_QTCanvas_startMainLoop(JNIEnv * env, jobject obj) {
	 /* enter the QT main loop */
  	return app->exec();
}


/*
 * Class:     org_thenesis_midpath_ui_backend_qt_QTCanvas
 * Method:    destroy
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_org_thenesis_midpath_ui_backend_qt_QTCanvas_quit(JNIEnv * env, jobject obj) {
    app->quit();
}

#ifdef __cplusplus
}
#endif




