#include <jni.h>
/* Header for class org_thenesis_midpath_ui_backend_qt_QTCanvas */

#ifndef _org_thenesis_midpath_ui_backend_qt_QTCanvas
#define _org_thenesis_midpath_ui_backend_qt_QTCanvas
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     org_thenesis_midpath_ui_backend_qt_QTCanvas
 * Method:    initialize
 * Signature: (II)I
 */
JNIEXPORT jboolean JNICALL Java_org_thenesis_midpath_ui_backend_qt_QTCanvas_initialize(JNIEnv * env, jobject obj, jint width, jint height);

/*
 * Class:     org_thenesis_midpath_ui_backend_qt_QTCanvas
 * Method:    writeARGB
 * Signature: ([IIIII)V
 */
JNIEXPORT void JNICALL Java_org_thenesis_midpath_ui_backend_qt_QTCanvas_writeARGB(JNIEnv * env, jobject obj, jintArray intBuffer, jint x_src, jint y_src, jint width, jint height);
/*
 * Class:     org_thenesis_midpath_ui_backend_qt_QTCanvas
 * Method:    startMainLoop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_midpath_ui_backend_qt_QTCanvas_startMainLoop(JNIEnv * env, jobject obj);

/*
 * Class:     org_thenesis_midpath_ui_backend_qt_QTCanvas
 * Method:    destroy
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_org_thenesis_midpath_ui_backend_qt_QTCanvas_quit(JNIEnv * env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif
