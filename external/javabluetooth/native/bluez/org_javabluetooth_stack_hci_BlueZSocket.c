#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <netdb.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <sys/types.h>
#include <sys/socket.h>

#ifndef _Included_org_javabluetooth_stack_hci_BlueZSocket
#define _Included_org_javabluetooth_stack_hci_BlueZSocket
#ifdef __cplusplus
extern "C" {
#endif       

/*
 * Class:     org_javabluetooth_stack_hci_BlueZSocket
 * Method:    open0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_javabluetooth_stack_hci_BlueZSocket_open0(JNIEnv * env, jobject obj, jint deviceNumber) {

	int sock, retval;
	int i;
	unsigned char buf[HCI_MAX_FRAME_SIZE];
	struct sockaddr_hci addr;
	struct hci_filter filter;
	
	sock = socket(AF_BLUETOOTH, SOCK_RAW, BTPROTO_HCI);
	if (sock < 0) 
		(*env)->ThrowNew(env,(*env)->FindClass(env,"java/io/IOException"), strerror(errno));

	hci_filter_clear(&filter);
	hci_filter_all_ptypes(&filter);
	hci_filter_all_events(&filter);

	retval = setsockopt(sock, SOL_HCI, HCI_FILTER, &filter, sizeof(filter)); 
	if (retval < 0) 
		(*env)->ThrowNew(env,(*env)->FindClass(env,"java/io/IOException"), strerror(errno));

	addr.hci_family = AF_BLUETOOTH;
	addr.hci_dev = deviceNumber; /* default: 0 */
	retval = bind(sock, (struct sockaddr *)&addr, sizeof(addr));
	if (retval < 0)
		(*env)->ThrowNew(env,(*env)->FindClass(env,"java/io/IOException"), strerror(errno));;
	
	return sock;

}

/*
 * Class:     org_javabluetooth_stack_hci_BlueZSocket
 * Method:    available
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_javabluetooth_stack_hci_BlueZSocket_available0(JNIEnv * env, jobject obj, jint handle) {
	
	int arg = 0;
    int result;
    
    result = ioctl(handle, FIONREAD, &arg);

    if (result < 0) {
    	(*env)->ThrowNew(env,(*env)->FindClass(env,"java/io/IOException"), strerror(errno));
    }
    
    return arg;  
}

/*
 * Class:     org_javabluetooth_stack_hci_BlueZSocket
 * Method:    read0
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_org_javabluetooth_stack_hci_BlueZSocket_read0(JNIEnv * env, jobject obj, jint handle, jbyteArray byteBuffer, jint offset, jint length) {
	
  	jsize arraySize = (*env)->GetArrayLength(env, byteBuffer);
  	jbyte *jarr = (*env)->GetByteArrayElements(env, byteBuffer, 0);
  	
  	/* get pointer to the buffer */
  	char *destBuffer = (char*)(jarr + offset);
	ssize_t result;
	
	/* receive from the socket */
	result = recv(handle, destBuffer, length, 0);

  	/* Release the array and clean context */
  	(*env)->ReleaseByteArrayElements(env, byteBuffer, jarr, 0);
  	
  	if (result == 0) {
		/* the peer has performed an orderly shutdown */
		return -1;
	} else if (result < 0)
		(*env)->ThrowNew(env,(*env)->FindClass(env,"java/io/IOException"), strerror(errno));
  
 	return result;

}

/*
 * Class:     org_javabluetooth_stack_hci_BlueZSocket
 * Method:    write0
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_org_javabluetooth_stack_hci_BlueZSocket_write0(JNIEnv * env, jobject obj, jint handle, jbyteArray byteBuffer, jint offset, jint length) {
	
  	jsize arraySize = (*env)->GetArrayLength(env, byteBuffer);
  	jbyte *jarr = (*env)->GetByteArrayElements(env, byteBuffer, 0);
  	
  	/* get pointer to the buffer */
  	char *srcBuffer = (char*)(jarr + offset);
	ssize_t result;

	/* send the given byte to the socket */
	result = send(handle, srcBuffer, length, 0);
  	
  	/* Release the array and clean context */
  	(*env)->ReleaseByteArrayElements(env, byteBuffer, jarr, 0);
  	
  	if (result < 0)
		(*env)->ThrowNew(env,(*env)->FindClass(env,"java/io/IOException"), strerror(errno));
  
 	return result;

}

/*
 * Class:     org_javabluetooth_stack_hci_BlueZSocket
 * Method:    close0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_javabluetooth_stack_hci_BlueZSocket_close0(JNIEnv * env, jobject obj, jint handle) {
	ssize_t result;
	
	result = close(handle);
	
	if (result < 0) { 
		(*env)->ThrowNew(env,(*env)->FindClass(env,"java/io/IOException"), strerror(errno));
	}

}

#ifdef __cplusplus
}
#endif
#endif


