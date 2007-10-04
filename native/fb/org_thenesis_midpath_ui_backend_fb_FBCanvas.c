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
 
/* References: 
 *  - http://www.linuxjournal.com/article/1080
 *  - http://www.kernel.org/pub/linux/kernel/people/aeb/kbdbook.tmpl
 *  - http://www.w00w00.org/files/articles/conioctls.txt
 *  - http://huru.imukuppi.org/opengl/plg/node191.html
 */
 
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <linux/fb.h>
#include <linux/kd.h>
#include <termios.h>

#ifndef _Included_org_thenesis_midpath_ui_backend_fb_FBCanvas
#define _Included_org_thenesis_midpath_ui_backend_fb_FBCanvas
#ifdef __cplusplus
extern "C" {
#endif

static int keyboard_fd;
static struct termios old_term, new_term;
static int old_mode = -1;

static jint imageWidth = 0;
static jint imageHeight = 0;
static struct fb_fix_screeninfo fb_fix_infos;
static struct fb_var_screeninfo fb_var_infos;
static int fb_fd; 
static char *fb_ptr;

/*
 * Class:     org_thenesis_midpath_ui_backend_fb_FBCanvas
 * Method:    initialize
 * Signature: (II)I
 */
JNIEXPORT jboolean JNICALL Java_org_thenesis_midpath_ui_backend_fb_FBCanvas_initialize(JNIEnv * env, jobject obj, jstring keyboardDeviceName, jstring fbDeviceName, jint width, jint height) {
   	
	char *keyboard_device_name;
	char *utfKeyboardDeviceName;
	char *fb_device_name;
	char *utfFbDeviceName;
	
	/* Initialize the keyboard */
	
	utfKeyboardDeviceName = (char *)(*env)->GetStringUTFChars(env, keyboardDeviceName, 0);
    keyboard_device_name = strdup(utfKeyboardDeviceName);
	(*env)->ReleaseStringUTFChars(env, keyboardDeviceName, utfKeyboardDeviceName);
	
	keyboard_fd = open(keyboard_device_name, O_RDWR | O_NDELAY, 0);
	if (keyboard_fd < 0) {
		return JNI_FALSE;
	}
	
	/* Find the keyboard's mode so we can restore it later. */
    if (ioctl(keyboard_fd, KDGKBMODE, &old_mode) != 0) {
        printf("Unable to query keyboard mode.\n");
        return JNI_FALSE;
    }
    
    /* Adjust the terminal's settings. In particular, disable
       echoing, signal generation, and line buffering. Any of
       these could cause trouble. Save the old settings first. */
    if (tcgetattr(keyboard_fd, &old_term) != 0) {
        printf("Unable to query terminal settings.\n");
        return JNI_FALSE;
    }

    new_term = old_term;
    new_term.c_iflag = 0;
    new_term.c_lflag &= ~(ECHO | ICANON | ISIG);

    /* TCSAFLUSH discards unread input before making the change.
       A good idea. */
    if (tcsetattr(keyboard_fd, TCSAFLUSH, &new_term) != 0) {
        printf("Unable to change terminal settings.\n");
    }

    /* Put the keyboard in mediumraw mode. */
    if (ioctl(keyboard_fd, KDSKBMODE, K_MEDIUMRAW) != 0) {
        printf("Unable to set mediumraw mode.\n");
        return JNI_FALSE;
    }
    
	/*// Set console in graphcis mode
	ioctl(keyboard_fd, KDSETMODE, KD_TEXT);
	// Request to get keycodes
	ioctl(keyboard_fd, KDSKBMODE, K_UNICODE);*/
	
	/* Initialize the Linux Framebuffer */
	
	utfFbDeviceName = (char *)(*env)->GetStringUTFChars(env, fbDeviceName, 0);
    fb_device_name = strdup(utfFbDeviceName);
	(*env)->ReleaseStringUTFChars(env, fbDeviceName, utfFbDeviceName);
  
   	imageWidth = width;
   	imageHeight = height;

  	fb_fd = open(fb_device_name, O_RDWR) ;
  	if (fb_fd < 0) {
     	return JNI_FALSE;
    }
  
  	ioctl(fb_fd, FBIOGET_FSCREENINFO, &fb_fix_infos);
   	ioctl(fb_fd, FBIOGET_VSCREENINFO, &fb_var_infos);
   	
   	/* printf("[DEBUG](native) Framebuffer: smem_len=%i type=%i visual=%i line_length=%i\n", fb_fix_infos.smem_len, fb_fix_infos.type, fb_fix_infos.visual, fb_fix_infos.line_length);
   	printf("[DEBUG](native) Framebuffer: xres=%i yres=%i bits_per_pixel=%i\n", fb_var_infos.xres, fb_var_infos.yres, fb_var_infos.bits_per_pixel); */
   	
   	fb_ptr = (char*)mmap(NULL, fb_fix_infos.smem_len,
                       PROT_READ | PROT_WRITE, MAP_SHARED,
                       fb_fd, 0);  

  	return JNI_TRUE;
  	
}

/*
 * Class:     org_thenesis_midpath_ui_backend_fb_FBCanvas
 * Method:    writeARGB
 * Signature: ([IIIII)V
 */
JNIEXPORT void JNICALL Java_org_thenesis_midpath_ui_backend_fb_FBCanvas_writeARGB(JNIEnv * env, jobject obj, jintArray intBuffer, jint x_src, jint y_src, jint width, jint height) {
	
	char *srcBuffer;
	
  	jint *jarr = (*env)->GetIntArrayElements(env, intBuffer, 0);
  	srcBuffer = (char*)jarr;
 	
 	int src_bytes_per_pixel = 4; 
 	int dest_bytes_per_pixel = fb_var_infos.bits_per_pixel / 8;
 	
 	int src_offset = (y_src * imageWidth + x_src) * src_bytes_per_pixel;
 	int src_pos = 0;

	int x, y;
	__u32 argb, a, r, g, b, c, color;
	for (y = 0; y < height; y++) {
		
		src_pos = src_offset + y * imageWidth;
		void *destBuffer = (void*) (fb_ptr + (y_src + y) * fb_fix_infos.line_length + x_src * dest_bytes_per_pixel);
      	
      	for (x = 0; x < width; x++) {
      		/* printf("%x\n", *srcBuffer); */

			/* Get ARGB components */
			argb = ((__u32*)srcBuffer)[src_pos + x];
			a = (argb >> 24) & 0xFF;
			r = (argb >> 16) & 0xFF;
			g = (argb >> 8) & 0xFF;
			b = argb & 0xFF;    
			/* printf("argb=%x a=%x r=%x g=%x b=%x\n", argb, a, r, g, b); */

			/* Convert ARGB components to the target color */
			c = r >> (8 - fb_var_infos.red.length) ;
			c <<= fb_var_infos.red.offset ;
			color = c ;
			c = g >> (8 - fb_var_infos.green.length) ;
       		c <<= fb_var_infos.green.offset ;
       		color |= c ;
       		c = b >> (8 - fb_var_infos.blue.length) ;
       		c <<= fb_var_infos.blue.offset ;
       		color |= c ;
       		c = a >> (8 - fb_var_infos.transp.length) ;
       		c <<= fb_var_infos.transp.offset ;
       		color |= c ;
       		/* printf("color=%x\n", color); */
       	
       		/* Set pixel in the framebuffer */
       		switch (fb_var_infos.bits_per_pixel) {
         	case 16 :
         		((__u16*)destBuffer)[x] = (__u16)color;
           		break ;
         	case 32 :
           		((__u32*)destBuffer)[x] = color;
           		break ;
       		}
      		
		}
  	}
  
  	/* Release the array and clean context */
  	(*env)->ReleaseIntArrayElements(env, intBuffer, jarr, 0);
	
	/* Sync memory and framebuffer */
  	msync(fb_ptr, fb_fix_infos.smem_len, MS_SYNC | MS_INVALIDATE); 

}




/*
 * Class:     org_thenesis_midpath_ui_backend_fb_FBCanvas
 * Method:    getKeyCode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_midpath_ui_backend_fb_FBCanvas_getKeyCode(JNIEnv * env, jobject obj) {
	unsigned char c;
   
    if (read(keyboard_fd, &c, 1) < 1) {
    	//printf("Unable to read data..\n");
    	return -1; // Error
    }
    
    return (jint)c;
    
    /* Print the keycode. The top bit is the pressed/released
           flag, and the lower seven are the keycode. */
       /* printf("%s: %2Xh (%i)\n",
               (data & 0x80) ? "Released" : " Pressed",
               (unsigned int)data & 0x7F,
               (unsigned int)data & 0x7F);*/
    
}


/*
 * Class:     org_thenesis_midpath_ui_backend_fb_FBCanvas
 * Method:    quit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_thenesis_midpath_ui_backend_fb_FBCanvas_quit(JNIEnv * env, jobject obj) {
    
    /* Clean the framebuffer context */
    munmap(fb_ptr, fb_fix_infos.smem_len);
	close(fb_fd);
	
	/* Restore the previous keyboard mode. */
    if (old_mode != -1) {
        ioctl(keyboard_fd, KDSKBMODE, old_mode);
        tcsetattr(keyboard_fd, 0, &old_term);
    }
    /* Only bother closing the keyboard fd if it's not stdin, stdout, or stderr. */
    if (keyboard_fd > 3)
        close(keyboard_fd);
}

#ifdef __cplusplus
}
#endif
#endif




