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

package javax.microedition.lcdui;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.game.Sprite;

import com.sun.midp.chameleon.layers.PopupLayer;
import com.sun.midp.chameleon.skins.DateEditorSkin;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.configurator.Constants;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;

/**
 * A utility class for editing date/time components for a DateField.
 */
class DateEditor extends PopupLayer implements CommandListener {     

    /**
     * Create a new DateEditor layer.
     *
     * @param lf The DateFieldLFImpl that triggered this date editor
     */
    public DateEditor(DateFieldLFImpl lf) {
        super(DateEditorSkin.IMAGE_BG, DateEditorSkin.COLOR_BG);
        this.lf = lf;
        
        mode = lf.df.mode;
        initialized = lf.df.initialized;
        editDate = Calendar.getInstance();
        Date date = lf.df.getDate();
        if (date != null) {
            editDate.setTime(date);
        }
        
        selectedDate = hilightedDate = editDate.get(Calendar.DATE);
        
        if (editDate.get(Calendar.AM_PM) == Calendar.AM) {
            amSelected = true;
            amHilighted = true;
        }
        
        switch (mode) {
            case DateField.DATE:
                focusOn = MONTH_POPUP;
                populateDateComponents();
                break;
            case DateField.TIME:
                focusOn = HOURS_POPUP;
                timeComponentsOffset = 0;
                populateTimeComponents();
                break;
            case DateField.DATE_TIME:
                focusOn = MONTH_POPUP;
                timeComponentsOffset = 98;
                populateDateComponents();
                populateTimeComponents();
                break;
            default:
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                               "DateEditor constructor, mode=" +mode);
                break;
        }
        
