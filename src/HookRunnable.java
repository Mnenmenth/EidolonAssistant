/*
 * Made by Earl Kennedy
 * https://github.com/Mnenmenth
 */

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;

import javax.swing.*;

public class HookRunnable implements Runnable {
    // point to hook
    private static WinUser.HHOOK hhk;
    // hook for keyboard capture
    private static WinUser.LowLevelKeyboardProc keyboardHook;
    // pressed key
    private static int key = -1;

    @Override
    public void run() {
        // User32 windows library
        final User32 lib = User32.INSTANCE;
        // Current kernel instance or something
        HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        // hook for keyboard capture
        keyboardHook = new LowLevelKeyboardProc() {
            @Override
            public LRESULT callback(int nCode, WPARAM wParam, WinUser.KBDLLHOOKSTRUCT info) {
                // process this callback
                if(nCode >= 0) {
                    // if keyboard action is a keypress
                    if (wParam.intValue() == WinUser.WM_SYSKEYUP || wParam.intValue() == WinUser.WM_KEYUP) {
                        // set key variable to current pressed key
                        key = info.vkCode;
                    }
                }
                // move on to next callback
                Pointer ptr = info.getPointer();
                long peer = Pointer.nativeValue(ptr);
                return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(peer));
            }
        };
        // pointer for hook
        hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, 0);

        // new thread to process key strokes
        new Thread(() -> {
                while(!EidolonAssistant.quit) {
                    // start ability countdown if right key is pressed
                    if (key == DisplayPanel.keyCode) {
                        // start countdown in ui in a way that makes swing happy
                        SwingUtilities.invokeLater(DisplayPanel::startCountdown);
                    }
                    // reset key variable
                    if(key != -1) {
                        key = -1;
                    }
                    // only process every 10 miliseconds so it doesn't hog cpu
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                    }
                }
                lib.UnhookWindowsHookEx(hhk);
        }).start();
        // process system messages about keystrokes (thread never actually executes past GetMessage)
        int result;
        WinUser.MSG msg = new WinUser.MSG();
        while((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
            if(result == -1) {
                System.err.println("Error in get message");
                break;
            } else {
                lib.TranslateMessage(msg);
                lib.DispatchMessage(msg);
            }
        }
        // detach hook
        lib.UnhookWindowsHookEx(hhk);
    }
}
