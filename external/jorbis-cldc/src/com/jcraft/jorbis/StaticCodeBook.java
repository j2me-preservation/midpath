/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *  
 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *   
 * Many thanks to 
 *   Monty <monty@xiph.org> and 
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jorbis;

import com.jcraft.jogg.*;

class StaticCodeBook{
  int   dim;            // codebook dimensions (elements per vector)
  int   entries;        // codebook entries
  int[] lengthlist;     // codeword lengths in bits

  // mapping
  int   maptype;        // 0=none
			// 1=implicitly populated values from map column 
			// 2=listed arbitrary values

  // The below does a linear, single monotonic sequence mapping.
  int   q_min;       // packed 32 bit float; quant value 0 maps to minval
  int   q_delta;     // packed 32 bit float; val 1 - val 0 == delta
  int   q_quant;     // bits: 0 < quant <= 16
  int   q_sequencep; // bitflag

  // additional information for log (dB) mapping; the linear mapping
  // is assumed to actually be values in dB.  encodebias is used to
  // assign an error weight to 0 dB. We have two additional flags:
  // zeroflag indicates if entry zero is to represent -Inf dB; negflag
  // indicates if we're to represent negative linear values in a
  // mirror of the positive mapping.

  int[] quantlist;  // map == 1: (int)(entries/dim) element column map
                    // map == 2: list of dim*entries quantized entry vals

  // encode helpers
  EncodeAuxNearestMatch nearest_tree;
  EncodeAuxThreshMatch  thresh_tree;

  StaticCodeBook(){}
  StaticCodeBook(int dim, int entries, int[] lengthlist,
		 int maptype, int q_min, int q_delta, 
		 int q_quant, int q_sequencep, int[] quantlist,
		 //EncodeAuxNearestmatch nearest_tree,
		 Object nearest_tree,
		 // EncodeAuxThreshmatch thresh_tree,
		 Object thresh_tree
		 ){
    this();
    this.dim=dim; this.entries=entries; this.lengthlist=lengthlist;
    this.maptype=maptype; this.q_min=q_min; this.q_delta=q_delta;
    this.q_quant=q_quant; this.q_sequencep=q_sequencep; 
    this.quantlist=quantlist;
  }

  int pack(Buffer opb){
    int i;
    boolean ordered=false;

    opb.write(0x564342,24);
    opb.write(dim, 16);
    opb.write(entries, 24);

    // pack the codewords.  There are two packings; length ordered and
    // length random.  Decide between the two now.
  
    for(i=1;i<entries;i++){
      if(lengthlist[i]<lengthlist[i-1])break;
    }
    if(i==entries)ordered=true;
  
    if(ordered){
      // length ordered.  We only need to say how many codewords of
      // each length.  The actual codewords are generated
      // deterministically

      int count=0;
      opb.write(1,1);               // ordered
      opb.write(lengthlist[0]-1,5); // 1 to 32

      for(i=1;i<entries;i++){
	int _this=lengthlist[i];
	int _last=lengthlist[i-1];
	if(_this>_last){
	  for(int j=_last;j<_this;j++){
	    opb.write(i-count,ilog(entries-count));
	    count=i;
	  }
	}
      }
      opb.write(i-count,ilog(entries-count));
    }
    else{
      // length random.  Again, we don't code the codeword itself, just
      // the length.  This time, though, we have to encode each length
      opb.write(0,1);   // unordered
    
      // algortihmic mapping has use for 'unused entries', which we tag
      // here.  The algorithmic mapping happens as usual, but the unused
      // entry has no codeword.
      for(i=0;i<entries;i++){
	if(lengthlist[i]==0)break;
      }

      if(i==entries){
	opb.write(0,1); // no unused entries
	for(i=0;i<entries;i++){
	  opb.write(lengthlist[i]-1,5);
	}
      }
      else{
	opb.write(1,1); // we have unused entries; thus we tag
	for(i=0;i<entries;i++){
	  if(lengthlist[i]==0){
	    opb.write(0,1);
	  }
	  else{
	    opb.write(1,1);
	    opb.write(lengthlist[i]-1,5);
	  }
	}
      }
    }

    // is the entry number the desired return value, or do we have a
    // mapping? If we have a mapping, what type?
    opb.write(maptype,4);
    switch(maptype){
    case 0:
      // no mapping
      break;
    case 1:
    case 2:
      // implicitly populated value mapping
      // explicitly populated value mapping
      if(quantlist==null){
	// no quantlist?  error
	return(-1);
      }
    
      // values that define the dequantization
      opb.write(q_min,32);
      opb.write(q_delta,32);
      opb.write(q_quant-1,4);
      opb.write(q_sequencep,1);
    
      {
	int quantvals=0;
	switch(maptype){
	case 1:
	  // a single column of (c->entries/c->dim) quantized values for
	  // building a full value list algorithmically (square lattice)
	  quantvals=maptype1_quantvals();
	  break;
	case 2:
	  // every value (c->entries*c->dim total) specified explicitly
	  quantvals=entries*dim;
	  break;
	}

	// quantized values
	for(i=0;i<quantvals;i++){
	  opb.write(Math.abs(quantlist[i]),q_quant);
	}
      }
      break;
    default:
      // error case; we don't have any other map types now
      return(-1);
    }
    return(0);
  }
/*
*/

