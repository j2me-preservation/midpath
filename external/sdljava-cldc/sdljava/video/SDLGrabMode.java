package sdljava.video;
/**
 *  sdljava - a java binding to the SDL API
 *
 *  Copyright (C) 2004  Ivan Z. Ganza
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA
 *
 *  Ivan Z. Ganza (ivan_ganza@yahoo.com)
 */

/**
 *
 *
 */

public final class SDLGrabMode {
  public final static SDLGrabMode SDL_GRAB_QUERY = new SDLGrabMode("SDL_GRAB_QUERY", -1);
  public final static SDLGrabMode SDL_GRAB_OFF = new SDLGrabMode("SDL_GRAB_OFF", 0);
  public final static SDLGrabMode SDL_GRAB_ON = new SDLGrabMode("SDL_GRAB_ON", 1);
  public final static SDLGrabMode SDL_GRAB_FULLSCREEN = new SDLGrabMode("SDL_GRAB_FULLSCREEN");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static SDLGrabMode swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + SDLGrabMode.class + " with value " + swigValue);
  }

  private SDLGrabMode(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private SDLGrabMode(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private static SDLGrabMode[] swigValues = { SDL_GRAB_QUERY, SDL_GRAB_OFF, SDL_GRAB_ON, SDL_GRAB_FULLSCREEN };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}


