/* src/native/vm/cldc1.1/com_sun_cldc_io_ResourceInputStream.c

   Copyright (C) 2007 R. Grafl, A. Krall, C. Kruegel, C. Oates,
   R. Obermaisser, M. Platter, M. Probst, S. Ring, E. Steiner,
   C. Thalinger, D. Thuernbeck, P. Tomsich, C. Ullrich, J. Wenninger,
   Institut f. Computersprachen - TU Wien

   This file is part of CACAO.

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2, or (at
   your option) any later version.

   This program is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
   02110-1301, USA.

   $Id: java_lang_VMRuntime.c 5900 2006-11-04 17:30:44Z michi $

*/

#include <sys/stat.h>
#include <stdlib.h>

#include "config.h"

#include "arch.h"
#include "mm/memory.h"

#include "native/jni.h"
#include "native/native.h"

#include "native/include/java_lang_Object.h"
#include "native/include/java_lang_String.h"
#include "native/include/java_lang_Integer.h"
#include "native/include/com_sun_cldc_io_ResourceInputStream.h"
#include "native/include/com_sun_cldchi_jvm_FileDescriptor.h"

#include "vm/types.h"
#include "vm/builtin.h"
#include "vm/vm.h" /* REMOVE ME: temporarily */
#include "vm/exceptions.h"
#include "vm/initialize.h"
#include "vm/stringlocal.h"
#include "vm/properties.h"

#include "vmcore/class.h"
#include "vmcore/classcache.h"
#include "vmcore/linker.h"
#include "vmcore/loader.h"
#include "vmcore/options.h"
#include "vmcore/statistics.h"
#include "vmcore/suck.h"
#include "vmcore/zip.h"

#include "toolbox/list.h"
#include "toolbox/logging.h"
#include "toolbox/util.h"


/* native methods implemented by this file ************************************/
 
static JNINativeMethod methods[] = {
	{ "open", "(Ljava/lang/String;)Ljava/lang/Object;", (void *) (ptrint) &Java_com_sun_cldc_io_ResourceInputStream_open },
	{ "bytesRemain", "(Ljava/lang/Object;)I", (void *) (ptrint) &Java_com_sun_cldc_io_ResourceInputStream_bytesRemain },
	{ "readByte", "(Ljava/lang/Object;)I", (void *) (ptrint) &Java_com_sun_cldc_io_ResourceInputStream_readByte },
	{ "readBytes", "(Ljava/lang/Object;[BII)I", (void *) (ptrint) &Java_com_sun_cldc_io_ResourceInputStream_readBytes },
	{ "clone", "(Ljava/lang/Object;)Ljava/lang/Object;", (void *) (ptrint) &Java_com_sun_cldc_io_ResourceInputStream_clone },
};
 
/* _Jv_com_sun_cldc_io_ResourceInputStream_init ********************************
 
   Register native functions.
 
*******************************************************************************/
 
void _Jv_com_sun_cldc_io_ResourceInputStream_init(void)
{
	utf *u;
 
	u = utf_new_char("com/sun/cldc/io/ResourceInputStream");
 
	native_method_register(u, methods, NATIVE_METHODS_COUNT);
}


/*
 * Class:     com/sun/cldc/io/ResourceInputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT struct java_lang_Object* JNICALL Java_com_sun_cldc_io_ResourceInputStream_open(JNIEnv *env, jclass clazz, java_lang_String *name)
{
	
	list_classpath_entry *lce;
	char                 *filename;
	s4                    filenamelen;
	char                 *path;
	FILE                 *classfile;
	/*struct stat           statBuffer;
	int bufferSize = -1;*/
	utf *uname;
	/*java_lang_Integer *fhandler;*/
	com_sun_cldchi_jvm_FileDescriptor *fileDescriptor; 
	classinfo *ci;
	
	
	/* get the classname as char string (do it here for the warning at
       the end of the function) */

	uname = javastring_toutf((java_objectheader *)name, false);
	filenamelen = utf_bytes(uname) + strlen("0");
	filename = MNEW(char, filenamelen);
	utf_copy(filename, uname);
	classfile = NULL;
	

	/* walk through all classpath entries */

	for (lce = list_first(list_classpath_entries); lce != NULL;
		 lce = list_next(list_classpath_entries, lce)) {

			path = MNEW(char, lce->pathlen + filenamelen);
			strcpy(path, lce->path);
			strcat(path, filename);  

			classfile = fopen(path, "r");
			
			MFREE(path, char, lce->pathlen + filenamelen);

			if (classfile) { /* file exists */
				break;
			}
	}

	MFREE(filename, char, filenamelen);

	if (classfile) {
		ci = load_class_bootstrap(utf_new_char("com/sun/cldchi/jvm/FileDescriptor"));
		fileDescriptor = (com_sun_cldchi_jvm_FileDescriptor *) native_new_and_init(ci);
		fileDescriptor->handle = (int) classfile;
		fileDescriptor->valid = (int) 0;
		return (java_lang_Object*) fileDescriptor;
	} else {
		return NULL;
	}
	
}