  // unpacks a codebook from the packet buffer into the codebook struct,
  // readies the codebook auxiliary structures for decode
  int unpack(Buffer opb){
    int i;
    //memset(s,0,sizeof(static_codebook));

    // make sure alignment is correct
    if(opb.read(24)!=0x564342){
//    goto _eofout;
      clear();
      return(-1); 
    }

    // first the basic parameters
    dim=opb.read(16);
    entries=opb.read(24);
    if(entries==-1){
//    goto _eofout;
      clear();
      return(-1); 
    }

    // codeword ordering.... length ordered or unordered?
    switch(opb.read(1)){
    case 0:
      // unordered
      lengthlist=new int[entries];

      // allocated but unused entries?
      if(opb.read(1)!=0){
	// yes, unused entries

	for(i=0;i<entries;i++){
	  if(opb.read(1)!=0){
	    int num=opb.read(5);
	    if(num==-1){
//            goto _eofout;
	      clear();
	      return(-1); 
	    }
	    lengthlist[i]=num+1;
	  }
	  else{
	    lengthlist[i]=0;
	  }
	}
      }
      else{
	// all entries used; no tagging
	for(i=0;i<entries;i++){
	  int num=opb.read(5);
	  if(num==-1){
//          goto _eofout;
	    clear();
	    return(-1); 
	  }
	  lengthlist[i]=num+1;
	}
      }
      break;
    case 1:
      // ordered
      {
	int length=opb.read(5)+1;
	lengthlist=new int[entries];

	for(i=0;i<entries;){
	  int num=opb.read(ilog(entries-i));
	  if(num==-1){
//          goto _eofout;
	    clear();
	    return(-1); 
	  }
	  for(int j=0;j<num;j++,i++){
	    lengthlist[i]=length;
	  }
	  length++;
	}
      }
      break;
    default:
      // EOF
      return(-1);
    }
  
    // Do we have a mapping to unpack?
    switch((maptype=opb.read(4))){
    case 0:
      // no mapping
      break;
    case 1:
    case 2:
      // implicitly populated value mapping
      // explicitly populated value mapping
      q_min=opb.read(32);
      q_delta=opb.read(32);
      q_quant=opb.read(4)+1;
      q_sequencep=opb.read(1);

      {
	int quantvals=0;
	switch(maptype){
	case 1:
	  quantvals=maptype1_quantvals();
	  break;
	case 2:
	  quantvals=entries*dim;
	  break;
	}
      
	// quantized values
	quantlist=new int[quantvals];
	for(i=0;i<quantvals;i++){
	  quantlist[i]=opb.read(q_quant);
	}
	if(quantlist[quantvals-1]==-1){
//        goto _eofout;
	  clear();
	  return(-1); 
	}
      }
      break;
    default:
//    goto _eofout;
      clear();
      return(-1); 
    }
    // all set
    return(0);
//    _errout:
//    _eofout:
//    vorbis_staticbook_clear(s);
//    return(-1); 
  }

  // there might be a straightforward one-line way to do the below
  // that's portable and totally safe against roundoff, but I haven't
  // thought of it.  Therefore, we opt on the side of caution
  private int maptype1_quantvals(){
    int vals=(int)(Math.floor(StrictMath.pow(entries,1./dim)));

    // the above *should* be reliable, but we'll not assume that FP is
    // ever reliable when bitstream sync is at stake; verify via integer
    // means that vals really is the greatest value of dim for which
    // vals^b->bim <= b->entries
    // treat the above as an initial guess
    while(true){
      int acc=1;
      int acc1=1;
      for(int i=0;i<dim;i++){
	acc*=vals;
	acc1*=vals+1;
      }
      if(acc<=entries && acc1>entries){	return(vals); }
      else{
	if(acc>entries){ vals--; }
	else{ vals++; }
      }
    }
  }
    
