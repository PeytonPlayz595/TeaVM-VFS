package PeytonPlayz585.io;

public class File {

    static private FileSystem fs = FileSystem.getFileSystem();

    private transient int prefixLength;
    String path;

    public static void main(String[] args) {
        /*
         * You can delete this function
         * It is only here so TeaVM knows what class to compile./
         */
    }

    private File(String pathname, int prefixLength) {
        this.path = pathname;
        this.prefixLength = prefixLength;
    }

    private File(String child, File parent) {
        assert parent.path != null;
        assert (!parent.path.equals(""));
        this.path = fs.resolve(parent.path, child);
        this.prefixLength = parent.prefixLength;
    }

    public File(String pathName) {
        if (pathName == null) {
            throw new NullPointerException();
        }
        this.path = fs.normalize(pathName);
        this.prefixLength = fs.prefixLength(this.path);
    }

    public File(String parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if(parent != null) {
            this.path = fs.resolve(fs.normalize(parent), fs.normalize(child));
        }
        this.prefixLength = fs.prefixLength(this.path);
    }

    public File(File parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (parent != null) {
            this.path = fs.resolve(parent.path, fs.normalize(child));
        } else {
            this.path = fs.normalize(child);
        }
        this.prefixLength = fs.prefixLength(this.path);
    }

    public String getName() {
        int index = path.lastIndexOf("/");
        if (index < prefixLength) {
            return path.substring(prefixLength);
        }
        return path.substring(index + 1);
    }

    public String getParent() {
        int index = path.lastIndexOf("/");
        if (index < prefixLength) {
            if ((prefixLength > 0) && (path.length() > prefixLength)) {
                return path.substring(0, prefixLength);
            }
            return null;
        }
        return path.substring(0, index);
    }

    public File getParentFile() {
        String p = this.getParent();
        if (p == null) {
            return null;
        }
        return new File(p, this.prefixLength);
    }

    public String getPath() {
        return path;
    }

    int getPrefixLength() {
        return prefixLength;
    }
}