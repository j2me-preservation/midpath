#include <jni.h>
/* Header for class org_thenesis_microbackend_ui_qt_QTBackend */

#ifndef _org_thenesis_microbackend_ui_qt_QTBackend
#define _org_thenesis_microbackend_ui_qt_QTBackend
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     org_thenesis_microbackend_ui_qt_QTBackend
 * Method:    initialize
 * Signature: (II)I
 */
JNIEXPORT jboolean JNICALL Java_org_thenesis_microbackend_ui_qt_QTBackend_initialize(JNIEnv * env, jobject obj, jint width, jint height);

/*
 * Class:     org_thenesis_microbackend_ui_qt_QTBackend
 * Method:    writeARGB
 * Signature: ([IIIII)V
 */
JNIEXPORT void JNICALL Java_org_thenesis_microbackend_ui_qt_QTBackend_writeARGB(JNIEnv * env, jobject obj, jintArray intBuffer, jint x_src, jint y_src, jint width, jint height);
/*
 * Class:     org_thenesis_microbackend_ui_qt_QTBackend
 * Method:    startMainLoop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_microbackend_ui_qt_QTBackend_startMainLoop(JNIEnv * env, jobject obj);

/*
 * Class:     org_thenesis_microbackend_ui_qt_QTBackend
 * Method:    destroy
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_org_thenesis_microbackend_ui_qt_QTBackend_quit(JNIEnv * env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif
