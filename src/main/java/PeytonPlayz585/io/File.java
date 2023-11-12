package PeytonPlayz585.io;

import java.util.*;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.indexeddb.EventHandler;
import org.teavm.jso.indexeddb.IDBCountRequest;
import org.teavm.jso.indexeddb.IDBCursor;
import org.teavm.jso.indexeddb.IDBCursorRequest;
import org.teavm.jso.indexeddb.IDBDatabase;
import org.teavm.jso.indexeddb.IDBFactory;
import org.teavm.jso.indexeddb.IDBGetRequest;
import org.teavm.jso.indexeddb.IDBObjectStoreParameters;
import org.teavm.jso.indexeddb.IDBOpenDBRequest;
import org.teavm.jso.indexeddb.IDBRequest;
import org.teavm.jso.indexeddb.IDBTransaction;
import org.teavm.jso.indexeddb.IDBVersionChangeEvent;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Uint8Array;

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

        if(!init) {
            initFileSystem();
        }
    }

    public File(String parent, String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if(parent != null) {
            this.path = fs.resolve(fs.normalize(parent), fs.normalize(child));
        }
        this.prefixLength = fs.prefixLength(this.path);

        if(!init) {
            initFileSystem();
        }
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

        if(!init) {
            initFileSystem();
        }
    }

    static boolean init = false;
    private static String err = "";
    private static IDBDatabase db = null;

    public static void initFileSystem() {
        DatabaseOpen dbo = AsyncHandlers.openDB("_PeytonPlayz585_io_VirtualFileSystem");
		if(dbo == null) {
			err = "Unknown Error";
			return;
		}
		if(dbo.failedLocked) {
			return;
		}
		if(dbo.failedInit || dbo.database == null) {
			err = dbo.failedError == null ? "Initialization Failed" : dbo.failedError;
			return;
		}
		db = dbo.database;
		init = true;
    }

    public boolean exists() {
        return AsyncHandlers.fileExists(db, this.path).bool;
    }

    public boolean isFile() {
        return AsyncHandlers.fileGetType(db, this.path) == FileExists.FILE;
    }

    public boolean isDirectory() {
        return AsyncHandlers.fileGetType(db, this.path) == FileExists.DIRECTORY;
    }

    public void mkdir() {
        if(isDirectory()) {
			return;
		}
		int i = this.path.lastIndexOf('/');
		if(i > 0) {
            File file = new File(this.path.substring(0, i));
			file.mkdir();
		}
		AsyncHandlers.writeWholeFile(db, this.path, true, ArrayBuffer.create(0));
    }

    public void writeBytes(byte[] data) {
        int i = this.path.lastIndexOf('/');
		if(i > 0) {
            File file = new File(this.path.substring(0, i));
			file.mkdir();
		}
		Uint8Array arr = Uint8Array.create(data.length);
		arr.set(data);
		AsyncHandlers.writeWholeFile(db, this.path, false, arr.getBuffer());
    }

    public byte[] getBytes() {
        ArrayBuffer arr = AsyncHandlers.readWholeFile(db, this.path);
		if(arr == null) {
			return null;
		}
		byte[] data = new byte[arr.getByteLength()];
		Uint8Array arrr = Uint8Array.create(arr);
		for(int i = 0; i < data.length; ++i) {
			data[i] = (byte) arrr.get(i);
		}
		return data;
    }

    public void renameTo(File dest) {
        this.copyTo(dest.getPath());
        AsyncHandlers.deleteFile(db, this.path);
        this.path = dest.getPath();
    }

    public void delete() {
        AsyncHandlers.deleteFile(db, this.path);
    }

    public String[] list() {
        Collection<FileSystem.FileEntry> files = listFileEntries();
        LinkedList<String> fileList = new LinkedList<>();
        for(FileSystem.FileEntry f : files) {
            fileList.add(f.path);
        }
        return fileList.toArray(new String[0]);
    }

    public Collection<FileSystem.FileEntry> listFileEntries() {
        LinkedList<FileSystem.FileEntry> lst = new LinkedList<>();
		AsyncHandlers.iterateFiles(db, this.path, true, false, lst);
		return lst;
    }

    private void copyTo(String newPath) {
        ArrayBuffer arr = AsyncHandlers.readWholeFile(db, this.path);
		int i = newPath.lastIndexOf('/');
		if(i > 0) {
            File file = new File(newPath.substring(0, i));
			file.mkdir();
		}
		AsyncHandlers.writeWholeFile(db, newPath, false, arr);
    }

    public long getLastModified() {
		int lm = AsyncHandlers.fileGetLastModified(db, this.path);
		return lm == -1 ? -1l : AsyncHandlers.epoch + lm;
	}
	
	public int getFileSize() {
		ArrayBuffer arr = AsyncHandlers.readWholeFile(db, this.path);
		if(arr == null) {
			return -1;
		}else {
			return arr.getByteLength();
		}
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

    protected static class BooleanResult {
		
		protected static final BooleanResult TRUE = new BooleanResult(true);
		protected static final BooleanResult FALSE = new BooleanResult(false);
		
		protected final boolean bool;
		
		private BooleanResult(boolean b) {
			bool = b;
		}
		
		protected static BooleanResult _new(boolean b) {
			return b ? TRUE : FALSE;
		}
		
	}
	
	protected static class DatabaseOpen {
		
		protected final boolean failedInit;
		protected final boolean failedLocked;
		protected final String failedError;
		
		protected final IDBDatabase database;
		
		protected DatabaseOpen(boolean init, boolean locked, String error, IDBDatabase db) {
			failedInit = init;
			failedLocked = locked;
			failedError = error;
			database = db;
		}
		
	}
	
	protected static enum FileExists {
		FILE, DIRECTORY, FALSE
	}
	
	@JSBody(script = "return ((typeof indexedDB) !== 'undefined') ? indexedDB : null;")
	protected static native IDBFactory createIDBFactory();
	
	protected static class AsyncHandlers {
		
		protected static final long epoch = 1645568542000l;
		
		@Async
		protected static native DatabaseOpen openDB(String name);
		
		private static void openDB(String name, final AsyncCallback<DatabaseOpen> cb) {
			IDBFactory i = createIDBFactory();
			if(i == null) {
				cb.complete(new DatabaseOpen(false, false, "window.indexedDB was null or undefined", null));
				return;
			}
			final IDBOpenDBRequest f = i.open(name, 1);
			f.setOnBlocked(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(new DatabaseOpen(false, true, null, null));
				}
			});
			f.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(new DatabaseOpen(false, false, null, f.getResult()));
				}
			});
			f.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(new DatabaseOpen(true, false, "open error", null));
				}
			});
			f.setOnUpgradeNeeded(new EventListener<IDBVersionChangeEvent>() {
				@Override
				public void handleEvent(IDBVersionChangeEvent evt) {
					IDBObjectStorePatched.createObjectStorePatch(f.getResult(), "filesystem", IDBObjectStoreParameters.create().keyPath("path"));
				}
			});
		}
		
		@Async
		protected static native BooleanResult deleteFile(IDBDatabase db, String name);
		
		private static void deleteFile(IDBDatabase db, String name, final AsyncCallback<BooleanResult> cb) {
			IDBTransaction tx = db.transaction("filesystem", "readwrite");
			final IDBRequest r = IDBObjectStorePatched.objectStorePatch(tx, "filesystem").delete(makeTheFuckingKeyWork(name));
			
			r.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(BooleanResult._new(true));
				}
			});
			r.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(BooleanResult._new(false));
				}
			});
		}
		
		@JSBody(params = { "obj" }, script = "return (typeof obj === 'undefined') ? null : ((typeof obj.data === 'undefined') ? null : obj.data);")
		protected static native ArrayBuffer readRow(JSObject obj);
		
		@JSBody(params = { "obj" }, script = "return (typeof obj === 'undefined') ? false : ((typeof obj.directory === 'undefined') ? false : obj.directory);")
		protected static native boolean isRowDirectory(JSObject obj);
		
		@JSBody(params = { "obj" }, script = "return (typeof obj === 'undefined') ? -1 : ((typeof obj.lastModified === 'undefined') ? -1 : obj.lastModified);")
		protected static native int readLastModified(JSObject obj);
		
		@JSBody(params = { "obj" }, script = "return [obj];")
		private static native JSObject makeTheFuckingKeyWork(String k);
		
		@Async
		protected static native ArrayBuffer readWholeFile(IDBDatabase db, String name);
		
		private static void readWholeFile(IDBDatabase db, String name, final AsyncCallback<ArrayBuffer> cb) {
			IDBTransaction tx = db.transaction("filesystem", "readonly");
			final IDBGetRequest r = IDBObjectStorePatched.objectStorePatch(tx, "filesystem").get(makeTheFuckingKeyWork(name));
			r.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(isRowDirectory(r.getResult()) ? null : readRow(r.getResult()));
				}
			});
			r.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(null);
				}
			});
			
		}
		
		@Async
		protected static native Integer readLastModified(IDBDatabase db, String name);
		
		private static void readLastModified(IDBDatabase db, String name, final AsyncCallback<Integer> cb) {
			IDBTransaction tx = db.transaction("filesystem", "readonly");
			final IDBGetRequest r = IDBObjectStorePatched.objectStorePatch(tx, "filesystem").get(makeTheFuckingKeyWork(name));
			r.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(readLastModified(r.getResult()));
				}
			});
			r.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(-1);
				}
			});
			
		}
		
		@JSBody(params = { "k" }, script = "return ((typeof k) === \"string\") ? k : (((typeof k) === \"undefined\") ? null : (((typeof k[0]) === \"string\") ? k[0] : null));")
		private static native String readKey(JSObject k);
		
		@Async
		protected static native Integer iterateFiles(IDBDatabase db, final String prefix, final boolean listDirs, final boolean recursiveDirs, final Collection<FileSystem.FileEntry> lst);
		
		private static void iterateFiles(IDBDatabase db, final String prefix, final boolean listDirs, final boolean recursiveDirs, final Collection<FileSystem.FileEntry> lst, final AsyncCallback<Integer> cb) {
			IDBTransaction tx = db.transaction("filesystem", "readonly");
			final IDBCursorRequest r = IDBObjectStorePatched.objectStorePatch(tx, "filesystem").openCursor();
			final int[] res = new int[1];
			r.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					IDBCursor c = r.getResult();
					if(c == null || c.getKey() == null || c.getValue() == null) {
						cb.complete(res[0]);
						return;
					}
					String k = readKey(c.getKey());
					if(k != null) {
						if(k.startsWith(prefix)) {
							if(recursiveDirs || k.indexOf('/', prefix.length() + 1) == -1) {
								boolean dir = isRowDirectory(c.getValue());
								if(dir) {
									if(listDirs) {
										lst.add(new FileSystem.FileEntry(k, true, -1));
									}
								}else {
									lst.add(new FileSystem.FileEntry(k, false, epoch + readLastModified(c.getValue())));
								}
							}
						}
					}
					c.doContinue();
				}
			});
			r.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(res[0] > 0 ? res[0] : -1);
				}
			});
		}
		
		@Async
		protected static native BooleanResult fileExists(IDBDatabase db, String name);
		
		private static void fileExists(IDBDatabase db, String name, final AsyncCallback<BooleanResult> cb) {
			IDBTransaction tx = db.transaction("filesystem", "readonly");
			final IDBCountRequest r = IDBObjectStorePatched.objectStorePatch(tx, "filesystem").count(makeTheFuckingKeyWork(name));
			r.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(BooleanResult._new(r.getResult() > 0));
				}
			});
			r.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(BooleanResult._new(false));
				}
			});
		}
		
		@Async
		protected static native Integer fileGetLastModified(IDBDatabase db, String name);
		
		private static void fileGetLastModified(IDBDatabase db, String name, final AsyncCallback<Integer> cb) {
			IDBTransaction tx = db.transaction("filesystem", "readonly");
			final IDBGetRequest r = IDBObjectStorePatched.objectStorePatch(tx, "filesystem").get(makeTheFuckingKeyWork(name));
			r.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(readLastModified(r.getResult()));
				}
			});
			r.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(-1);
				}
			});
		}
		
		@Async
		protected static native FileExists fileGetType(IDBDatabase db, String name);
		
		private static void fileGetType(IDBDatabase db, String name, final AsyncCallback<FileExists> cb) {
			IDBTransaction tx = db.transaction("filesystem", "readonly");
			final IDBGetRequest r = IDBObjectStorePatched.objectStorePatch(tx, "filesystem").get(makeTheFuckingKeyWork(name));
			r.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(r.getResult() == null ? FileExists.FALSE : (isRowDirectory(r.getResult()) ? FileExists.DIRECTORY : FileExists.FILE));
				}
			});
			r.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(FileExists.FALSE);
				}
			});
		}
		
		@JSBody(params = { "pat", "dir", "lm", "dat" }, script = "return { path: pat, directory: dir, lastModified: lm, data: dat };")
		protected static native JSObject writeRow(String name, boolean directory, int lm, ArrayBuffer data);
		
		@Async
		protected static native BooleanResult writeWholeFile(IDBDatabase db, String name, boolean directory, ArrayBuffer data);
		
		private static void writeWholeFile(IDBDatabase db, String name, boolean directory, ArrayBuffer data, final AsyncCallback<BooleanResult> cb) {
			IDBTransaction tx = db.transaction("filesystem", "readwrite");
			final IDBRequest r = IDBObjectStorePatched.objectStorePatch(tx, "filesystem").put(writeRow(name, directory, (int)(System.currentTimeMillis() - epoch), data));
			r.setOnSuccess(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(BooleanResult._new(true));
				}
			});
			r.setOnError(new EventHandler() {
				@Override
				public void handleEvent() {
					cb.complete(BooleanResult._new(false));
				}
			});
		}
    }
}