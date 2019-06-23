package jpcasillas.gdl.jal.mx.strategosmx.utilities;

import java.io.File;
import java.io.FileFilter;

public class ImageFilter implements FileFilter {
    // only want to see the following audio file types
    private String[] extension = {"jpg", "JPG"};
    @Override
    public boolean accept(File pathname) {
        // if we are looking at a directory/file that's not hidden we want to see it so return TRUE
        if (pathname.isDirectory() && !pathname.isHidden()) {
            return true;
        }
        // loops through and determines the extension of all files in the directory
        // returns TRUE to only show the audio files defined in the String[] extension array
        for (String ext : extension) {
            if (pathname.getName().toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}