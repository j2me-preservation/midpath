/*
 * MIDPath - Copyright (C) 2006-2007 Guillaume Legris, Mathieu Legris
 * 
 * Copyright (C) Sebastian Mancke
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
#include <gtk/gtk.h>

#include "midpath_ui_peer.h"
#include "midpath_ui_backend.h"

static GtkWindow *window;
static GtkWidget *darea;

/* jint isMainLoopStarted = FALSE; */
static jint imageWidth = 0;
static jint imageHeight = 0;
static guchar *rgbBuffer; 

jobject gtkCanvasObject;
JavaVM *vm = NULL;

static void main_loop_started();

/* References: 
 * - http://developer.gnome.org/doc/API/gdk/gdk-gdkrgb.html 
 * - http://www.linux-france.org/article/devl/gtk/gtk_tut-2.html
 * - http://developer.gnome.org/doc/API/
 */

/*
 * Class:     org_thenesis_midpath_ui_backend_gtk_GTKDrawingArea
 * Method:    initialize
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_midpath_ui_backend_gtk_GTKCanvas_initialize(JNIEnv * env, jobject obj, jint width, jint height) {

  	/* Get VM and current object pointers for use in callbacks */
	if ((*env)->GetJavaVM (env, &vm) < 0)
		return FALSE;
	gtkCanvasObject = (*env)->NewGlobalRef (env, obj);
    if (obj == NULL)
    	return FALSE;

	/* Create image buffer */
	imageWidth = width;
	imageHeight = height;
	rgbBuffer = malloc(width * height * 3 * sizeof(char));
	if (rgbBuffer == NULL) (*env)->ThrowNew(env,(*env)->FindClass(env, "java/lang/OutOfMemoryError"), "malloc");

	/* Initialize GTK */
	int argc = 1;
    char **argv = malloc((argc + 1) * sizeof(char *));
    if (argv == NULL) (*env)->ThrowNew(env,(*env)->FindClass(env, "java/lang/OutOfMemoryError"), "malloc");
    argv[0] = "java";
    argv[argc] = NULL;
  	gtk_init(&argc, &argv);
  	gtk_init_add((GtkFunction)main_loop_started, NULL);
  	free(argv);
  
  	/* Initialize GdkRGB */
  	gdk_rgb_init();
  	gtk_widget_set_default_colormap(gdk_rgb_get_cmap());
  	gtk_widget_set_default_visual(gdk_rgb_get_visual());
  
  	window = create_window("MIDPath");
  	/*gtk_window_set_title(GTK_WINDOW(pWindow), "Java");*/
  	darea = gtk_drawing_area_new();
  	gtk_drawing_area_size(GTK_DRAWING_AREA(darea), imageWidth, imageHeight);
  	gtk_container_add(GTK_CONTAINER(window), darea);


    /** key signals */
    gtk_signal_connect(GTK_OBJECT (window), "key_press_event", GTK_SIGNAL_FUNC(key_event), NULL);
    gtk_signal_connect(GTK_OBJECT (window), "key_release_event", GTK_SIGNAL_FUNC(key_event), NULL);

    /** draw area signals */
    gtk_widget_set_events (darea, GDK_EXPOSURE_MASK | GDK_POINTER_MOTION_MASK | GDK_POINTER_MOTION_HINT_MASK | 
                           GDK_BUTTON_MOTION_MASK | GDK_BUTTON_PRESS_MASK | GDK_BUTTON_RELEASE_MASK | GDK_KEY_PRESS_MASK | GDK_KEY_RELEASE_MASK);
    gtk_signal_connect(GTK_OBJECT(darea), "expose-event", GTK_SIGNAL_FUNC(on_darea_expose), NULL);
    gtk_signal_connect(GTK_OBJECT(darea), "motion_notify_event", GTK_SIGNAL_FUNC(motion_notify_event), NULL);
    gtk_signal_connect(GTK_OBJECT(darea), "button_press_event", GTK_SIGNAL_FUNC(button_event), NULL);
    gtk_signal_connect(GTK_OBJECT(darea), "button_release_event", GTK_SIGNAL_FUNC(button_event), NULL);  
  
  	 /** window signals */
  	gtk_signal_connect(GTK_OBJECT(window), "delete_event", GTK_SIGNAL_FUNC(delete_event), NULL);  
    
  	gtk_widget_show_all(GTK_WIDGET(window));

    /* it is important to do this after showing the window! */
    initialize_window_events(window);
    initialize_drawing_area_events(darea);
  	
  	return TRUE;
}



