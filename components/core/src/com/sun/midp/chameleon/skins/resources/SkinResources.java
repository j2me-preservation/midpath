/*
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */

package com.sun.midp.chameleon.skins.resources;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;

import com.sun.midp.configurator.Constants;
import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;
import com.sun.midp.main.Configuration;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.util.ResourceHandler;

/** Utility class for skin resources. */ 
public class SkinResources {

    /** Constant to distinguish image resources with no index */
    private static final int NO_INDEX = -1;

    /**
     * Loaded skin resources. This object is used as resources
     * cache, so we don't have to load same resource again. In
     * MVM case this object is shared between Isolates, so all
     * Isolates can benefit from caching. However, only AMS
     * Isolate can put resources into the cache.
     */
    private static LoadedSkinResources resources;
    
    /**
     * Loaded skin properties. In MVM case, this object is shared
     * between Isolates.
     */
    private static LoadedSkinProperties skinProperties;
    
    /**
     * A private internal reference to the system security token
     */
    private static SecurityToken secureToken;
    
    /**
     * A special internal reference to a tunnel which will provide
     * package access to Image inside of javax.microedition.lcdui.
     */
    private static ImageTunnel imageTunnel;

    /**
     * True, if skin is being loaded in AMS Isolate in MVM case,
     * false otherwise. Always true in SVM case.
     */
    private static boolean isAmsIsolate;
    
    private static boolean skinImageEnabled;
    
    /**
     * This class needs no real constructor, but its here as 'public'
     * so the SecurityIntializer can do a newInstance() on it and call
     * the initSecurityToken() method.
     */
    private SkinResources() {
    }

    /**
     * This method is called by the SecurityInitializer class to
     * initialize the security token for this class to utilize
     * secure system functionality
     *
     * @param st the SecurityToken instance used throughout the system
     * @param tunnel used to get images from the public LCDUI package
     */
    public static void initSecurityToken(SecurityToken st, 
                                         ImageTunnel tunnel) 
    {
        if (secureToken == null) {
            secureToken = st;
            imageTunnel = tunnel;
        }
        loadSkin(false);
    }
    
    /**
     * Load the skin, including all its properties and images. Some parts
     * of the skin may be lazily initialized, but this method starts the
     * process. If the flag to 'reload' is true, the method will ignore
     * all previously loaded resources and go through the process again.
     *
     * @param reload if true, ignore previously loaded resources and reload
     */
    public static void loadSkin(boolean reload) {
    	
    	// Check if skin images are enabled
    	skinImageEnabled = Configuration.getPropertyDefault("com.sun.midp.chameleon.skins.imageEnabled", "yes").equals("yes") ? true : false;
    	
        resources = null;
        skinProperties = null;
        isAmsIsolate = isAmsIsolate();
            
        if (reload) {
            if (Logging.REPORT_LEVEL <= Logging.CRITICAL) {
                Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI,
                        "Skin reloading isn't implemented yet");
            }
        }
        
        if (!isAmsIsolate) {
            resources = (LoadedSkinResources)getSharedResourcePool();
            skinProperties = (LoadedSkinProperties)getSharedSkinProperties();
        } else {
            resources = new LoadedSkinResources();
            skinProperties = new LoadedSkinProperties();

            RomizedSkin.getProperties(skinProperties);
            resources.fonts = new Font[skinProperties.fontValues.length];
            resources.images = new Image[skinProperties.imageValues.length];
            
            // For MVM, share resources cache and loaded properties
            // between Isolates. For SVM, those methods do nothing.
            shareResourcePool(resources);
            shareSkinProperties(skinProperties);
        }
        
        
        // After reading in the properties from storage (either ROM
        // image or filesystem, we establish all the individual values
        // in the various properties classes
        boolean loadAll = ifLoadAllResources();
        loadResources(loadAll);
        
        // NOTE: The remaining properties classes need to be set and
        // possibly re-loaded lazily. 
        