/*
 * Class:     com_sun_cldc_io_ResourceInputStream
 * Method:    bytesRemain
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT s4 JNICALL Java_com_sun_cldc_io_ResourceInputStream_bytesRemain(JNIEnv *env, jclass clazz, struct java_lang_Object* jobj) {
	
	com_sun_cldchi_jvm_FileDescriptor *fileDescriptor;
	struct stat statBuffer;
	FILE *file;
	int fd;
	int position;
	int hposition;
	
	fileDescriptor = (com_sun_cldchi_jvm_FileDescriptor *) jobj;
	file = (FILE *)fileDescriptor->handle;
	
	/* Change access position if needed */
	hposition = fileDescriptor->valid;
	position = ftell(file);
	if (position != hposition) {
		fseek(file, hposition, SEEK_SET);
	}
	
	fd = fileno(file);
	if (fstat(fd, &statBuffer) != -1) {
		return (statBuffer.st_size - hposition);
	} else {
		/* TODO Throw an IOException */
		return 0; 
	}

}

/*
 * Class:     com_sun_cldc_io_ResourceInputStream
 * Method:    readByte
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT s4 JNICALL Java_com_sun_cldc_io_ResourceInputStream_readByte(JNIEnv *env, jclass clazz, struct java_lang_Object* jobj) {
	
	com_sun_cldchi_jvm_FileDescriptor *fileDescriptor;
	int readBytes = -1;
	char byte;
	FILE * file;
	int position;
	int hposition;
	
	fileDescriptor = (com_sun_cldchi_jvm_FileDescriptor *) jobj;
	file = (FILE *)fileDescriptor->handle;
	
	/* Change access position if needed */
	hposition = fileDescriptor->valid;
	position = ftell(file);
	if (position != hposition) {
		fseek(file, hposition, SEEK_SET);
	}
	
	readBytes = fread(&byte, 1, 1, file);
	
	/* Check if EOF or an error occurred */
	if (readBytes != 1) {
		if (feof(file)) {
			return -1;
		} else if (ferror(file)) {
			/* TODO: throw an IOException */
		}
	}
	
	/* Update access position */
	fileDescriptor->valid =  ftell(file); 
	
	return (byte & 0xFF);

}

/*
 * Class:     com_sun_cldc_io_ResourceInputStream
 * Method:    readBytes
 * Signature: (Ljava/lang/Object;[BII)I
 */
JNIEXPORT s4 JNICALL Java_com_sun_cldc_io_ResourceInputStream_readBytes(JNIEnv *env, jclass clazz, struct java_lang_Object* jobj, java_bytearray* byteArray, s4 off, s4 len) {
	
	com_sun_cldchi_jvm_FileDescriptor *fileDescriptor;
	int readBytes = -1;
	FILE * file;
	int position;
	int hposition;
	void  *buf;

	/* get pointer to the buffer */
	buf = &(byteArray->data[off]);
	
	fileDescriptor = (com_sun_cldchi_jvm_FileDescriptor *) jobj;
	file = (FILE *)fileDescriptor->handle;
	
	/* Change access position if needed */
	hposition = fileDescriptor->valid;
	position = ftell(file);
	if (position != hposition) {
		fseek(file, hposition, SEEK_SET);
	}
	
	readBytes = fread(buf, 1, len, file);
	
	/* Check if EOF or an error occurred */
	if (readBytes != len) {
		if ((readBytes == 0) && feof(file)) {
			return -1;
		} else if (ferror(file)) {
			/* TODO: throw an IOException */
		}
	}
	
	/* Update access position */
	fileDescriptor->valid = ftell(file);
	
	return readBytes;
}

/*
 * Class:     com_sun_cldc_io_ResourceInputStream
 * Method:    clone
 * Signature: (Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT struct java_lang_Object* JNICALL Java_com_sun_cldc_io_ResourceInputStream_clone(JNIEnv *env, jclass clazz, struct java_lang_Object* jobj) {
	
	classinfo *ci;
	com_sun_cldchi_jvm_FileDescriptor *srcFileDescriptor;
	com_sun_cldchi_jvm_FileDescriptor *dstFileDescriptor;
	
	srcFileDescriptor = (com_sun_cldchi_jvm_FileDescriptor *) jobj;
	
	ci = load_class_bootstrap(utf_new_char("com/sun/cldchi/jvm/FileDescriptor"));
	dstFileDescriptor = (com_sun_cldchi_jvm_FileDescriptor *) native_new_and_init(ci);
	dstFileDescriptor->handle = srcFileDescriptor->handle;
	dstFileDescriptor->valid = srcFileDescriptor->valid;
	
	return (java_lang_Object*) dstFileDescriptor;

}




/*
 * These are local overrides for various environment variables in Emacs.
 * Please do not remove this and leave it at the end of the file, where
 * Emacs will automagically detect them.
 * ---------------------------------------------------------------------
 * Local variables:
 * mode: c
 * indent-tabs-mode: t
 * c-basic-offset: 4
 * tab-width: 4
 * End:
 * vim:noexpandtab:sw=4:ts=4:
 */