  void clear(){
//  if(quantlist!=null)free(b->quantlist);
//  if(lengthlist!=null)free(b->lengthlist);
//  if(nearest_tree!=null){
//    free(b->nearest_tree->ptr0);
//    free(b->nearest_tree->ptr1);
//    free(b->nearest_tree->p);
//    free(b->nearest_tree->q);
//    memset(b->nearest_tree,0,sizeof(encode_aux_nearestmatch));
//    free(b->nearest_tree);
//  }
//  if(thresh_tree!=null){
//    free(b->thresh_tree->quantthresh);
//    free(b->thresh_tree->quantmap);
//    memset(b->thresh_tree,0,sizeof(encode_aux_threshmatch));
//    free(b->thresh_tree);
//  }
//  memset(b,0,sizeof(static_codebook));
  }

  // unpack the quantized list of values for encode/decode
  // we need to deal with two map types: in map type 1, the values are
  // generated algorithmically (each column of the vector counts through
  // the values in the quant vector). in map type 2, all the values came
  // in in an explicit list.  Both value lists must be unpacked
  float[] unquantize(){

    if(maptype==1 || maptype==2){
      int quantvals;
      float mindel=float32_unpack(q_min);
      float delta=float32_unpack(q_delta);
      float[] r=new float[entries*dim];

       //System.err.println("q_min="+q_min+", mindel="+mindel);

      // maptype 1 and 2 both use a quantized value vector, but
      // different sizes
      switch(maptype){
      case 1:
	// most of the time, entries%dimensions == 0, but we need to be
	// well defined.  We define that the possible vales at each
	// scalar is values == entries/dim.  If entries%dim != 0, we'll
	// have 'too few' values (values*dim<entries), which means that
	// we'll have 'left over' entries; left over entries use zeroed
	// values (and are wasted).  So don't generate codebooks like that
	quantvals=maptype1_quantvals();
	for(int j=0;j<entries;j++){
	  float last=0.f;
	  int indexdiv=1;
	  for(int k=0;k<dim;k++){
	    int index=(j/indexdiv)%quantvals;
	    float val=quantlist[index];
	    val=Math.abs(val)*delta+mindel+last;
	    if(q_sequencep!=0)last=val;	  
	    r[j*dim+k]=val;
	    indexdiv*=quantvals;
	  }
	}
	break;
      case 2:
	for(int j=0;j<entries;j++){
	  float last=0.f;
	  for(int k=0;k<dim;k++){
	    float val=quantlist[j*dim+k];
//if((j*dim+k)==0){System.err.println(" | 0 -> "+val+" | ");}
	    val=Math.abs(val)*delta+mindel+last;
	    if(q_sequencep!=0)last=val;	  
	    r[j*dim+k]=val;
//if((j*dim+k)==0){System.err.println(" $ r[0] -> "+r[0]+" | ");}
	  }
	}
//System.err.println("\nr[0]="+r[0]);
      }
      return(r);
    }
    return(null);
  }

  private static int ilog(int v){
    int ret=0;
    while(v!=0){
      ret++;
      v>>>=1;
    }
    return(ret);
  }

  // 32 bit float (not IEEE; nonnormalized mantissa +
  // biased exponent) : neeeeeee eeemmmmm mmmmmmmm mmmmmmmm 
  // Why not IEEE?  It's just not that important here.

  static final int VQ_FEXP=10;
  static final int VQ_FMAN=21;
  static final int VQ_FEXP_BIAS=768; // bias toward values smaller than 1.

  // doesn't currently guard under/overflow 
  static long float32_pack(float val){
    int sign=0;
    int exp;
    int mant;
    if(val<0){
      sign=0x80000000;
      val= -val;
    }
    exp=(int)Math.floor(StrictMath.log(val)/StrictMath.log(2));
    mant=(int)StrictMath.rint(StrictMath.pow(val,(VQ_FMAN-1)-exp));
    exp=(exp+VQ_FEXP_BIAS)<<VQ_FMAN;
    return(sign|exp|mant);
  }

  static float float32_unpack(int val){
    float mant=val&0x1fffff;
    //float sign=val&0x80000000;
    float exp =(val&0x7fe00000)>>>VQ_FMAN;
//System.err.println("mant="+mant+", sign="+sign+", exp="+exp);
    //if(sign!=0.0)mant= -mant;
    if((val&0x80000000)!=0)mant= -mant;
//System.err.println("mant="+mant);
    return(ldexp(mant,((int)exp)-(VQ_FMAN-1)-VQ_FEXP_BIAS));
  }