/*
 * Class:     org_thenesis_midpath_ui_backend_gtk_GTKCanvas
 * Method:    gtkMain
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_thenesis_midpath_ui_backend_gtk_GTKCanvas_gtkMainIterationDo(JNIEnv * env, jobject obj) {
	 /* enter the GTK main loop */
  	//gtk_main();
  	
  	/* Runs a single iteration of the mainloop and don't block*/
  	return gtk_main_iteration_do(FALSE);

}

/*
 * Class:     org_thenesis_midpath_ui_backend_gtk_GTKCanvas
 * Method:    isMainLoopStarted
 * Signature: ()I
 */
/*JNIEXPORT jint JNICALL Java_org_thenesis_midpath_ui_backend_gtk_GTKCanvas_isMainLoopStarted(JNIEnv * env, jobject obj) {
	return isMainLoopStarted;
}*/

/*
 * Class:     org_thenesis_midpath_ui_backend_gtk_GTKCanvas
 * Method:    destroy
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_org_thenesis_midpath_ui_backend_gtk_GTKCanvas_destroy(JNIEnv * env, jobject obj) {
    /* FIXME: Gtk-CRITICAL when called:
      gtk_main_quit(); */ 
	free(rgbBuffer);
}
	

/*
 * Class:     org_thenesis_midpath_ui_backend_gtk_GTKCanvas
 * Method:    writeARGB
 * Signature: ([IIIII)V
 */
JNIEXPORT void JNICALL Java_org_thenesis_midpath_ui_backend_gtk_GTKCanvas_writeARGB(JNIEnv * env, jobject obj, jintArray intBuffer, jint x_src, jint y_src, jint width, jint height) {
	
	gint x, y;	
	GdkRectangle update_rect;
	guchar *srcBuffer;
	guchar *destBuffer;  
	
	// Lock the array (http://www.iam.ubc.ca/guides/javatut99/native1.1/implementing/array.html)
    // arraySize not used
  	//jsize arraySize = (*env)->GetArrayLength(env, intBuffer);
  	jint *jarr = (*env)->GetIntArrayElements(env, intBuffer, 0);
  	
  	srcBuffer = (guchar*)jarr;
 	destBuffer = rgbBuffer;
 	
 	int src_offset = (y_src * imageWidth + x_src) * 4 ;
 	int dest_offset = (y_src * imageWidth + x_src ) * 3 ;
 	int src_pos = 0;
 	int dest_pos = 0;

#ifndef WORDS_BIGENDIAN
    for (y = 0; y < height; y++) {
    	src_pos = src_offset + y * imageWidth * 4;
		dest_pos = dest_offset + y * imageWidth * 3;
		//printf("B%i\n", y);
      	for (x = 0; x < width; x++) {
	  		destBuffer[dest_pos] = srcBuffer[src_pos + 2];
	  		//printf("%i: %x\n", x, destBuffer[dest_pos]); 
	  		destBuffer[dest_pos + 1] = srcBuffer[src_pos + 1];
	  		destBuffer[dest_pos + 2] = srcBuffer[src_pos];
	  		src_pos += 4;
	  		dest_pos += 3;
		}
  	}
#else
	for (y = 0; y < height; y++) {
		src_pos = src_offset + y * imageWidth * 4;
		dest_pos = dest_offset + y * imageWidth * 3;
      	for (x = 0; x < width; x++) {
      		//printf("%x\n", *srcBuffer); 
      		src_pos++;
      		//printf("%x\n", *srcBuffer); 
	  		destBuffer[dest_pos++] = srcBuffer[src_pos++];
	  		//printf("%x\n", *srcBuffer); 
	  		destBuffer[dest_pos++] = srcBuffer[src_pos++];
	  		//printf("%x\n", *srcBuffer); 
	  		destBuffer[dest_pos++] = srcBuffer[src_pos++];
		}
  	}
#endif
  
  // Release the array and clean context
  (*env)->ReleaseIntArrayElements(env, intBuffer, jarr, 0);
	
  update_rect.x = x_src;
  update_rect.y = y_src;
  update_rect.width = width;
  update_rect.height = height;
  
  //printf("[native] GTKCanvas_writeARGB() : x=%i y=%i w=%i h=%i \n", x_src, y_src, width, height);
  //gtk_widget_queue_draw_area (darea, update_rect.x, update_rect.y, update_rect.width, update_rect.height);
  //gdk_window_invalidate_rect(window, &update_rect, TRUE);
  //printf("redraw area\n");
  gtk_widget_draw (darea, &update_rect);
  
  /* Make sure all X commands are sent to the X server; not strictly
   * necessary here, but always a good idea when you do anything
   * from a thread other than the one where the main loop is running.
   */
   //gdk_flush ();


}