        // IMPL_NOTE if (reload), need to signal the LF classes in a way they
        // will re-load their skin resources on demand
    }

    /**
     * Utility method used by skin property classes to load
     * image resources.
     * 
     * @param identifier a unique identifier for the image property
     * @return the Image if one is available, null otherwise
     */
    public static Image getImage(int identifier) {
        return getImage(identifier, NO_INDEX);
    }
    
    /**
     * Utility method used by skin property classes to load
     * image resources.
     * 
     * @param identifier a unique identifier for the image property
     * @param index index of the image
     *
     * @return the Image if one is available, null otherwise
     */
    public static Image getImage(int identifier, int index) {
        int imageIdx = 0;
        String imageIdentifier = null;
        Image image = null;
        boolean isLoaded = false;
        
        // If we're set not to bother loading images, just return null
        if (!Constants.CHAM_USE_IMAGES) {
            return null;
        }

        try {
            imageIdx = skinProperties.properties[identifier];
            if (index != NO_INDEX) {
                imageIdx += index;
            }

            // Check if this resource alread has been loaded
            image = resources.images[imageIdx];
        
            if (image != null) {
                // image already has been loaded
                isLoaded = true;
            } else {
                byte[] imageData;
           
                imageIdentifier = skinProperties.imageValues[imageIdx];
                if (imageIdentifier.length() == 0) {
                    // image hasn't been specified
                    return null;
                }

                if (imageIdentifier == null) {
                    if (Logging.REPORT_LEVEL <= Logging.CRITICAL) {
                        Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI, 
                            "No value for skin property: id = " + identifier);
                    }

                    return null;
                }
                
                //System.out.println("[DEBUG] SkinResources.getImage(): " + imageIdentifier);

                // Try to load the romized image
//                if (Constants.CHAM_ROMIZED_IMAGES && imageTunnel != null) {
//                    int romIndex = RomizedSkin.getRomizedImageIndex(imageIdx);
//                    System.out.println("[DEBUG] SkinResources.getImage() 2: " + imageIdentifier);
//                    if (romIndex != -1) {
////                        int imageDataPtr = getRomizedImageDataArrayPtr(
////                                romIndex);
////                        int imageDataLength = getRomizedImageDataArrayLength(
////                                romIndex);
//                        //image = imageTunnel.getRomizedImage(imageDataPtr, 
//                        //        imageDataLength);
//                    	System.out.println("[DEBUG] SkinResources.getImage() 3: " + imageIdentifier);
//                    	image = imageTunnel.getRomizedImage(imageIdentifier);
//                    }
//                } 
                
                if (Constants.CHAM_ROMIZED_IMAGES && imageTunnel != null) {
                	image = imageTunnel.getRomizedImage(imageIdentifier);
                }
                
                // Image is not romized, load from filesystem
                if (image == null) {
                    // Try to load the PNG image from the filesystem
                    imageData = ResourceHandler.getSystemResource(
                            secureToken, imageIdentifier + ".png");

                    // Try to load the raw image from the filesystem
                    if (imageData == null) {
                        imageData = ResourceHandler.getSystemResource(
                                secureToken, imageIdentifier + ".raw");
                    }

                    if (imageData != null) {
                        image = Image.createImage(
                                imageData, 0, imageData.length);
                    }
                }
            }
        } catch (Throwable t) {
            if (Logging.REPORT_LEVEL <= Logging.CRITICAL) {
                Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI,
                        "Exception caught while loading Image resource " +
                        imageIdentifier + ": " + t);
            }
        }
        
        if (image != null) {
            if (!isLoaded && isAmsIsolate) {
                // add resource to shared resources pool
                resources.images[imageIdx] = image;
            }
        } else {
            if (Logging.REPORT_LEVEL <= Logging.CRITICAL) {
                Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI,
                        "No resource found when loading Image resource: " + 
                        imageIdentifier);
            }
        }

        return image;
    }

    /**
     * Utility method used by skin property classes to load
     * composite image resources consisting of a few images.
     *
     * @param identifier a unique identifier for the composite image property
     * @param piecesNumber number of pieces consisting the composite image 
     *
     * @return the Image[] with loaded image pieces,
     * or null if some of the pieces is not available
     */
    public static Image[] getCompositeImage(
            int identifier, int piecesNumber) {
        Image[] result = new Image[piecesNumber];
        for (int i = 0; i < piecesNumber; i++) {
            result[i] = getImage(identifier, i);
            if (result[i] == null) {
                result = null;
                break;
            }
        }
        return result;
    }

    /**
     * Utility method used by skin property classes to load
     * Font resources.
     *
     * @param identifier a unique identifier for the Font property
     * @return the Font object or null in case of error
     */     
    public static Font getFont(int identifier) {
        int fontIdx = 0;
        int fontIdentifier = 0;
        Font font = null;
        boolean isLoaded = false;

        try {
            fontIdx = skinProperties.properties[identifier];

            // Check if this resource alread has been loaded
            font = resources.fonts[fontIdx];
            
            if (font != null) {
                isLoaded = true;
            } else {
                fontIdentifier = skinProperties.fontValues[fontIdx];
                font = FontResources.getFont(fontIdentifier);
            }
        } catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.CRITICAL) {
                Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI,
                        "Exception caught while loading Font resource: " + 
                        identifier + ": " + e);
            }
        }
            
        if (font != null) {
            if (!isLoaded && isAmsIsolate) {
                // add resource to shared resources pool
                resources.fonts[fontIdx] = font;
            }
        } else {
            if (Logging.REPORT_LEVEL <= Logging.CRITICAL) {
                Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI,
                        "No resource found when loading Font resource: " + 
                        fontIdentifier);
            }

            font = FontResources.getFont(500);
        }

        return font;
    }
    
    /**
     * Utility method used by skin property classes to load
     * String resources.
     *
     * @param identifier a unique identifier for the String property
     * @return the String object or null in case of error
     */     
    public static String getString(int identifier) {
        try {
            int stringIdx = skinProperties.properties[identifier];
            return skinProperties.stringValues[stringIdx];
        } catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.CRITICAL) {
                Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI,
                        "Exception caught while loading String resource: " + 
                        identifier + ": " + e);
            }
            return null;
        }
    }
    
    /**
     * Utility method used by skin property classes to load
     * integer resources.
     *
     * @param identifier a unique identifier for the integer property
     * @return an integer or -1 in case of error
     */     
    public static int getInt(int identifier) {
        try {
            return skinProperties.properties[identifier];
        } catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.CRITICAL) {
                Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI,
                        "Exception caught while loading integer resource: " +
                        identifier + ": " + e);
            }            
            return -1;
        }        
    }
    
    /**
     * Returns sequence of integer numbers corresponding to 
     * specified property identifer.
     *
     * @param identifier a unique identifier for the property
     * @return the int[] representing the sequence or null in case of error
     */
    public static int[] getNumbersSequence(int identifier) {
        try {
            int seqIdx = skinProperties.properties[identifier];
            int totalNumbers = skinProperties.intSeqValues[seqIdx];
            seqIdx += 1;
            int[] nums = new int[totalNumbers];
            for (int i = 0; i < totalNumbers; ++i) {
                nums[i] = skinProperties.intSeqValues[seqIdx + i];
            }
            
            return nums;
        } catch (Exception e) {
            Logging.report(Logging.CRITICAL, LogChannels.LC_HIGHUI,
                    "Exception caught while loading integers sequence " + 
                    "resource: " + identifier + ": " + e);
        }
        return null;
    } 
    
    
    /**
	 * @return the resourceImageEnabled
	 */
	static boolean isSkinImageEnabled() {
		return skinImageEnabled;
	}

	/**
     * Load resources data.
     * 
     * @param loadAll if true, load all resources. Otherwise, 
     * load only selected resources. The rest will be loaded 
     * lazily.
     */
    private static void loadResources(boolean loadAll) {
        // load selected resources
        ScreenResources.load();
        ScrollIndResources.load();
        SoftButtonResources.load();
        TickerResources.load();
        PTIResources.load();
        TitleResources.load();

        // load the rest of resources
        if (loadAll) {
            AlertResources.load();
            BusyCursorResources.load();
            ChoiceGroupResources.load();
            DateEditorResources.load();
            DateFieldResources.load();
            GaugeResources.load();
            ImageItemResources.load();
            MenuResources.load();
            ProgressBarResources.load();
            StringItemResources.load();
            TextFieldResources.load();
            UpdateBarResources.load();
        }
    }
       
    /**
     * Determines if this is the AMS isolate.
     * 
     * @return true if this is the AMS isolate.
     */
    private static boolean isAmsIsolate() {
    	// TODO
    	if (Logging.TRACE_ENABLED)
    		System.out.println("[DEBUG] SkinResources.isAmsIsolate(): not yet implemented");
    	return true;
    }

    /**
     * Native method to store a strong cross-isolate reference
     * to the resource pool Object. This will allow the CSkin
     * class in all isolates to share the same loaded resources.
     * @param pool object reference to store in the resource pool
     */
    private static void shareResourcePool(Object pool) {
        	// TODO
    	if (Logging.TRACE_ENABLED)
        	System.out.println("[DEBUG] SkinResources.shareResourcePool(): not yet implemented");
    }
    
    /**
     * Native method to retrieve the cross-isolate resource 
     * pool Object. This will allow the CSkin class in all
     * isolates to share the same loaded resources.
     * @return a shared resource pool object reference
     */
    private static Object getSharedResourcePool() {
    	// TODO
    	if (Logging.TRACE_ENABLED)
    		System.out.println("[DEBUG] SkinResources.getSharedResourcePool(): not yet implemented");
    	return null;
    }

    /**
     * Native method to store a strong cross-isolate reference
     * to the skin properties Object. This will allow the CSkin
     * class in all isolates to share skin properties.
     * @param props object reference to store 
     */
    private static void shareSkinProperties(Object props) {
    	// TODO 
    	if (Logging.TRACE_ENABLED)
    		System.out.println("[DEBUG] SkinResources.shareSkinProperties(): not yet implemented");
    }
    
    /**
     * Native method to retrieve the cross-isolate skin 
     * properties Object. This will allow the CSkin class 
     * in all isolates to share skin properties.
     * @return a shared skin properties object reference
     */
    private static Object getSharedSkinProperties() {
    	// TODO
    	if (Logging.TRACE_ENABLED)
    		System.out.println("[DEBUG] SkinResources.getSharedSkinProperties(): not yet implemented");
    	return null;
    }
    
    /**
     * Determine how skin resources should be loaded: at once, 
     * during skin loading, or lazily, on first use.
     * @return true if all resources should be loaded at once.
     */
    private static boolean ifLoadAllResources() {
        	// TODO
    	if (Logging.TRACE_ENABLED)
        	System.out.println("[DEBUG] SkinResources.ifLoadAllResources(): not yet implemented");
        return false;
    }

    /**
     * Returns native pointer to romized image data array as Java int
     *
     * @param imageId romized image id
     * @return native pointer to romized image data array as Java int,
     * if image is not romized the return value is 0
     */
    private static int getRomizedImageDataArrayPtr(int imageId) {
    	// TODO
    	if (Logging.TRACE_ENABLED)
    		System.out.println("[DEBUG] SkinResources.getRomizedImageDataArrayPtr(): not yet implemented");
    	return 0;
    }

    /**
     * Returns length of native romized image data array
     *
     * @param imageId romized image id
     * @return length of native romized image data array,
     * if image is not romized the return value is -1
     */
    private static int getRomizedImageDataArrayLength(int imageId) {
        	// TODO
    	if (Logging.TRACE_ENABLED)
        	System.out.println("[DEBUG] SkinResources.getRomizedImageDataArrayPtr(): not yet implemented");
        return -1;
        
    }
}

