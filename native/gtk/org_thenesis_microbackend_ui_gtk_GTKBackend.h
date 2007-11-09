#include <jni.h>
/* Header for class org_thenesis_microbackend_ui_gtk_GTKBackend */

#ifndef _org_thenesis_microbackend_ui_gtk_GTKBackend
#define _org_thenesis_microbackend_ui_gtk_GTKBackend

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     org_thenesis_microbackend_ui_gtk_GTKBackend
 * Method:    initialize
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_microbackend_ui_gtk_GTKBackend_initialize(JNIEnv * env, jobject obj, jint width, jint height);

/*
 * Class:     org_thenesis_microbackend_ui_gtk_GTKBackend
 * Method:    gtkMain
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_microbackend_ui_gtk_GTKBackend_gtkMainIterationDo(JNIEnv * env, jobject obj);

/*
 * Class:     org_thenesis_microbackend_ui_gtk_GTKBackend
 * Method:    destroy
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_org_thenesis_microbackend_ui_gtk_GTKBackend_destroy(JNIEnv * env, jobject obj);

/*
 * Class:     org_thenesis_microbackend_ui_gtk_GTKBackend
 * Method:    writeARGB
 * Signature: ([IIIII)V
 */
JNIEXPORT void JNICALL Java_org_thenesis_microbackend_ui_gtk_GTKBackend_writeARGB(JNIEnv * env, jobject obj, jintArray intBuffer, jint x_src, jint y_src, jint width, jint height);

#ifdef __cplusplus
}
#endif
#endif