        setCommands(commands);
        setCommandListener(this);
    }
      
    /**
     * Sets the location of the popup layer.
     *
     * @param x the x-coordinate of the popup layer location
     * @param y the y-coordinate of the popup layer location
     */
    public void setLocation(int x, int y) {
        bounds[X] = x;
        bounds[Y] = y;
        bounds[H] = DateEditorSkin.HEIGHT;
        
        switch (mode) {
            case DateField.DATE:
                bounds[W] = DateEditorSkin.WIDTH_DATE;
                break;
            case DateField.TIME:
                bounds[W] = DateEditorSkin.WIDTH_TIME;
                break;
            case DateField.DATE_TIME:
                bounds[W] = DateEditorSkin.WIDTH_DATETIME;
                break;
            default:
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                               "DateEditor.setLocation(), mode=" +mode);
                break;
        }
        
        if (bounds[X] + bounds[W] > ScreenSkin.WIDTH) {
            bounds[X] = ScreenSkin.WIDTH - bounds[W];
        } else if (bounds[X] < 0) {
            bounds[X] = 0;
        }
        
    }

    /**
     * Paints the background of the date editor layer.
     *
     * @param g The graphics context to paint to
     */

    public void paintBackground(Graphics g) {
        super.paintBackground(g);
        if (DateEditorSkin.IMAGE_BG == null) {
            g.setColor(DateEditorSkin.COLOR_BORDER);
            g.drawRect(0, 0, bounds[W] - 1, bounds[H] - 1);
            g.setColor(0);
        }
    }

    /**
     * Paints the body (open state) of the date editor layer.
     *
     * @param g The graphics context to paint to
     */
    public void paintBody(Graphics g) {        
        setDayOffset();
        lastDay = daysInMonth(editDate.get(Calendar.MONTH),
            editDate.get(Calendar.YEAR));
        
        nextX = 0;
        nextY = 0;
        
        switch (mode) {
            case DateField.DATE:
                drawDateComponents(g);
                break;
            case DateField.TIME:
                drawTimeComponents(g);
                break;
            case DateField.DATE_TIME:
                drawDateComponents(g);
                g.translate(timeComponentsOffset, 0);
                drawTimeComponents(g);
                break;
            default:
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                               "DateEditor.paintBody(), mode=" +mode);
                break;
        }        
    }

    /**
     * Handles key input to the popup layer.
     *
     * @param type the type of this key event (pressed, released)
     * @param code the code of this key event
     * @return true always, since popupLayers swallow all key events
     */
    public boolean keyInput(int type, int code) {        
        if (type == EventConstants.PRESSED && lf != null) {
            if (code == Constants.KEYCODE_SELECT) {
                selectFired();
                requestRepaint();
            } else {
                traverseEditor(code);
                requestRepaint();
            }
        }
        // PopupLayers always swallow all key events
        return (code != EventConstants.SOFT_BUTTON1 &&
                code != EventConstants.SOFT_BUTTON2);
    }

    /**
     * Handle a command action.
     *
     * @param cmd The Command to handle
     * @param s   The Displayable with the Command
     */
    public void commandAction(Command cmd, Displayable s) {
        
        lf.uCallKeyPressed(Constants.KEYCODE_SELECT);
        
        if (cmd == set) {
            lf.saveDate(editDate.getTime());
        }
            
        // SYNC NOTE: Move the call to the application's
        // ItemStateListener outside LCDUILock
        Form form = null;
        synchronized (Display.LCDUILock) {         
            if (lf.df.owner instanceof Form) {
                form = (Form)lf.df.owner;
            }
        }
        
        if (form != null) {
            form.uCallItemStateChanged(lf.df);
        }
    }

    // ********** package private *********** //
    /**
     * Show the date editor popup.
     */
    void show() {
        // refresh the edit date to value stored in DateField each time
        editDate = Calendar.getInstance();
        Date date = lf.df.getDate();
        if (date != null) {
            editDate.setTime(date);
        }
        
        selectedDate = hilightedDate = editDate.get(Calendar.DATE);
        
        amSelected = amHilighted = false;
        if (editDate.get(Calendar.AM_PM) == Calendar.AM) {
            amSelected = true;
            amHilighted = true;
        }
        
        switch (mode) {
            case DateField.DATE:
            case DateField.DATE_TIME:
                focusOn = MONTH_POPUP;
                break;
            case DateField.TIME:
                focusOn = HOURS_POPUP;
                break;
            default:
                Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                               "DateEditor.show(), mode=" +mode);
                break;
        }
        
        ScreenLFImpl sLF = (ScreenLFImpl)lf.df.owner.getLF();
        sLF.lGetCurrentDisplay().showPopup(this);
    }

    /**
     * Hide all sub-popups triggered by this date editor.
     */
    void hideAllPopups() {
        ScreenLFImpl sLF = (ScreenLFImpl)lf.df.owner.getLF();
        sLF.lGetCurrentDisplay().hidePopup(monthPopup);
        sLF.lGetCurrentDisplay().hidePopup(yearPopup);
        sLF.lGetCurrentDisplay().hidePopup(hoursPopup);
        sLF.lGetCurrentDisplay().hidePopup(minutesPopup);
    }

    // *********** private ************ //
    /**
     * Populate the date components.
     */
    protected void populateDateComponents() {
        // populate MONTHS[]
        MONTHS = new String[DateFieldLFImpl.MONTH_NAMES.length];
        for (int i = 0; i < DateFieldLFImpl.MONTH_NAMES.length; i++) {
            MONTHS[i] = DateFieldLFImpl.MONTH_NAMES[i].substring(0, 3);
        }
        monthPopup = new DEPopupLayer(this, MONTHS,
                                      editDate.get(Calendar.MONTH), true);
        
        // populate YEARS[]
        int selectedIndex = 
            createYearStrings(editDate.get(Calendar.YEAR) - 10);
        yearPopup = new DEPopupLayer(this, YEARS, selectedIndex, false);
    }

    /**
     * Recreates years string given a start year.
     * @param startYear the first year to be added to the YEARS array
     * @return selected year in the newly created array
     */
    protected int createYearStrings(int startYear) {
        int selectedIndex = 0;
        int year = startYear;
        YEARS = new String[22];
        YEARS[0]  = Resource.getString(ResourceConstants.LCDUI_DF_YEAR_BEFORE);
        YEARS[21] = Resource.getString(ResourceConstants.LCDUI_DF_YEAR_AFTER);
        for (int i = 1; i < 21; i++) {
            if (year == editDate.get(Calendar.YEAR)) {
                selectedIndex = i;
            }
            YEARS[i] = Integer.toString(year++);
        }
        return selectedIndex;
    }

    /**
     * Populate the time components.
     */
    protected void populateTimeComponents() {
        int selectedIndex = 0;
        
        // populate HOURS[]
        int endIndex;
        String[] hours;
        if (lf.CLOCK_USES_AM_PM) {
            HOURS = new int[12];
            hours = new String[12];
            
            selectedIndex = editDate.get(Calendar.HOUR) - 1;
            if (selectedIndex < 0) {
                selectedIndex = 11;
            }

            for (int i = 0; i < 12; i++) {
                HOURS[i] = i + 1;
                hours[i] = Integer.toString(i + 1);
            }
        } else {
            HOURS = new int[24];
            hours = new String[24];
            selectedIndex = editDate.get(Calendar.HOUR_OF_DAY);
            for (int i = 0; i < 24; i++) {
                HOURS[i] = i;
                hours[i] = Integer.toString(i);
            }
        }
        hoursPopup = new DEPopupLayer(this, hours, selectedIndex, true);
        
        // populate MINUTES[]
        selectedIndex = 0;
        MINUTES = new int[60];
        String[] minutes = new String[60];
        int minute = editDate.get(Calendar.MINUTE);
        for (int i = 0; i < 60; i++) {
            if (i == minute) {
                selectedIndex = i;
            }
            MINUTES[i] = i;
            minutes[i] = Integer.toString(i);
        }
        minutesPopup = new DEPopupLayer(this, minutes, selectedIndex, true);
    }


    /**
     * Draw the date components.
     *
     * @param g The Graphics object to paint to
     */
    protected void drawDateComponents(Graphics g) {
        nextX = (mode == DateField.DATE) ? 10 : 4;
        nextY = 5;
        
        g.translate(nextX, nextY);
        if (DateEditorSkin.IMAGE_MONTH_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_MONTH_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            int w = DateEditorSkin.IMAGE_MONTH_BG.getWidth();
            int h = DateEditorSkin.IMAGE_MONTH_BG.getHeight();
            if (focusOn == MONTH_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            monthPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            monthPopup.setBounds(g.getTranslateX(),
                                 g.getTranslateY() + h,
                                 w, DateEditorSkin.HEIGHT_POPUPS);
        }
        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(MONTHS[editDate.get(Calendar.MONTH)],
                     4, 0, Graphics.LEFT | Graphics.TOP);

        g.translate(45, 0);
        if (DateEditorSkin.IMAGE_YEAR_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_YEAR_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            int w = DateEditorSkin.IMAGE_YEAR_BG.getWidth();
            int h = DateEditorSkin.IMAGE_YEAR_BG.getHeight();
            if (focusOn == YEAR_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            yearPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            yearPopup.setBounds(g.getTranslateX(),
                                 g.getTranslateY() + h,
                                 w, DateEditorSkin.HEIGHT_POPUPS);
        }

        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(Integer.toString(editDate.get(Calendar.YEAR)),
                     4, 0, Graphics.LEFT | Graphics.TOP);
        g.translate(-nextX - 45, -nextY);
        
        int x = (mode == DateField.DATE) ? 10 : 4;
        g.translate(x, 29);
        paintCalendar(g);
        g.translate(-x, -29);
    }
    
    /**
     * Draw the time components.
     *
     * @param g The Graphics object to paint to
     */
    protected void drawTimeComponents(Graphics g) {
        nextX = (mode == DateField.TIME) ? 17 : 0;
        nextY = (mode == DateField.TIME) ? 10 : 5;
        
        g.translate(nextX, nextY);
        if (DateEditorSkin.IMAGE_TIME_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_TIME_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            int w = DateEditorSkin.IMAGE_TIME_BG.getWidth();
            int h = DateEditorSkin.IMAGE_TIME_BG.getHeight();
            if (focusOn == HOURS_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            hoursPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            hoursPopup.setBounds(g.getTranslateX(),
                                 g.getTranslateY() + h,
                                 w, DateEditorSkin.HEIGHT_POPUPS);
        }

        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(Integer.toString(HOURS[hoursPopup.getSelectedIndex()]), 
                     3, 0, Graphics.LEFT | Graphics.TOP);
                     
        g.translate(34, 0);
        if (DateEditorSkin.IMAGE_TIME_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_TIME_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            int w = DateEditorSkin.IMAGE_TIME_BG.getWidth();
            int h = DateEditorSkin.IMAGE_TIME_BG.getHeight();
            if (focusOn == MINUTES_POPUP) {
                g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                g.drawRect(-2, -2, w + 3, h + 3);
            }
            minutesPopup.setElementSize(
                w - 4, DateEditorSkin.FONT_POPUPS.getHeight());
            minutesPopup.setBounds(g.getTranslateX(),
                                   g.getTranslateY() + h,
                                   w, DateEditorSkin.HEIGHT_POPUPS);
        }

        g.setFont(DateEditorSkin.FONT_POPUPS);
        g.setColor(0);
        g.drawString(DateFieldLFImpl.twoDigits(editDate.get(Calendar.MINUTE)),
                     3, 0, Graphics.LEFT | Graphics.TOP);
                     
        g.translate(-nextX - 34, -nextY);
        
        nextX = (mode == DateField.TIME) ? 15 : 0;
        nextY = 29;
        g.translate(nextX, nextY);
        paintAmPm(g);
    }

    /**
     * Paint the Calendar.
     *
     * @param g The Graphics context to paint to
     */
    protected void paintCalendar(Graphics g) {
        if (DateEditorSkin.IMAGE_CAL_BG == null ||
            DateEditorSkin.IMAGE_DATES == null)
        {
            return;
        }

        g.drawImage(DateEditorSkin.IMAGE_CAL_BG, 0, 0,
                    Graphics.LEFT | Graphics.TOP);
        
        if (DateEditorSkin.IMAGE_DATES == null) {
            return;
        }
        g.translate(2, 0);
        
        int o = DateEditorSkin.IMAGE_CAL_BG.getWidth() / 7;
        int rem = DateEditorSkin.IMAGE_CAL_BG.getWidth() % 7;
        int rowH = 11;
        int h = (int)(DateEditorSkin.IMAGE_DATES.getHeight() / 31);         
        int w = DateEditorSkin.IMAGE_DATES.getWidth();
        
        // draw calendar
        int x = 5 + ((dayOffset - 1) * o);
        int y = h + 4;
        
        if (hilightedDate > lastDay) {
            hilightedDate = lastDay;
        }
        
        calendarTopLimit = y;
        int lastCol = 7 * o;
        for (int i = 1; i <= lastDay; ++i) {
            // draw focus highlight
            if (i == hilightedDate) {
                dateHilightX = x;
                dateHilightY = y;
                g.setColor(
                    (focusOn == CALENDAR) ? 
                        DateEditorSkin.COLOR_TRAVERSE_IND:
                        0);
                g.drawRect(x - 6, y - 1, w, h + 1);
            }
            
            g.drawRegion(DateEditorSkin.IMAGE_DATES,
                         0, ((i - 1) * h),
                         w, h,
                         Sprite.TRANS_NONE,
                         x, y,
                         Graphics.TOP | Graphics.HCENTER);
                         
            x += o;
            if (x > lastCol) {
                calendarRightLimit = x - o;
                x = 5;
                y += rowH;
            }
        }
        calendarBottomLimit = y;
        g.translate(-2, 0);
    }
    
    /**
     * Paint the am/pm indicators.
     * 
     * @param g The graphics context to paint to
     */
    protected void paintAmPm(Graphics g) {
        int clockStartX, clockStartY;

        if (!lf.CLOCK_USES_AM_PM) {
            clockStartY = 9;
        } else {
            // paint AM
            if (DateEditorSkin.IMAGE_RADIO != null) {
                g.drawImage((amSelected) ? 
                            DateEditorSkin.IMAGE_RADIO[1] : 
                            DateEditorSkin.IMAGE_RADIO[0],
                            0, 0, Graphics.LEFT | Graphics.TOP);
                
                if ((focusOn == AM_PM) && (amHilighted)) {
                    g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                    g.drawRect(0, 0, 
                               DateEditorSkin.IMAGE_RADIO[0].getWidth(), 
                               DateEditorSkin.IMAGE_RADIO[0].getHeight());
                    g.setColor(0);
                }
                
                if (DateEditorSkin.IMAGE_AMPM != null) {
                    int w = (int)(DateEditorSkin.IMAGE_AMPM.getWidth() / 2);
                    g.drawRegion(DateEditorSkin.IMAGE_AMPM,
                                 0, 0, 
                                 w, DateEditorSkin.IMAGE_AMPM.getHeight(),
                                 Sprite.TRANS_NONE,
                                 DateEditorSkin.IMAGE_RADIO[0].getWidth(), 
                                 (DateEditorSkin.IMAGE_RADIO[0].getHeight()/2),
                                 Graphics.VCENTER | Graphics.LEFT);
                }
            }
            
            g.translate(35, 0);
            // paint PM
            if (DateEditorSkin.IMAGE_RADIO != null) {
                g.drawImage((amSelected) ?
                            DateEditorSkin.IMAGE_RADIO[0] : 
                            DateEditorSkin.IMAGE_RADIO[1],
                            0, 0, Graphics.LEFT | Graphics.TOP);
                
                if ((focusOn == AM_PM) && (!amHilighted)) {
                    g.setColor(DateEditorSkin.COLOR_TRAVERSE_IND);
                    g.drawRect(0, 0, 
                               DateEditorSkin.IMAGE_RADIO[0].getWidth(), 
                               DateEditorSkin.IMAGE_RADIO[0].getHeight());
                    g.setColor(0);
                }
                
                if (DateEditorSkin.IMAGE_AMPM != null) {
                    int w = (int)(DateEditorSkin.IMAGE_AMPM.getWidth() / 2);
                    g.drawRegion(DateEditorSkin.IMAGE_AMPM,
                                 (DateEditorSkin.IMAGE_AMPM.getWidth() / 2), 0, 
                                 w, DateEditorSkin.IMAGE_AMPM.getHeight(),
                                 Sprite.TRANS_NONE,
                                 DateEditorSkin.IMAGE_RADIO[0].getWidth(), 
                                 (DateEditorSkin.IMAGE_RADIO[0].getHeight()/2),
                                 Graphics.VCENTER | Graphics.LEFT);
                }
            }
            g.translate(-35, 0);
            clockStartY = 22;
        }

        clockStartX = (mode == DateField.TIME) ? 10 : 6;
        g.translate(clockStartX, clockStartY);
        if (DateEditorSkin.IMAGE_CLOCK_BG != null) {
            g.drawImage(DateEditorSkin.IMAGE_CLOCK_BG, 0, 0,
                        Graphics.LEFT | Graphics.TOP);
            paintTime(g);
        }
        g.translate(-clockStartX, -clockStartY);
    }
    
    /**
     * Paint the clock.
     *
     * @param g The Graphics to paint to
     */
    protected void paintTime(Graphics g) {
        int hour   = editDate.get(Calendar.HOUR) % 12;
        int minute = editDate.get(Calendar.MINUTE);
        
        int minuteAngle = 90 - (minute * 6);
        int hourAngle   = 90 - (hour * 30 + (minute / 2));
        
        int anchorX = DateEditorSkin.IMAGE_CLOCK_BG.getWidth() / 2;
        int anchorY = DateEditorSkin.IMAGE_CLOCK_BG.getHeight() / 2;
        g.translate(anchorX, anchorY);
        
        g.setColor(DateEditorSkin.COLOR_CLOCKHAND_DK);
        int x = (cos(hourAngle)*anchorX / 2) >> 16;
        int y = -(sin(hourAngle)*anchorX / 2) >> 16;
        g.drawLine(0, 0, x, y);
        g.drawLine(0, 1, x, y + 1);
        g.setColor(DateEditorSkin.COLOR_CLOCKHAND_LT);
        g.drawLine(0, 2, x, y + 2);
        
        g.setColor(DateEditorSkin.COLOR_CLOCKHAND_DK);
        x = (cos(minuteAngle)*(anchorX - 10)) >> 16;
        y = -(sin(minuteAngle)*(anchorX - 10)) >> 16;
        g.drawLine(0, 0, x, y);
        g.drawLine(0, 1, x, y + 1);
        g.setColor(DateEditorSkin.COLOR_CLOCKHAND_LT);
        g.drawLine(0, 2, x, y + 2);
        
        g.translate(-anchorX, -anchorY);
    }

    /**
     * Called when select key is fired, to take further action on it,
     * based on where the focus is on the date editor.
     *
     * @return true if key was handled, false otherwise
     */
    protected boolean selectFired() {
        boolean done = false;
        ScreenLFImpl sLF = (ScreenLFImpl)lf.df.owner.getLF();
        switch (focusOn) {
            case MONTH_POPUP:
                if (!monthPopup.open) {
                    sLF.lGetCurrentDisplay().showPopup(monthPopup);
                    monthPopup.open = !monthPopup.open;
                    done = true;
                } else {
                    monthPopup.open = !monthPopup.open;
                    int month = monthPopup.getSelectedIndex();
                    lastDay = daysInMonth(month, editDate.get(Calendar.YEAR));
                    if (selectedDate > lastDay) {
                        selectedDate = lastDay;
                        editDate.set(Calendar.DATE, 
                        selectedDate);
                    }
                    editDate.set(Calendar.MONTH, month);
                    sLF.lGetCurrentDisplay().hidePopup(monthPopup);
                }
                break;
            case YEAR_POPUP:
                if (!yearPopup.open) {
                    sLF.lGetCurrentDisplay().showPopup(yearPopup);
                    yearPopup.open = !yearPopup.open;
                    done = true;
                } else {
                    int selectedIndex = yearPopup.getSelectedIndex();
                    if (selectedIndex == 0) {
                        createYearStrings(Integer.parseInt(YEARS[1]) - 19);
                        yearPopup.setContent(YEARS, 20);
                        yearPopup.requestRepaint();
                        
                    } else if (selectedIndex == 21) {
                        createYearStrings(Integer.parseInt(YEARS[20]));
                        yearPopup.setContent(YEARS, 1);
                        yearPopup.requestRepaint();

                    } else {
                        yearPopup.open = !yearPopup.open;
                        int year = Integer.parseInt(YEARS[selectedIndex]);
                        lastDay = daysInMonth(
                                     editDate.get(Calendar.MONTH), year);
                        if (selectedDate > lastDay) {
                            selectedDate = lastDay;
                            editDate.set(Calendar.DATE, selectedDate);
                        }

                        editDate.set(Calendar.YEAR, year);
                        sLF.lGetCurrentDisplay().hidePopup(yearPopup);
                    }
                }
                break;
            case HOURS_POPUP:
                if (!hoursPopup.open) {
                    sLF.lGetCurrentDisplay().showPopup(hoursPopup);
                    hoursPopup.open = !hoursPopup.open;
                    done = true;
                } else {
                    hoursPopup.open = !hoursPopup.open;
                    int hour = HOURS[hoursPopup.getSelectedIndex()];
                    if ((lf.CLOCK_USES_AM_PM) && (!amSelected)) {
                        hour += 12;
                    }
                    editDate.set(Calendar.HOUR_OF_DAY, hour);
                    sLF.lGetCurrentDisplay().hidePopup(hoursPopup);
                }
                break;
            case MINUTES_POPUP:
                if (!minutesPopup.open) {
                    sLF.lGetCurrentDisplay().showPopup(minutesPopup);
                    minutesPopup.open = !minutesPopup.open;
                    done = true;
                } else {
                    minutesPopup.open = !minutesPopup.open;
                    editDate.set(Calendar.MINUTE, 
                                 MINUTES[minutesPopup.getSelectedIndex()]);
                    sLF.lGetCurrentDisplay().hidePopup(minutesPopup);
                }
                break;
            case CALENDAR:
                selectedDate = hilightedDate;
                editDate.set(Calendar.DATE, selectedDate);
                focusOn = MONTH_POPUP;
                done = true;
                break;
            case AM_PM:
                amSelected = amHilighted;
                int hour = hoursPopup.getSelectedIndex() + 1;
                if (hour == 12) {
                    if (amSelected) {
                        hour = 0;
                    }
                } else if (!amSelected) {
                    hour += 12;
                }
                    
                editDate.set(Calendar.HOUR_OF_DAY, hour);
                done = true;
                break;
            default:
                lf.uCallKeyPressed(Constants.KEYCODE_SELECT);
                done = true;
                break;
        }
        return done;
    }

    /**
     * Handles internal traversal within the date editor.
     *
     * @param code the code of this key event
     * @return true always, since popup layers swallow all events
     */
    protected boolean traverseEditor(int code) {
        // handle internal traversal
        switch (focusOn) {
        case MONTH_POPUP:
            switch (code) {
            case Constants.KEYCODE_DOWN:
                focusOn = CALENDAR;
                break;
            case Constants.KEYCODE_RIGHT:
                focusOn = YEAR_POPUP;
                break;
            default:
                // no-op
                break;
            }
            break;
        case YEAR_POPUP:
           switch (code) {
            case Constants.KEYCODE_DOWN:
                focusOn = CALENDAR;
                break;
            case Constants.KEYCODE_LEFT:
                focusOn = MONTH_POPUP;
                break;
            case Constants.KEYCODE_RIGHT:
                if (mode == DateField.DATE_TIME) {
                    focusOn = HOURS_POPUP;
                }
                break;
            default:
                // no-op
                break;
            }
            break;
        case HOURS_POPUP:
            switch (code) {
            case Constants.KEYCODE_DOWN:
                focusOn = AM_PM;
                break;
            case Constants.KEYCODE_LEFT:
                if (mode == DateField.DATE_TIME) {
                    focusOn = YEAR_POPUP;
                }
                break;
            case Constants.KEYCODE_RIGHT:
                focusOn = MINUTES_POPUP;
                break;
            default:
                // no-op
                break;
            }
            break;
        case MINUTES_POPUP:
            switch (code) {
            case Constants.KEYCODE_DOWN:
                focusOn = AM_PM;
                break;
            case Constants.KEYCODE_LEFT:
                focusOn = HOURS_POPUP;
                break;
            default:
                // no-op
                break;
            }
            break;
        case CALENDAR:
            if (!traverseCalendar(code)) {
                switch (code) {
                case Constants.KEYCODE_RIGHT:
                    if (mode == DateField.DATE_TIME) {
                        focusOn = AM_PM;
                    }
                    break;
                case Constants.KEYCODE_UP:
                    focusOn = MONTH_POPUP;
                    break;
                default:
                    // no-op
                    break;
                }
            }
            break;
        case AM_PM:
            traverseAmPm(code);
            break;
        default:
            Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                           "DateEditor.traverseEditor(), focusOn=" +focusOn);
            break;
        }
        return true;
    }

    /**
     * Handle internal traversal between the am/pm indicators.
     *
     * @param code the code of this key event
     * @return true if internal traversal occurred, false otherwise
     */
    protected boolean traverseAmPm(int code) {
        boolean traverse = false;
        switch (code) {
        case Constants.KEYCODE_UP:
            focusOn = HOURS_POPUP;
            traverse = true;
            break;
        case Constants.KEYCODE_LEFT:
            if (amHilighted) {
                focusOn = CALENDAR;
                traverse = true;
            } else {
                amHilighted = true;
                traverse = true;
            }
            break;
        case Constants.KEYCODE_RIGHT:
            if (amHilighted) {
                amHilighted = false;
                traverse = true;
            }
            break;
        default:
            // no-op
            break;
        }
        return traverse;
    }

    /**
     * Handle internal traversal within the calendar.
     *
     * @param code the code of this key event
     * @return true if internal traversal occurred, false otherwise
     */
    protected boolean traverseCalendar(int code) {
        boolean traverse = false;
        
        switch (code) {
        case Constants.KEYCODE_LEFT:
            if (hilightedDate > 1) {
                hilightedDate--;
            }
            traverse = true;
            break;
        case Constants.KEYCODE_RIGHT:
            if ((hilightedDate < lastDay) && 
                (dateHilightX < calendarRightLimit)) {
                hilightedDate++;
                traverse = true;
            }
            break;
        case Constants.KEYCODE_UP:
            if (hilightedDate == 1) {
                break;
            }
            if (hilightedDate > 7) {
                hilightedDate -= 7;
                traverse = true;
            } else if (dateHilightY > calendarTopLimit) {
                hilightedDate = 1;
                traverse = true;
            }
            break;
        case Constants.KEYCODE_DOWN:
            if (hilightedDate == lastDay) {
                break;
            }
            if (hilightedDate <= (lastDay - 7)) {
                hilightedDate += 7;
                traverse = true;
            } else if (dateHilightY < calendarBottomLimit) {
                hilightedDate = lastDay;
                traverse = true;
            }
            break;
         default:
            // no-op
            break;
        }
        return traverse;
    } // traverseCalendar()


    // *************** utility methods *********** //

    /**
     * Utility method to return the cosine of an angle.
     *
     * @param angle The angle to compute the cosine of
     * @return int The cosine of the angle
     */
    protected static int cos(int angle) {
        angle += 360000;
        angle %= 360;

        if (angle >= 270) {
            return TRIG_TABLE[360 - angle];
        } else if (angle >= 180) {
            return -TRIG_TABLE[angle - 180];
        } else if (angle >= 90) {
            return -TRIG_TABLE[180 - angle];
        } else {
            return TRIG_TABLE[angle];
        }
    }

    /**
     * Utility method to return the sin of an angle.
     *
     * @param angle The angle to compute the sin of
     * @return int The sin of the angle
     */
    protected static int sin(int angle) {
        return cos(90 - angle);
    }

    /**
     * Utility method to calculate the number of days
     * in a month.
     *
     * @param month  The month to use
     * @param year  The year the month occurs in
     * @return int  The number of days in the month
     */
    protected int daysInMonth(int month, int year) {
        switch (month) {
        case Calendar.JANUARY:
        case Calendar.MARCH:
        case Calendar.MAY:
        case Calendar.JULY:
        case Calendar.AUGUST:
        case Calendar.OCTOBER:
        case Calendar.DECEMBER:
            return 31;
        case Calendar.FEBRUARY:
            if (((year % 400) == 0)
                || (((year & 3) == 0) && ((year % 100) != 0))) {
                return 29;
            }
            return 28;
        case Calendar.APRIL:
        case Calendar.JUNE:
        case Calendar.SEPTEMBER:
        case Calendar.NOVEMBER:
        default:
            return 30;
        }
    }

    /**
     * Set the day offset.
     */
    protected void setDayOffset() {
        Date save = editDate.getTime();
        editDate.set(Calendar.DATE, 1);
        dayOffset = editDate.get(Calendar.DAY_OF_WEEK);
        
        if (Resource.getFirstDayOfWeek() != Calendar.SUNDAY) {
            dayOffset = (dayOffset == 1) ? 7 : (dayOffset - 1);
        }
        editDate.setTime(save);
    }
    

    // *********** attributes ************* //

    /**
     * Table of trigonometric functions, in 16.16 fixed point.
     */
    protected static final int TRIG_TABLE[] = {
        65535, // cos 0
        65525, // cos 1
        65495, // cos 2
        65445, // cos 3
        65375, // cos 4
        65285, // cos 5
        65175, // cos 6
        65046, // cos 7
        64897, // cos 8
        64728, // cos 9
        64539, // cos 10
        64330, // cos 11
        64102, // cos 12
        63855, // cos 13
        63588, // cos 14
        63301, // cos 15
        62996, // cos 16
        62671, // cos 17
        62327, // cos 18
        61964, // cos 19
        61582, // cos 20
        61182, // cos 21
        60762, // cos 22
        60325, // cos 23
        59869, // cos 24
        59394, // cos 25
        58902, // cos 26
        58392, // cos 27
        57863, // cos 28
        57318, // cos 29
        56754, // cos 30
        56174, // cos 31
        55576, // cos 32
        54962, // cos 33
        54330, // cos 34
        53683, // cos 35
        53018, // cos 36
        52338, // cos 37
        51642, // cos 38
        50930, // cos 39
        50202, // cos 40
        49459, // cos 41
        48701, // cos 42
        47929, // cos 43
        47141, // cos 44
        46340, // cos 45
        45524, // cos 46
        44694, // cos 47
        43851, // cos 48
        42994, // cos 49
        42125, // cos 50
        41242, // cos 51
        40347, // cos 52
        39439, // cos 53
        38520, // cos 54
        37589, // cos 55
        36646, // cos 56
        35692, // cos 57
        34728, // cos 58
        33753, // cos 59
        32767, // cos 60
        31771, // cos 61
        30766, // cos 62
        29752, // cos 63
        28728, // cos 64
        27696, // cos 65
        26655, // cos 66
        25606, // cos 67
        24549, // cos 68
        23485, // cos 69
        22414, // cos 70
        21336, // cos 71
        20251, // cos 72
        19160, // cos 73
        18063, // cos 74
        16961, // cos 75
        15854, // cos 76
        14742, // cos 77
        13625, // cos 78
        12504, // cos 79
        11380, // cos 80
        10251, // cos 81
        9120,  // cos 82
        7986,  // cos 83
        6850,  // cos 84
        5711,  // cos 85
        4571,  // cos 86
        3429,  // cos 87
        2287,  // cos 88
        1143,  // cos 89
        0      // cos 90
    };

    /**
     * Constant indicating the month popup, used in the process of current
     * focus tracking inside the date editor.
     */
    protected static final int MONTH_POPUP = 1;
    
    /**
     * Constant indicating the year popup, used in the process of current
     * focus tracking inside the date editor.
     */
    protected static final int YEAR_POPUP = 2;

    /**
     * Constant indicating the hour popup, used in the process of current
     * focus tracking inside the date editor.
     */
    protected static final int HOURS_POPUP = 3;

    /**
     * Constant indicating the minutes popup, used in the process of current
     * focus tracking inside the date editor.
     */
    protected static final int MINUTES_POPUP = 4;
    
    /**
     * Constant indicating the calendar, used in the process of current
     * focus tracking inside the date editor.
     */
    protected static final int CALENDAR = 5;

    /**
     * Constant indicating the am/pm indicators, used in the process of 
     * current focus tracking inside the date editor.
     */
    protected static final int AM_PM = 6;

    /**
     * Static array holding the localized equivalent of month names.
     */
    protected static String[] MONTHS;

    /**
     * Static array holding the year values.
     */
    protected static String[] YEARS;
    
    /**
     * Static array holding the hour values.
     */
    protected static int[] HOURS;
    
    /**
     * Static array holding the minute values.
     */
    protected static int[] MINUTES;

    /**
     * The DateFieldLFImpl that triggered this date editor.
     */
    protected DateFieldLFImpl lf;
    
    /**
     * The date currently being edited.
     */
    protected Calendar editDate;

    /**
     * The mode of the date field, that triggered this date editor.
     */
    protected int mode;

    /**
     * Whether date field that triggered this date editor was initialized 
     * or not.
     */
    protected boolean initialized = false;

    /**
     * Special command to cancel any changes and close the date editor
     * without any impact on the datefield that triggered this editor.
     */
    protected Command cancel = 
        new Command(Resource.getString(ResourceConstants.CANCEL), 
                    Command.CANCEL, 0);
    
    /**
     * Special command to set/save the changes done in the editor into the 
     * datefield that triggered this editor and close the editor.
     */
    protected Command set = 
        new Command(Resource.getString(ResourceConstants.SET), 
                    Command.OK, 1);
    
    /**
     * The command array that holds both the commands associated with
     * the date editor.
     */
    protected Command[] commands = {set, cancel};

    /**
     * The location x-coordinate, used to calculate where to draw 
     * the next component.
     */
    protected int nextX = 0;
    
    /**
     * The location y-coordinate, used to calculate where to draw 
     * the next component.
     */
    protected int nextY = 0;
    
    /** The last day of the month. */
    protected int lastDay;
    
    /** The day offset. */
    protected int dayOffset;

    /** The state of the date editor popup (true=open/false=closed). */
    boolean popUpOpen;

    /** The sub-popup layer used to select month value. */
    protected DEPopupLayer monthPopup;

    /** The sub-popup layer used to select year value. */
    protected DEPopupLayer yearPopup;

    /** The sub-popup layer used to select hour value. */
    protected DEPopupLayer hoursPopup;

    /** The sub-popup layer used to select minutes value. */
    protected DEPopupLayer minutesPopup;

    /** Keeps track of the currently focused item inside the date editor. */
    protected int focusOn;

    /**
     * Indicates whether am or pm is currently selected. True indicates "am"
     * is selected and false indicates "pm" is selected.
     */
    protected boolean amSelected = false;

    /**
     * Indicates whether am or pm is currently highlighted. 
     * True indicates "am" is highlighted and false indicates "pm" is 
     * highlighted.
     */
    protected boolean amHilighted = false;

    /** Currently highlighted date in the calendar. */
    protected int hilightedDate = 1;

    /** Currently selected date in the calendar. */
    protected int selectedDate = 1;
    
    /** Width of a sub-popup in its closed state. */
    protected int popupWidth;

    /** Height of a sub-popup in its closed state. */
    protected int popupHeight;

    /** Width of the element within the popup in its closed state. */
    protected int elementWidth;

    /** Height of the element within the popup in its closed state. */
    protected int elementHeight;

    /** 
     * The location offset to draw time components used for DateField.TIME 
     * and DateField.DATE_TIME modes. 
     */
    protected int timeComponentsOffset;

    /** Indicates calendar's top limit, used in traversal calculations. */
    protected int calendarTopLimit;

    /** Indicates calendar's bottom limit, used in traversal calculations. */
    protected int calendarBottomLimit;

    /** Indicates calendar's right limit, used in traversal calculations. */
    protected int calendarRightLimit;

    /**
     * Indicates x co-ordinate of previously highlighted date, used in 
     * traversal calculations.
     */
    protected int dateHilightX;

    /**
     * Indicates y co-ordinate of previously highlighted date, used in 
     * traversal calculations.
     */
    protected int dateHilightY;
}
