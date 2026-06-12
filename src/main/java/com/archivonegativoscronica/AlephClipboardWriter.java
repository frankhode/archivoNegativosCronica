package com.archivonegativoscronica;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class AlephClipboardWriter {

    private static final int CF_TEXT = 1;
    private static final int CF_UNICODETEXT = 13;
    private static final int GMEM_MOVEABLE = 0x0002;

    private static final String FORMATO_ALEPH_TAG = "ALEPH_TAG";

    private interface User32Clipboard extends StdCallLibrary {

        User32Clipboard INSTANCE = Native.load(
                "user32",
                User32Clipboard.class,
                W32APIOptions.DEFAULT_OPTIONS
        );

        boolean OpenClipboard(HWND hWndNewOwner);

        boolean EmptyClipboard();

        Pointer SetClipboardData(int uFormat, Pointer hMem);

        boolean CloseClipboard();

        int RegisterClipboardFormat(String lpszFormat);
    }

    private interface Kernel32Clipboard extends StdCallLibrary {

        Kernel32Clipboard INSTANCE = Native.load(
                "kernel32",
                Kernel32Clipboard.class,
                W32APIOptions.DEFAULT_OPTIONS
        );

        Pointer GlobalAlloc(int uFlags, int dwBytes);

        Pointer GlobalLock(Pointer hMem);

        boolean GlobalUnlock(Pointer hMem);

        Pointer GlobalFree(Pointer hMem);
    }

    public void copiarTextoPlano(String texto) {
        Map<Integer, byte[]> formatos = new LinkedHashMap<>();
        formatos.put(CF_UNICODETEXT, toUnicodeText(texto));
        formatos.put(CF_TEXT, toAnsiText(texto));

        escribirFormatos(formatos);
    }

    public void copiarAlephTag(byte[] alephTagBytes, String textoFallback) {
        int formatoAlephTag = User32Clipboard.INSTANCE.RegisterClipboardFormat(FORMATO_ALEPH_TAG);

        if (formatoAlephTag == 0) {
            throw new IllegalStateException("No se pudo registrar el formato " + FORMATO_ALEPH_TAG);
        }

        Map<Integer, byte[]> formatos = new LinkedHashMap<>();

        formatos.put(formatoAlephTag, asegurarNullFinal(alephTagBytes));
        formatos.put(CF_UNICODETEXT, toUnicodeText(textoFallback));
        formatos.put(CF_TEXT, toAnsiText(textoFallback));

        escribirFormatos(formatos);
    }

    public void copiarAlephTagExperimentalDesdeTexto(String texto) {
        copiarAlephTagExperimentalDesdeTexto(texto, "UTF-8");
    }

    public void copiarAlephTagExperimentalDesdeTexto(String texto, String charsetName) {
        String seguro = texto == null ? "" : texto;

        byte[] alephTagBytes = seguro.getBytes(Charset.forName(charsetName));

        copiarAlephTag(alephTagBytes, seguro);
    }

    private void escribirFormatos(Map<Integer, byte[]> formatos) {
        HWND hwnd = null;

        if (!User32Clipboard.INSTANCE.OpenClipboard(hwnd)) {
            throw new IllegalStateException("No se pudo abrir el portapapeles de Windows.");
        }

        try {
            if (!User32Clipboard.INSTANCE.EmptyClipboard()) {
                throw new IllegalStateException("No se pudo vaciar el portapapeles de Windows.");
            }

            for (Map.Entry<Integer, byte[]> entry : formatos.entrySet()) {
                int formato = entry.getKey();
                byte[] data = entry.getValue();

                Pointer hMem = copiarBytesAGlobalMemory(data);
                Pointer result = User32Clipboard.INSTANCE.SetClipboardData(formato, hMem);

                if (result == null) {
                    Kernel32Clipboard.INSTANCE.GlobalFree(hMem);
                    throw new IllegalStateException("No se pudo escribir el formato " + formato + " en el portapapeles.");
                }
            }
        } finally {
            User32Clipboard.INSTANCE.CloseClipboard();
        }
    }

    private Pointer copiarBytesAGlobalMemory(byte[] data) {
        Pointer hMem = Kernel32Clipboard.INSTANCE.GlobalAlloc(GMEM_MOVEABLE, data.length);

        if (hMem == null) {
            throw new IllegalStateException("GlobalAlloc falló.");
        }

        Pointer pointer = Kernel32Clipboard.INSTANCE.GlobalLock(hMem);

        if (pointer == null) {
            Kernel32Clipboard.INSTANCE.GlobalFree(hMem);
            throw new IllegalStateException("GlobalLock falló.");
        }

        try {
            pointer.write(0, data, 0, data.length);
        } finally {
            Kernel32Clipboard.INSTANCE.GlobalUnlock(hMem);
        }

        return hMem;
    }

    private byte[] toUnicodeText(String texto) {
        String seguro = texto == null ? "" : texto;
        byte[] contenido = seguro.getBytes(StandardCharsets.UTF_16LE);

        byte[] conNull = new byte[contenido.length + 2];
        System.arraycopy(contenido, 0, conNull, 0, contenido.length);

        return conNull;
    }

    private byte[] toAnsiText(String texto) {
        String seguro = texto == null ? "" : texto;
        byte[] contenido = seguro.getBytes(Charset.forName("windows-1252"));

        byte[] conNull = new byte[contenido.length + 1];
        System.arraycopy(contenido, 0, conNull, 0, contenido.length);

        return conNull;
    }

    private byte[] asegurarNullFinal(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[]{0};
        }

        if (data[data.length - 1] == 0) {
            return data;
        }

        byte[] conNull = new byte[data.length + 1];
        System.arraycopy(data, 0, conNull, 0, data.length);

        return conNull;
    }
}