static void main_loop_started() {
	/*isMainLoopStarted = TRUE;
	printf("main_loop_started %i\n", isMainLoopStarted);*/
}


gboolean on_darea_expose (GtkWidget *widget, GdkEventExpose *event, gpointer user_data) {

  int x = event->area.x;
  int y = event->area.y;
  int w = event->area.width;
  int h = event->area.height;
  
  guchar *srcBuffer = &(rgbBuffer[(y * imageWidth + x) * 3]);
  int rowstride = imageWidth * 3;
	
  gdk_draw_rgb_image(widget->window, widget->style->fg_gc[GTK_STATE_NORMAL], x, y, w, h,
		      GDK_RGB_DITHER_MAX, srcBuffer , rowstride);
		 
//printf("[native] on_darea_expose(): x=%i y=%i w=%i h=%i \n", x, y, w, h);		 
//  gdk_draw_rgb_image (widget->window, widget->style->fg_gc[GTK_STATE_NORMAL],
//		      0, 0, imageWidth, imageHeight,
//		      GDK_RGB_DITHER_MAX, rgbBuffer, imageWidth * 3);
		   
  return TRUE;
}


void key_entered(int eventtype, int keycode, int unicode_character) {

	JNIEnv *env = NULL;
    jclass class;
    jmethodID callback;

    if ((*vm)->AttachCurrentThread(vm, (void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return;
    }

    class = (*env)->GetObjectClass (env, gtkCanvasObject);
    callback = (*env)->GetMethodID (env, class, "onKeyEvent", "(III)V");
    if (callback == NULL) {
    	 fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
         return;
    }
    
   	(*env)->CallVoidMethod(env, gtkCanvasObject, callback, eventtype, keycode, unicode_character);

    
    /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
}

gboolean key_event(GtkWidget *widget, GdkEventKey *event) {
	
    guint32 unicode;
    
    unicode = gdk_keyval_to_unicode(event->keyval);
    key_entered(event->type, event->keyval, unicode);
    
    /*printf("Key pressed\n"); */

    
  	return TRUE;
}


gboolean button_event(GtkWidget *widget, GdkEventButton *event) {
  /*if (event->button == 1 && pixmap != NULL)
      draw_brush (widget, event->x, event->y);*/
      
    JNIEnv *env = NULL;
    jclass class;
    jmethodID callback;
    
    /*printf("Button pressed\n");*/

    if ((*vm)->AttachCurrentThread(vm, (void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return FALSE;
    }

    class = (*env)->GetObjectClass (env, gtkCanvasObject);
    callback = (*env)->GetMethodID (env, class, "onButtonEvent", "(IIII)V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return FALSE;
    }
    
    (*env)->CallVoidMethod(env, gtkCanvasObject, callback, (int)event->type, (int)event->x, (int)event->y, (int)event->state);
    
     /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/

  	return TRUE;
}


gboolean motion_notify_event(GtkWidget *widget, GdkEventMotion *event ) {
	
	JNIEnv *env = NULL;
    jclass class;
    jmethodID callback;
    int x, y;
  	GdkModifierType state;
    
  	/*printf("Motion detetected: %i \n", env);*/

    if ((*vm)->AttachCurrentThread(vm, (void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return FALSE;
    }
    
    class = (*env)->GetObjectClass (env, gtkCanvasObject);
    
    callback = (*env)->GetMethodID (env, class, "onMotionEvent", "(III)V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return FALSE;
    }
    
    if (event->is_hint) {
    	gdk_window_get_pointer (event->window, &x, &y, &state);
  	} else {
    	x = event->x;
      	y = event->y;
      	state = event->state;
   	}
    
    (*env)->CallVoidMethod(env, gtkCanvasObject, callback, x, y, (int)state);

    /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
    
  	return TRUE;
  
}

/* GDK_DELETE means that the window manager has asked the application to destroy this window. 
 * If a widget receives the signal corresponding to this event, and the signal emission returns FALSE, 
 * the widget is automatically destroyed by the GTK+ main loop. */
gint delete_event(GtkWidget *widget, GdkEvent *event, gpointer data) {
   
	JNIEnv *env = NULL;
    jclass class;
    jmethodID callback;
    
    /*printf("window delete event\n");*/

    if ((*vm)->AttachCurrentThread(vm, (void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return FALSE;
    }

    class = (*env)->GetObjectClass (env, gtkCanvasObject);
    callback = (*env)->GetMethodID (env, class, "onWindowDeleteEvent", "()V");
    if (callback == NULL) {
   		fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return FALSE;
    }
    
    (*env)->CallVoidMethod(env, gtkCanvasObject, callback);
    
     /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/

 
    return FALSE; 
}