/**
 * Class for caching loaded resources. Because we know about 
 * all resources in advance, we can assign integer value to 
 * each resource. This value is used as index into arrays below.
 */
final class LoadedSkinResources {
    /** Loaded fonts */
    Font[] fonts;

    /** Loaded images */
    Image[] images;
    



    //private native void finalize();
    //protected native void finalize();

    
}

/**
 * Class reperesenting loaded skin properties.
 */
final class LoadedSkinProperties {
    /**
     * Skin properties. Each property has an integer value associated
     * with it, which is used as index into this array. The meaning of 
     * value in this array depends of the property type. 
     * For integer property, the value stored in array is the value of 
     * property itself.
     * For string property, the value stored in array is an index into 
     * another array, where string values are stored.
     * For image property, the value stored in array is an index into 
     * another array, where images identifiers are stored.
     * For font property, the value stored in array is an index into
     * another array, where fonts indentifiers are stored.
     * For integer numbers sequence property, the value stored in array
     * is an index into another array, where integer numbers sequences
     * are stored.
     */
    int[] properties;
    
    /**
     * Array with values for string properties.
     */
    String[] stringValues;
    
    /**
     * Array with images identifiers for image properties.
     */
    String[] imageValues;

    /**
     * Array with font indentifiers for font properties.
     */
    int[] fontValues;

    /**
     * Array with integer numbers sequences for integer numbers
     * sequence properties. Each sequence is stored as number of
     * integers in sequence followed by integers themself.
     */
    int[] intSeqValues;
    



    //protected native void finalize();
    //private native void finalize();

}
