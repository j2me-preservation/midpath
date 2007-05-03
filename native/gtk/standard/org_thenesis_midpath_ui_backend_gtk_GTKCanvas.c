#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <gtk/gtk.h>

GtkWidget *window, *darea;

/* jint isMainLoopStarted = FALSE; */
jint imageWidth = 0;
jint imageHeight = 0;
char *rgbBuffer; 

jobject gtkCanvasObject;
JavaVM *vm = NULL;

static gboolean on_darea_expose (GtkWidget *widget, GdkEventExpose *event, gpointer user_data);
static gboolean button_event(GtkWidget *widget, GdkEventButton *event);
static gboolean motion_notify_event(GtkWidget *widget, GdkEventMotion *event);
static gboolean key_event(GtkWidget *widget, GdkEventKey *event);
static void main_loop_started();


/* References: 
 * - http://developer.gnome.org/doc/API/gdk/gdk-gdkrgb.html 
 * - http://www.linux-france.org/article/devl/gtk/gtk_tut-2.html
 * - http://www.gtk-server.org/GTK-server_Tutorial.html
 * - http://developer.gnome.org/doc/API/
 * - http://www.gtkforums.com/about182.html
 * - http://www.gtk-fr.org/wakka.php?wiki=LeTutorial
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
  
  	window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
  	/*gtk_window_set_title(GTK_WINDOW(pWindow), "Java");*/
  	darea = gtk_drawing_area_new();
  	gtk_drawing_area_size(GTK_DRAWING_AREA(darea), imageWidth, imageHeight);
  	gtk_container_add(GTK_CONTAINER(window), darea);
  	gtk_widget_set_events (darea, GDK_EXPOSURE_MASK | GDK_POINTER_MOTION_MASK | GDK_POINTER_MOTION_HINT_MASK | 
  		GDK_BUTTON_MOTION_MASK | GDK_BUTTON_PRESS_MASK | GDK_BUTTON_RELEASE_MASK | GDK_KEY_PRESS_MASK | GDK_KEY_RELEASE_MASK);
  	gtk_signal_connect(GTK_OBJECT (darea), "expose-event", GTK_SIGNAL_FUNC(on_darea_expose), NULL);
  	gtk_signal_connect (GTK_OBJECT (darea), "motion_notify_event", GTK_SIGNAL_FUNC(motion_notify_event), NULL);
  	gtk_signal_connect (GTK_OBJECT (darea), "button_press_event", GTK_SIGNAL_FUNC(button_event), NULL);
  	gtk_signal_connect (GTK_OBJECT (darea), "button_release_event", GTK_SIGNAL_FUNC(button_event), NULL);
  	gtk_signal_connect (GTK_OBJECT (window), "key_press_event", GTK_SIGNAL_FUNC(key_event), NULL);
  	gtk_signal_connect (GTK_OBJECT (window), "key_release_event", GTK_SIGNAL_FUNC(key_event), NULL);
  	
  	gtk_widget_show_all(window);
  	
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
	gtk_main_quit();
	//free(rgbBuffer);
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
  	jsize arraySize = (*env)->GetArrayLength(env, intBuffer);
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


static gboolean on_darea_expose (GtkWidget *widget, GdkEventExpose *event, gpointer user_data) {
  gdk_draw_rgb_image (widget->window, widget->style->fg_gc[GTK_STATE_NORMAL],
		      0, 0, imageWidth, imageHeight,
		      GDK_RGB_DITHER_MAX, rgbBuffer, imageWidth * 3);
}

static gboolean key_event(GtkWidget *widget, GdkEventKey *event) {
	
	JNIEnv *env = NULL;
    jclass class;
    jmethodID callback;
    guint32 unicode;
    
    unicode = gdk_keyval_to_unicode(event->keyval);
    
    /*printf("Key pressed\n"); */

    if ((*vm)->AttachCurrentThread(vm, (void **)&env, NULL) < 0) {
    	fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n", __FILE__, __LINE__);
        return FALSE;
    }

    class = (*env)->GetObjectClass (env, gtkCanvasObject);
    callback = (*env)->GetMethodID (env, class, "onKeyEvent", "(III)V");
    if (callback == NULL) {
    	 fprintf (stderr, "%s[%d]: GetMethodID ()\n", __FILE__, __LINE__);
      	return FALSE;
    }
    
   	(*env)->CallVoidMethod(env, gtkCanvasObject, callback, event->type, event->keyval, unicode);

    
    /*if ((*vm)->DetachCurrentThread(vm) < 0) {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n", __FILE__, __LINE__);
    }*/
    
  	return TRUE;
}


static gboolean button_event(GtkWidget *widget, GdkEventButton *event) {
  /*if (event->button == 1 && pixmap != NULL)
      draw_brush (widget, event->x, event->y);*/
      
    JNIEnv *env = NULL;
    jclass class;
    jmethodID callback;
    gdouble x, y;
  	GdkModifierType state;
    
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


static gboolean motion_notify_event(GtkWidget *widget, GdkEventMotion *event ) {
	
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