  static float ldexp(float foo, int e){
    return (float)(foo*StrictMath.pow(2, e));
  }

/*
  // TEST
  // Unit tests of the dequantizer; this stuff will be OK
  // cross-platform, I simply want to be sure that special mapping cases
  // actually work properly; a bug could go unnoticed for a while

  // cases:
  //
  // no mapping
  // full, explicit mapping
  // algorithmic mapping
  //
  // nonsequential
  // sequential

  static int[] full_quantlist1={0,1,2,3, 4,5,6,7, 8,3,6,1};
  static int[] partial_quantlist1={0,7,2};

  // no mapping
  static StaticCodeBook test1=new StaticCodeBook(4,16,null,
						 0,0,0,0,0,
						 null,null,null);
  static float[] test1_result=null;
  
  // linear, full mapping, nonsequential
  static StaticCodeBook test2=new StaticCodeBook(4,3,null,
						 2,-533200896,1611661312,4,0,
						 full_quantlist1, null, null);
  static float[] test2_result={-3,-2,-1,0, 1,2,3,4, 5,0,3,-2};

  // linear, full mapping, sequential
  static StaticCodeBook test3=new StaticCodeBook(4,3,null,
						 2, -533200896,1611661312,4,1,
						 full_quantlist1,null, null);
  static float[] test3_result={-3,-5,-6,-6, 1,3,6,10, 5,5,8,6};

  // linear, algorithmic mapping, nonsequential
  static StaticCodeBook test4=new StaticCodeBook(3,27,null,
						 1,-533200896,1611661312,4,0,
						 partial_quantlist1,null,null);
  static float[] test4_result={-3,-3,-3, 4,-3,-3, -1,-3,-3,
				-3, 4,-3, 4, 4,-3, -1, 4,-3,
				-3,-1,-3, 4,-1,-3, -1,-1,-3, 
				-3,-3, 4, 4,-3, 4, -1,-3, 4,
				-3, 4, 4, 4, 4, 4, -1, 4, 4,
				-3,-1, 4, 4,-1, 4, -1,-1, 4,
				-3,-3,-1, 4,-3,-1, -1,-3,-1,
				-3, 4,-1, 4, 4,-1, -1, 4,-1,
				-3,-1,-1, 4,-1,-1, -1,-1,-1};

  // linear, algorithmic mapping, sequential
  static StaticCodeBook test5=new StaticCodeBook(3,27,null,
						 1,-533200896,1611661312,4,1,
						 partial_quantlist1,null,null);
  static float[] test5_result={-3,-6,-9, 4, 1,-2, -1,-4,-7,
				-3, 1,-2, 4, 8, 5, -1, 3, 0,
				-3,-4,-7, 4, 3, 0, -1,-2,-5, 
				-3,-6,-2, 4, 1, 5, -1,-4, 0,
				-3, 1, 5, 4, 8,12, -1, 3, 7,
				-3,-4, 0, 4, 3, 7, -1,-2, 2,
				-3,-6,-7, 4, 1, 0, -1,-4,-5,
				-3, 1, 0, 4, 8, 7, -1, 3, 2,
				-3,-4,-5, 4, 3, 2, -1,-2,-3};

  void run_test(float[] comp){
    float[] out=unquantize();
    if(comp!=null){
      if(out==null){
	System.err.println("_book_unquantize incorrectly returned NULL");
	System.exit(1);
      }
      for(int i=0;i<entries*dim;i++){
	if(Math.abs(out[i]-comp[i])>.0001){
	  System.err.println("disagreement in unquantized and reference data:\nposition "+i+": "+out[i]+" != "+comp[i]);
	  System.exit(1);
	}
      }
    }
    else{
      if(out!=null){
	System.err.println("_book_unquantize returned a value array:\n  correct result should have been NULL");
	System.exit(1);
      }
    }
  }

  public static void main(String[] arg){
    // run the nine dequant tests, and compare to the hand-rolled results
    System.err.print("Dequant test 1... ");
    test1.run_test(test1_result);
    System.err.print("OK\nDequant test 2... ");
    test2.run_test(test2_result);
    System.err.print("OK\nDequant test 3... ");
    test3.run_test(test3_result);
    System.err.print("OK\nDequant test 4... ");
    test4.run_test(test4_result);
    System.err.print("OK\nDequant test 5... ");
    test5.run_test(test5_result);
    System.err.print("OK\n\n");
  }
*/
}





