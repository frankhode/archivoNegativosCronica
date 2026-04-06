/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.archivonegativoscronica;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Keyboard {

    /**
     * @return the robot
     */
    public Robot getRobot() {
        return robot;
    }

    private final Robot robot;
    private boolean isCaptured = false;

    public Keyboard() throws AWTException {
        this.robot = new Robot();
    }

    public Keyboard(Robot robot) {
        this.robot = robot;
        
    }

    public void type(CharSequence characters) {
        if (characters == null) {
            characters = "" ;
        }
        int length = characters.length();
        for (int i = 0; i < length; i++) {
            char character = characters.charAt(i);
            type(character);
        }
    }

    public void type(char character) {
        switch (character) {
        case 'a': doType(VK_A); break;
        case 'b': doType(VK_B); break;
        case 'c': doType(VK_C); break;
        case 'd': doType(VK_D); break;
        case 'e': doType(VK_E); break;
        case 'f': doType(VK_F); break;
        case 'g': doType(VK_G); break;
        case 'h': doType(VK_H); break;
        case 'i': doType(VK_I); break;
        case 'j': doType(VK_J); break;
        case 'k': doType(VK_K); break;
        case 'l': doType(VK_L); break;
        case 'm': doType(VK_M); break;
        case 'n': doType(VK_N); break;
        case 'o': doType(VK_O); break;
        case 'p': doType(VK_P); break;
        case 'q': doType(VK_Q); break;
        case 'r': doType(VK_R); break;
        case 's': doType(VK_S); break;
        case 't': doType(VK_T); break;
        case 'u': doType(VK_U); break;
        case 'v': doType(VK_V); break;
        case 'w': doType(VK_W); break;
        case 'x': doType(VK_X); break;
        case 'y': doType(VK_Y); break;
        case 'z': doType(VK_Z); break;
        case 'A': doType(VK_SHIFT, VK_A); break;
        case 'B': doType(VK_SHIFT, VK_B); break;
        case 'C': doType(VK_SHIFT, VK_C); break;
        case 'D': doType(VK_SHIFT, VK_D); break;
        case 'E': doType(VK_SHIFT, VK_E); break;
        case 'F': doType(VK_SHIFT, VK_F); break;
        case 'G': doType(VK_SHIFT, VK_G); break;
        case 'H': doType(VK_SHIFT, VK_H); break;
        case 'I': doType(VK_SHIFT, VK_I); break;
        case 'J': doType(VK_SHIFT, VK_J); break;
        case 'K': doType(VK_SHIFT, VK_K); break;
        case 'L': doType(VK_SHIFT, VK_L); break;
        case 'M': doType(VK_SHIFT, VK_M); break;
        case 'N': doType(VK_SHIFT, VK_N); break;
        case 'O': doType(VK_SHIFT, VK_O); break;
        case 'P': doType(VK_SHIFT, VK_P); break;
        case 'Q': doType(VK_SHIFT, VK_Q); break;
        case 'R': doType(VK_SHIFT, VK_R); break;
        case 'S': doType(VK_SHIFT, VK_S); break;
        case 'T': doType(VK_SHIFT, VK_T); break;
        case 'U': doType(VK_SHIFT, VK_U); break;
        case 'V': doType(VK_SHIFT, VK_V); break;
        case 'W': doType(VK_SHIFT, VK_W); break;
        case 'X': doType(VK_SHIFT, VK_X); break;
        case 'Y': doType(VK_SHIFT, VK_Y); break;
        case 'Z': doType(VK_SHIFT, VK_Z); break;
        case '`': doType(VK_BACK_QUOTE); break;
        case '0': doType(VK_0); break;
        case '1': doType(VK_1); break;
        case '2': doType(VK_2); break;
        case '3': doType(VK_3); break;
        case '4': doType(VK_4); break;
        case '5': doType(VK_5); break;
        case '6': doType(VK_6); break;
        case '7': doType(VK_7); break;
        case '8': doType(VK_8); break;
        case '9': doType(VK_9); break;
        case '-': doType(VK_MINUS); break;
        case '=': doType(VK_EQUALS); break;
        case '~': doType(VK_SHIFT, VK_BACK_QUOTE); break;
        case '!': doType(VK_EXCLAMATION_MARK); break;
        case '@': doType(VK_AT); break;
        case '#': doType(VK_NUMBER_SIGN); break;
        case '$': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD3,VK_NUMPAD6); break;
        case '%': doType(VK_SHIFT, VK_5); break;
        case '^': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD9,VK_NUMPAD4); break;
        case '&': doType(VK_AMPERSAND); break;
        case '*': doType(VK_F6); break;
        case '(': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD4,VK_NUMPAD0); break;
        case ')': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD4,VK_NUMPAD1); break;  
        case '_': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD9,VK_NUMPAD5); break;
        case '+': doType(VK_PLUS); break;
        case '\t': doType(VK_TAB); break;
        case '\n': doType(VK_ENTER); break;
        case '[': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD9,VK_NUMPAD1); break;
        case ']': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD9,VK_NUMPAD3); break;        
        case 'º': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD7); break;        
        case '\\': doType(VK_BACK_SLASH); break;
        case '{': doType(VK_SHIFT, VK_OPEN_BRACKET); break;
        case '}': doType(VK_SHIFT, VK_CLOSE_BRACKET); break;
        case '|': doType(VK_SHIFT, VK_BACK_SLASH); break;
        case ';': doType(VK_SEMICOLON); break;
        case ':': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD5,VK_NUMPAD8); break;
        case '\'': doType(VK_QUOTE); break;
        case '"': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD3,VK_NUMPAD4); break;
        case ',': doType(VK_COMMA); break;
        case '<': doType(VK_SHIFT, VK_COMMA); break;
        case '.': doType(VK_PERIOD); break;
        case '>': doType(VK_SHIFT, VK_PERIOD); break;
        case '/': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD4,VK_NUMPAD7); break;
        case '?': doType(VK_ALT,VK_NUMPAD0,VK_NUMPAD6,VK_NUMPAD3); break;
        case ' ': doType(VK_SPACE); break;
        case 'á': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD0); break;
        case 'é': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD0); break;
        case 'í': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD1); break;
        case 'ó': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD2); break;
        case 'ú': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD3); break;
        case 'Ç': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD2,VK_NUMPAD8); break;
        case 'ü': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD2,VK_NUMPAD9); break;
        case 'â': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD1); break;
        case 'ä': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD2); break;
        case 'à': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD3); break;
        case 'å': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD4); break;
        case 'ç': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD5); break;
        case 'ê': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD6); break;
        case 'ë': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD7); break;
        case 'è': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD8); break;
        case 'ï': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD3,VK_NUMPAD9); break;
        case 'î': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD0); break;
        case 'ì': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD1); break;
        case 'Ä': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD2); break;
        case 'Å': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD3); break;
        case 'É': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD4); break;
        case 'æ': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD5); break;
        case 'Æ': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD6); break;
        case 'ô': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD7); break;
        case 'ö': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD8); break;
        case 'ò': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD4,VK_NUMPAD9); break;
        case 'û': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD0); break;
        case 'ù': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD1); break;
        case 'ÿ': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD2); break;
        case 'Ö': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD3); break;
        case 'Ü': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD4); break;
        case 'ø': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD5); break;
        case '£': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD6); break;
        case 'Ø': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD7); break;
        case '×': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD8); break;
        case 'ƒ': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD5,VK_NUMPAD9); break;
        case 'ñ': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD4); break;
        case 'Ñ': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD5); break;
        case 'ª': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD6); break;
        case '¿': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD8); break;
        case '®': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD6,VK_NUMPAD9); break;
        case '¬': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD0); break;
        case '½': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD1); break;
        case '¼': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD2); break;
        case '¡': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD3); break;
        case '«': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD4); break;
        case '»': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD5); break;
        case '░': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD6); break;
        case '▒': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD7); break;
        case '▓': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD8); break;
        case '│': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD7,VK_NUMPAD9); break;
        case '┤': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD0); break;
        case 'Á': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD1); break;
        case 'Â': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD2); break;
        case 'À': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD3); break;
        case '©': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD4); break;
        case '╣': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD5); break;
        case '║': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD6); break;
        case '╗': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD7); break;
        case '╝': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD8); break;
        case '¢': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD8,VK_NUMPAD9); break;
        case '¥': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD0); break;
        case '┐': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD1); break;
        case '└': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD2); break;
        case '┴': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD3); break;
        case '┬': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD4); break;
        case '├': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD5); break;
        case '─': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD6); break;
        case '┼': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD7); break;
        case 'ã': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD8); break;
        case 'Ã': doType(VK_ALT,VK_NUMPAD1,VK_NUMPAD9,VK_NUMPAD9); break;
        case '╚': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD0); break;
        case '╔': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD1); break;
        case '╩': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD2); break;
        case '╦': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD3); break;
        case '╠': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD4); break;
        case '═': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD5); break;
        case '╬': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD6); break;
        case '¤': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD7); break;
        case 'ð': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD8); break;
        case 'Ð': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD0,VK_NUMPAD9); break;
        case 'Ê': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD0); break;
        case 'Ë': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD1); break;
        case 'È': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD2); break;
        case 'ı': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD3); break;
        case 'Í': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD4); break;
        case 'Î': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD5); break;
        case 'Ï': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD6); break;
        case '┘': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD7); break;
        case '┌': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD8); break;
        case '█': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD1,VK_NUMPAD9); break;
        case '▄': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD0); break;
        case '¦': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD1); break;
        case 'Ì': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD2); break;
        case '▀': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD3); break;
        case 'Ó': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD4); break;
        case 'ß': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD5); break;
        case 'Ô': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD6); break;
        case 'Ò': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD7); break;
        case 'õ': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD8); break;
        case 'Õ': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD2,VK_NUMPAD9); break;
        case 'µ': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD0); break;
        case 'þ': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD1); break;
        case 'Þ': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD2); break;
        case 'Ú': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD3); break;
        case 'Û': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD4); break;
        case 'Ù': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD5); break;
        case 'ý': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD6); break;
        case 'Ý': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD7); break;
        case '¯': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD8); break;
        case '´': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD3,VK_NUMPAD9); break;
        case '≡': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD0); break;
        case '±': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD1); break;
        case '‗': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD2); break;
        case '¾': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD3); break;
        case '¶': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD4); break;
        case '§': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD5); break;
        case '÷': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD6); break;
        case '¸': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD7); break;
        case '°': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD8); break;
        case '¨': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD4,VK_NUMPAD9); break;
        case '·': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD5,VK_NUMPAD0); break;
        case '¹': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD5,VK_NUMPAD1); break;
        case '³': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD5,VK_NUMPAD2); break;
        case '²': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD5,VK_NUMPAD3); break;
        case '■': doType(VK_ALT,VK_NUMPAD2,VK_NUMPAD5,VK_NUMPAD4); break;
        case '…': doType(VK_PERIOD,VK_PERIOD,VK_PERIOD); break;        
        default:
            //throw new IllegalArgumentException("Cannot type character ->'" + character+"'<-");
            System.out.println("Cannot type character ->'" + character+"'<-");
        }
    }

    private void doType(int... keyCodes) {
        doType(keyCodes, 0, keyCodes.length);
    }

    private void doType(int[] keyCodes, int offset, int length) {
        if (length == 0) {
            return;
        }

        getRobot().keyPress(keyCodes[offset]);
        doType(keyCodes, offset + 1, length - 1);
        getRobot().keyRelease(keyCodes[offset]);
    }

    public void directType(int... tecla) {
        for (int i : tecla) {
            getRobot().keyPress(i);
        }
        for (int i : tecla) {
            getRobot().keyRelease(i);
        }
    }
    
    public void guardarRegistro() {
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_L) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_ENTER) ;
        getRobot().delay(500);
    }

    public void abrePlantilla() {
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_A) ;
        getRobot().delay(50);
        directType(KeyEvent.VK_Z) ;
        getRobot().delay(50);
        directType(KeyEvent.VK_ENTER) ;
        getRobot().delay(50);
    }
    
    public void expandePlantilla(String plantilla) {
        switch(plantilla){
            case "ind" :
                directType(KeyEvent.VK_CONTROL,KeyEvent.VK_E) ;
                getRobot().delay(50);
                directType(KeyEvent.VK_Z) ;
                getRobot().delay(50);
                directType(KeyEvent.VK_UP) ;
                getRobot().delay(50);
                directType(KeyEvent.VK_UP) ;
                getRobot().delay(50);
                directType(KeyEvent.VK_ENTER) ;
                getRobot().delay(50);
                break;
        }
        
    }
    
    public void carga008(String fecha) {
        if (fecha.isEmpty()) {
            directType(KeyEvent.VK_DOWN) ;
        } else {
            getRobot().delay(500);
            directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F) ;
            String anio = fecha.substring(0,4) ;
            getRobot().delay(500);
            directType(KeyEvent.VK_TAB) ;
            getRobot().delay(500);
            type(anio);
            getRobot().delay(500);
            directType(KeyEvent.VK_ENTER) ;
            getRobot().delay(500);
        }        
    }
    
    public void irAlFinalDelRegistro() {
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_END) ;
        getRobot().delay(500);
    }
    
    public void irAlInicioDelRegistro() {
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_HOME) ;
        getRobot().delay(50);
    }
    
    public void cargaCampo040() {
        //$$aAR-BaBN$$bspa$$cAR-BaBN$$eaacr
        directType(KeyEvent.VK_F6) ;
        type("040");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type("AR-BaBN$$bspa$$cAR-BaBN$$eaacr");
        /*key.type("AR-BaBN");
        key.directType(KeyEvent.VK_F7) ;
        key.type("bspa");
        key.directType(KeyEvent.VK_F7) ;
        key.type("cAR-BaBN");
        key.directType(KeyEvent.VK_F7) ;
        key.type("eaacr");*/
    }
    
    public void cargaCampo043() {
        directType(KeyEvent.VK_F6) ;
        type("043");
    }
    
    public void cargaTitulo(String titulo, String fechaISO) {
        directType(KeyEvent.VK_F6) ;
        type("24500");
        directType(KeyEvent.VK_RIGHT) ;
        type(titulo);
        String fechaFormateada = RegistrosParaAleph.fechaFormateada(fechaISO);
        if (!"".equals(fechaFormateada)) {
            type(", ") ;
            type(fechaFormateada) ;            
        }
        directType(KeyEvent.VK_F7) ;
        type("h[material gráfico].") ;        
    }
    
    public void cargaCampo260(String fechaISO) {
        directType(KeyEvent.VK_F6) ;
        type("260");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type("c");
        try {
            String anio = fechaISO.substring(0,4);
            type(anio);
        } catch (Exception e) {
            //type("[197-?]") ;
            type("[198-?]") ;
        }         
    }
    
    public void cargaCampo300Ind(boolean col) {
        directType(KeyEvent.VK_F6) ;
        type("300");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        if (!col) {
            type("1 sobre (negativos flexibles) :");
            directType(KeyEvent.VK_F7) ;
            type("bbyn");
        } else {
            type("1 sobre (diapositivas) :");
            directType(KeyEvent.VK_F7) ;
            type("bcol.");
        }
    }

    public void cargaCampo500IndTit() {
        directType(KeyEvent.VK_F6) ;
        type("500");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type("Título tomado del sobre.");
    }

    public void cargaCampo500IndAut(String autor) {
        if (!autor.equals("")) {
            directType(KeyEvent.VK_F6) ;
            type("500");
            directType(KeyEvent.VK_RIGHT) ;
            directType(KeyEvent.VK_RIGHT) ;
            directType(KeyEvent.VK_RIGHT) ;
            type("Fotógrafo: ");        
            type(autor);        
            type(".");        
        }
    }

    public void cargaCampo540() {
        directType(KeyEvent.VK_F6) ;
        type("540");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type("Puede presentar restricciones. "
                + "Consultar en el Departamento de Materiales "
                + "Cartográficos y Fotográficos.");
        directType(KeyEvent.VK_F7) ;
        type("5AR-BaBN");
    }

    public void cargaCampo561() {
        directType(KeyEvent.VK_F6) ;
        type("561");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type("Forma parte del archivo fotográfico del diario Crónica.");
        directType(KeyEvent.VK_F7) ;
        type("5AR-BaBN");
    }

    public void cargaCampos6XX() {
        //60014 L $$a $$d $$x $$v
        directType(KeyEvent.VK_F6) ;
        type("60014");
        directType(KeyEvent.VK_F7) ;
        type("d");
        directType(KeyEvent.VK_F7) ;
        type("x");
        directType(KeyEvent.VK_F7) ;
        type("v");
        //61024 L $$a $$v
        directType(KeyEvent.VK_F6) ;
        type("61024");
        directType(KeyEvent.VK_F7) ;
        type("v");
        //61124 L $$a $$n $$d $$c $$v
        directType(KeyEvent.VK_F6) ;
        type("61124");
        directType(KeyEvent.VK_F7) ;
        type("n");
        directType(KeyEvent.VK_F7) ;
        type("d");
        directType(KeyEvent.VK_F7) ;
        type("c");
        directType(KeyEvent.VK_F7) ;
        type("v");
        //63004 L $$a $$v
        directType(KeyEvent.VK_F6) ;
        type("63004");
        directType(KeyEvent.VK_F7) ;
        type("v");
        //650 4 L $$a $$x $$y $$v
        directType(KeyEvent.VK_F6) ;
        type("650 4");
        directType(KeyEvent.VK_F7) ;
        type("x");
        directType(KeyEvent.VK_F7) ;
        type("y");
        directType(KeyEvent.VK_F7) ;
        type("v");        
        //651 4 L $$a $$x $$y $$v
        directType(KeyEvent.VK_F6) ;
        type("651 4");
        directType(KeyEvent.VK_F7) ;
        type("x");
        directType(KeyEvent.VK_F7) ;
        type("y");
        directType(KeyEvent.VK_F7) ;
        type("v"); 
    }

    public void cargaCampo655(boolean col) {        
        directType(KeyEvent.VK_F6) ;
        type("655 4");
        directType(KeyEvent.VK_RIGHT) ;
        if (col) {
            type("Fotografía en color");
        } else {
            type("Negativos flexibles");
        }
    }
    
    public void reemplazaCampo655(boolean col) {
        irAlFinalDelRegistro();
        directType(KeyEvent.VK_UP) ;
        directType(KeyEvent.VK_UP) ;
        directType(KeyEvent.VK_UP) ;
        directType(KeyEvent.VK_HOME) ;
        for (int i = 0; i < 50; i++) {
            directType(KeyEvent.VK_DELETE) ;
        }
        directType(KeyEvent.VK_RIGHT) ;
        if (col) {
            type("Fotografía en color");
        } else {
            type("Negativos flexibles");
        }
    }
    
    void reemplazaCampo300(boolean col) {
        irAlFinalDelRegistro();
        for (int i = 0; i < 25; i++) {
            directType(KeyEvent.VK_UP) ;
        }
        getRobot().delay(500);
        borraTodo() ; 
        getRobot().delay(500);        
        if (col) {
            type("1 sobre (diapositivas) :");
            getRobot().delay(500);
            directType(KeyEvent.VK_DOWN) ;
            borraTodo();
            getRobot().delay(500);
            type("col.");
        } else {
            type("1 sobre (negativos flexibles) :");
            getRobot().delay(500);
            directType(KeyEvent.VK_DOWN) ;
            borraTodo();
            getRobot().delay(500);
            type("byn.");            
        }
    }

    public void cargaCampo773() {
        //tSección Archivo fotográfico $$w(AR-BaBN)001412736
        directType(KeyEvent.VK_F6) ;
        type("77318");
        type("tSección Archivo fotográfico");        
        directType(KeyEvent.VK_F7) ;
        type("w(AR-BaBN)001412736");        
    }

    public void cargaOWN() {
        directType(KeyEvent.VK_F6) ;
        type("OWN  ");
        directType(KeyEvent.VK_RIGHT) ;
        type("CAT_FOTO");        
    }
    
    public void cargaItem(String[] reg, boolean primerItem) {
        String barcode = reg[0] ;
        String nroA ;
        if (reg[1].equals("")) {
            nroA = "["+barcode+"]" ;
        } else {
            nroA = reg[1] ;
        }
        String ufi = reg[8] ;
        if (primerItem) {
            getRobot().mouseMove(30, 368);
            getRobot().delay(500);
            getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
            getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            getRobot().delay(50);
            getRobot().mouseMove(90, 384);
            getRobot().delay(500);
            getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
            getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            getRobot().delay(50);
            getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
            getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            getRobot().delay(500);
        }
        //nuevo
        getRobot().mouseMove(1321, 169);
        getRobot().delay(500);
        getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().delay(500);
        //barcode
        type(barcode);
        getRobot().delay(500);
        //ufi
        for (int i = 0; i < 9; i++) {
            directType(KeyEvent.VK_TAB) ;            
        }
        getRobot().delay(50);
        type(ufi);
        //nro A
        for (int i = 0; i < 3; i++) {
            directType(KeyEvent.VK_TAB) ;            
        }
        getRobot().delay(50);
        type(nroA);
        //pestaña 2
        getRobot().mouseMove(530, 437);
        getRobot().delay(500);
        getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        //inventario
        type(barcode);
        getRobot().delay(500);
        directType(KeyEvent.VK_ENTER) ;        
    }

    public void carga008Grupal(List<String> fechas) {
        int menorAnio = Integer.MAX_VALUE;
        int mayorAnio = Integer.MIN_VALUE;
        boolean todosIguales = true;
        
        if (fechas.isEmpty()) {
            directType(KeyEvent.VK_DOWN) ;
        } else if (fechas.size() == 1) {            
            cargaFecha1(fechas.get(0)) ;
        } else {
            for (String anioStr : fechas) {
                try {
                    int anio = Integer.parseInt(anioStr);
                    if (anio < menorAnio) {
                        menorAnio = anio;
                    }
                    if (anio > mayorAnio) {
                        mayorAnio = anio;
                    }
                    if (!anioStr.equals(fechas.get(0))) {
                        todosIguales = false;
                    }
                } catch (Exception e) {
                }
                
            }
            if (todosIguales) {                
                cargaFecha1(fechas.get(0));
            } else {
                cargaFechaRango(menorAnio,mayorAnio) ;                
            }
        }        
    }
    
    public void cargaFecha1(String fecha1) {
        getRobot().delay(500);
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        type(fecha1);
        getRobot().delay(500);
        directType(KeyEvent.VK_ENTER) ;
        getRobot().delay(500);
    }
    
    public void cargaFechaRango(int menorAnio, int mayorAnio) {
        getRobot().delay(500);
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F) ;
        getRobot().delay(500);
        type("k");
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        type(Integer.toString(menorAnio));
        getRobot().delay(500);
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        type(Integer.toString(mayorAnio));
        getRobot().delay(500);
        directType(KeyEvent.VK_ENTER) ;
        getRobot().delay(500);
    }

    public void cargaTituloGrupal(RegistroGrupal reg) {
        directType(KeyEvent.VK_F6) ;
        type("24500");
        directType(KeyEvent.VK_RIGHT) ;
        type("[");
        type(reg.getTitulo());
        type("]");
        directType(KeyEvent.VK_F7) ;
        type("h[material gráfico].") ;  
    }

    public void cargaCampo260Grupal(List<String> fechas) {
        String fecha ;
        int menorAnio = Integer.MAX_VALUE;
        int mayorAnio = Integer.MIN_VALUE;
        boolean todosIguales = true;
        
        if (fechas.isEmpty()) {
            fecha = "[197-?]";
        } else if (fechas.size() == 1) {
            fecha = fechas.get(0).substring(0,4);
        } else {
            for (String anioStr : fechas) {
                try {
                    int anio = Integer.parseInt(anioStr.substring(0,4));
                    if (anio < menorAnio) {
                        menorAnio = anio;
                    }
                    if (anio > mayorAnio) {
                        mayorAnio = anio;
                    }
                    if (!anioStr.equals(fechas.get(0).substring(0,4))) {
                        todosIguales = false;
                    }
                } catch (Exception e) {
                }                
            }
            if (todosIguales) {
                fecha = fechas.get(0).substring(0,4);
            } else {
                fecha = menorAnio + "-" + mayorAnio ;
            }
        }
        directType(KeyEvent.VK_F6) ;
        type("260");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type("c");
        type(fecha);
        //type("]");
    }

    public void cargaCampo300Grupal(int size, boolean col) {
        directType(KeyEvent.VK_F6) ;
        type("300");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type(Integer.toString(size));
        if (!col) {
            type(" sobres (negativos flexibles) :");
            directType(KeyEvent.VK_F7) ;
            type("bbyn");
        } else {
            type(" sobres (diapositivas) :");
            directType(KeyEvent.VK_F7) ;
            type("bcol.");
        }        
    }

    public void cargaCampo500GrupTit() {
        directType(KeyEvent.VK_F6) ;
        type("500");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type("Título asignado por el personal de la Biblioteca.");
    }

    public void cargaCampo500GrupAut(List<String> autores) {
        Collections.sort(autores) ;
        if (!autores.isEmpty()) {
            directType(KeyEvent.VK_F6) ;
            type("500");
            directType(KeyEvent.VK_RIGHT) ;
            directType(KeyEvent.VK_RIGHT) ;
            directType(KeyEvent.VK_RIGHT) ;
            type("Fotógrafo: ");
            autores.forEach((fotografo) -> {
                type(fotografo);
                int indice = autores.indexOf(fotografo);
                if (indice == autores.size() - 1) {
                    //no hace nada
                } else {
                    type(", ");        
                }                
            });
            type(".");
        }
    }

    public void cargaTitulos505(List<String> titulos) {        
        StringBuilder temp = new StringBuilder() ;        
        try {
            Collections.sort(titulos, new MyComparator()) ;
        } catch (Exception e) {
            System.out.println(e);
        }

        if (titulos.size() > 10) {
            directType(KeyEvent.VK_F6) ;
            type("5050");
            directType(KeyEvent.VK_RIGHT) ;
            directType(KeyEvent.VK_RIGHT) ;
            for (int i = 0; i < titulos.size(); i++) {
                System.out.println(i+". "+titulos.get(i));
                temp.append(titulos.get(i)) ;                
                if (i == titulos.size() - 1) {
                    //no hace nada
                } else {
                    temp.append(" -- ") ;
                }
                if (temp.toString().length() >= 1300) {
                    pegaTexto(temp.toString()) ;                    
                    //getRobot().delay(2000);
                    //type(temp.toString());
                    temp = new StringBuilder() ;                    
                    directType(KeyEvent.VK_F6) ;
                    type("5050");
                    directType(KeyEvent.VK_RIGHT) ;
                    directType(KeyEvent.VK_RIGHT) ;                    
                }
            }
            //carga los que faltan
            pegaTexto(temp.toString());
            //key.type(temp.toString());
            type(".");                    
        } else {
            directType(KeyEvent.VK_F6) ;
            type("5050");
            directType(KeyEvent.VK_RIGHT) ;
            directType(KeyEvent.VK_RIGHT) ;
            temp = new StringBuilder() ;
            for (int i = 0; i < titulos.size(); i++) {
                temp.append(titulos.get(i));
                if (i == titulos.size() - 1) {
                    //no hace nada
                } else {
                    temp.append(" -- ") ;
                }
            }
            temp.append(".");
            pegaTexto(temp.toString());
        }
    }
    
    private void pegaTexto(String texto) {
        getRobot().delay(2000);
        StringSelection seleccion = new StringSelection(texto.replace(" -- -- "," -- "));
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(seleccion, null);
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_V) ;
        getRobot().delay(2000);
    }

    public void abreBibliografico(String sys) {
        getRobot().delay(500);
        getRobot().mouseMove(68, 54);
        getRobot().delay(500);
        getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        type(sys);
        directType(KeyEvent.VK_ENTER);
    }

    public void borraCampos() {
        getRobot().delay(500);
        irAlFinalDelRegistro();
        getRobot().delay(1000);
        for (int i = 0; i < 50; i++) {
            directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F5) ;            
        }
        getRobot().delay(1000);
    }

    public void expandePlantilla() {
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_E) ;
        getRobot().delay(50);
        directType(KeyEvent.VK_Z) ;
        getRobot().delay(50);
        directType(KeyEvent.VK_ENTER) ;
        getRobot().delay(500);
    }

    public void agregaCampo043(String campo043) {
        directType(KeyEvent.VK_F6) ;
        type("043");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type(campo043);
    }

    public void cargaTitulo245(String titulo245) {
        directType(KeyEvent.VK_F6) ;
        type("24500");
        directType(KeyEvent.VK_RIGHT) ;
        type(titulo245);        
    }

    public void agregaCampo500Tit(String titulo245) {
        directType(KeyEvent.VK_F6) ;
        type("500  ");
        directType(KeyEvent.VK_RIGHT) ;
        if (titulo245.substring(0,1).equals("[")) {            
            type("Título asignado por el personal de la Biblioteca.");
        } else {
            type("Título tomado del sobre.");
        }
    }

    public void agregaCampo6XX(String campo, List<String[]> campo6XX) {
        campo6XX.forEach((t) -> {
            getRobot().delay(1000);
            directType(KeyEvent.VK_F6) ;
            type(campo);
            type(t[0]);
            directType(KeyEvent.VK_RIGHT) ;            
            type(t[1]);
            getRobot().delay(1000);
        });
    }

    public void actualiza007(String code) {
        irAlInicioDelRegistro();
        getRobot().delay(500);
        directType(KeyEvent.VK_DOWN) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_CONTROL,KeyEvent.VK_F) ;
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        type(code) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_ENTER) ;
        getRobot().delay(500);
    }

    void actualizaCampo300(int cantItems, String mixto) {
        directType(KeyEvent.VK_F6) ;
        type("300");
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        directType(KeyEvent.VK_RIGHT) ;
        type(Integer.toString(cantItems));
        type(" sobres (negativos flexibles, diapositivas) :");
        directType(KeyEvent.VK_F7) ;
        type("bbyn y col.");
    }

    void borraRegistroAleph(String sys) {
        abreBibliografico(sys);
        getRobot().delay(500);
        directType(KeyEvent.VK_F6) ;
        getRobot().delay(500);
        type("STA");
        getRobot().delay(500);
        directType(KeyEvent.VK_TAB) ;
        directType(KeyEvent.VK_TAB) ;
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_DOWN) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_ENTER) ;
        getRobot().delay(500);
        guardarRegistro();
    }
    
    public void enfocaAleph() throws InterruptedException {
        // Obtener todas las ventanas del sistema
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        // Buscar la ventana deseada por título
        String windowTitle = "ALEPH";
        DesktopWindow targetWindow = null;
        for (DesktopWindow window : windows) {
            if (window.getTitle().contains(windowTitle)) {
                targetWindow = window;
                break;
            }
        }
        if (targetWindow != null) {
            WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
            WinDef.HWND hwnd = targetWindow.getHWND();
            try {
                if (foregroundWindow.equals(hwnd)) {                
                    //System.out.println("La ventana ya está en primer plano");
                } else {
                    // Establecer la ventana de destino como la ventana activa            
                    User32.INSTANCE.SetForegroundWindow(targetWindow.getHWND()) ;
                    Thread.sleep(500);
                }            
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            
        }
    }

    void borraItem(String barcode) {
        abreBibliografico("");
        directType(KeyEvent.VK_TAB) ;
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        type(barcode) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_ENTER) ;
        getRobot().delay(800);
        directType(KeyEvent.VK_TAB) ;
        directType(KeyEvent.VK_TAB) ;
        directType(KeyEvent.VK_TAB) ;
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_SPACE) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_LEFT) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_SPACE) ;
    }
    
    public void abreItem(String barcode) {
        abreBibliografico("");
        directType(KeyEvent.VK_TAB) ;
        directType(KeyEvent.VK_TAB) ;
        getRobot().delay(500);
        type(barcode) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_ENTER) ;        
    }

    void agregaCampo655(RegistroParaActualizar rpa) {
        boolean tieneDiapo = rpa.tieneDiapo() ;
        boolean tieneNega = rpa.tieneNega() ;
        if (tieneNega) {
            getRobot().delay(500);
            directType(KeyEvent.VK_F6) ;
            type("655");
            type(" 4");
            directType(KeyEvent.VK_RIGHT) ;            
            type("Negativos flexibles");
            getRobot().delay(500);
        }
        if (tieneDiapo) {
            getRobot().delay(500);
            directType(KeyEvent.VK_F6) ;
            type("655");
            type(" 4");
            directType(KeyEvent.VK_RIGHT) ;            
            type("Fotografía en color");
            getRobot().delay(500);
        }
    }

    void cambiaUfi(String barcode, String ufi) throws InterruptedException {
        enfocaAleph();
        // Breve espera para asegurar que el programa esté al frente
        Thread.sleep(500);
        
        //abre item
        abreItem(barcode);
        
        //pestaña 1
        getRobot().mouseMove(444, 442);
        getRobot().delay(500);
        getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().delay(500);
        
        //doble click en la ufi
        getRobot().mouseMove(507, 644);
        getRobot().delay(500);
        //dos clicks para seleccionar
        getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().mousePress(InputEvent.BUTTON1_DOWN_MASK);
        getRobot().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        //ufi nueva
        type(ufi);
        directType(VK_ENTER);
    }
    
    public void capturaPosicionMouse() throws InterruptedException {
        Stage dialog = new Stage();
        //dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setAlwaysOnTop(true);
        dialog.setTitle("Mouse");

        Label label = new Label("Mové el mouse a la posicion deseada.");
        ProgressBar progressBar = new ProgressBar(0);
        Label countdownLabel = new Label("5");

        StackPane dialogLayout = new StackPane(label, countdownLabel, progressBar);
        Scene dialogScene = new Scene(dialogLayout, 250, 150);
        dialog.setScene(dialogScene);
        dialog.show();

        new Thread(() -> {
            try {
                for (int i = 5; i >= 1; i--) {
                    final int count = i;
                    Thread.sleep(1000); // Sleep for 1 second
                    // Update the UI components on the JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        countdownLabel.setText(String.valueOf(count));
                        progressBar.setProgress((5 - count) / 5.0);
                    });
                }

                Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                double x = mouseLocation.getX();
                double y = mouseLocation.getY();

                // Simulate a left mouse click
                robot.mouseMove((int) x, (int) y); // Move the mouse to the current location
                robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);

                System.out.printf("Left Clicked at - X: %.2f, Y: %.2f%n", x, y);

                // Close the dialog on the FX Application Thread
                Platform.runLater(dialog::close);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }   

    private void borraTodo() {
        directType(KeyEvent.VK_HOME) ;
        for (int i = 0; i < 100; i++) {
            directType(KeyEvent.VK_DELETE) ;
        }
    }

    public void cierraRegistro() {
        getRobot().delay(1600);
        directType(VK_F2);
        getRobot().delay(500);
        directType(KeyEvent.VK_ALT,KeyEvent.VK_C) ;
        getRobot().delay(500);
        directType(KeyEvent.VK_UP) ;
        directType(KeyEvent.VK_ENTER) ;
    }
    
}