package PeytonPlayz585.io;

import java.lang.StringBuilder;

public class FileSystem {

    public static String resolve(String parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent != null) {
            if (parent.equals("")) {
                return normalize(child);
            } else {
                return normalize(parent + "/" + child);
            }
        } else {
            return normalize(child);
        }
    }

    public static String normalize(String path) {
        StringBuilder normalizedPath = new StringBuilder();
        String[] pathElements = path.split("/");
    
        for (String element : pathElements) {
            if (element.equals("..")) {
                if (normalizedPath.length() > 0) {
                    int lastIndex = normalizedPath.lastIndexOf("/");
                    normalizedPath.delete(lastIndex, normalizedPath.length());
                }
            } else if (!element.equals(".")) {
                normalizedPath.append("/").append(element);
            }
        }
    
        return normalizedPath.toString();
    }

    public int prefixLength(String path) {
        return path.length();
    }

    public static FileSystem getFileSystem() {
        return new FileSystem();
    }
}