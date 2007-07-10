/* src/native/vm/cldc1.1/com_sun_cldchi_jvm_FileDescriptor.c

   Copyright (C) 2006, 2007 R. Grafl, A. Krall, C. Kruegel, C. Oates,
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

#include "config.h"

#include <stdio.h>

#include "vm/types.h"

#include "native/jni.h"
#include "native/native.h"

#include "native/include/com_sun_cldchi_jvm_FileDescriptor.h"

/* native methods implemented by this file ************************************/
 
static JNINativeMethod methods[] = {
	{ "finalize", "()V", (void *) (ptrint) &Java_com_sun_cldchi_jvm_FileDescriptor_finalize },
};
 
/* _Jv_com_sun_cldchi_jvm_FileDescriptor_init ******************************
 
   Register native functions.
 
*******************************************************************************/
 
void _Jv_com_sun_cldchi_jvm_FileDescriptor_init(void)
{
	utf *u;
 
	u = utf_new_char("com/sun/cldchi/jvm/FileDescriptor");
 
	native_method_register(u, methods, NATIVE_METHODS_COUNT);
}

/*
 * Class:     com/sun/cldchi/jvm/FileDescriptor
 * Method:    finalize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_sun_cldchi_jvm_FileDescriptor_finalize(JNIEnv *env, struct com_sun_cldchi_jvm_FileDescriptor* this) {
	/* printf("close\n"); */
	fclose((FILE *)this->handle);
	
}



