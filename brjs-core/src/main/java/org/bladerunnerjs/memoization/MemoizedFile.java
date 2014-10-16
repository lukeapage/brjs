package org.bladerunnerjs.memoization;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bladerunnerjs.model.engine.RootNode;


public class MemoizedFile extends File
{
	private static final long serialVersionUID = 7406703034536312889L;
	private MemoizedValue<Boolean> exists;
	private MemoizedValue<Boolean> isDirectory;
	private MemoizedValue<Boolean> isFile;
	private MemoizedValue<List<File>> filesAndDirs;
	private RootNode rootNode;
	
	public MemoizedFile(RootNode rootNode, String file) {
		super(file);
		this.rootNode = rootNode;
		String className = this.getClass().getSimpleName();
		exists = new MemoizedValue<>(className+".exists", rootNode, this);
		isDirectory = new MemoizedValue<>(className+".isDirectory", rootNode, this);
		isFile = new MemoizedValue<>(className+".isFile", rootNode, this);
		filesAndDirs = new MemoizedValue<>(className+".filesAndDirs", rootNode, this);
	}
	
	public MemoizedFile(RootNode rootNode, File file)
	{
		this( rootNode, file.getAbsolutePath() );		
	}
	
	public MemoizedFile(RootNode rootNode, File parent, String child)
	{
		this( rootNode, new File(parent, child).getAbsolutePath() );		
	}
	
	
	// ---- Methods Using Memoized Values ----
	
	@Override
	public boolean exists() {
		return exists.value(() -> {
			return super.exists();
		});
	}
	
	@Override
	public boolean isDirectory() {
		return isDirectory.value(() -> {
			return super.isDirectory();
		});
	}
	
	@Override
	public boolean isFile() {
		return isFile.value(() -> {
			return super.isFile();
		});
	}
	
	public List<File> filesAndDirs(IOFileFilter fileFilter) {
		List<File> returnedFilesAndDirsCopy = new ArrayList<>();
		returnedFilesAndDirsCopy.addAll( filesAndDirs.value(() -> {
			List<File> returnedFilesAndDirs = new ArrayList<>();
			for (File file : super.listFiles()) {
				if (fileFilter.accept(file)) {
					returnedFilesAndDirs.add(file);				
				}
			}
			return returnedFilesAndDirs;
		}) );
		return returnedFilesAndDirsCopy; // return a copy so multiple callers dont have the same object by reference
	}
	
	// ---- End Methods Using Memoized Values ----
	
	@Override
	public File[] listFiles(FileFilter filter) {
		List<File> listedFiles = new ArrayList<>();
		List<File> filesAndDirs = filesAndDirs();
		for (File file : filesAndDirs) {
			if (file.isDirectory() && filter.accept(file)) {
				listedFiles.add(file);
			}
		}
		return listedFiles.toArray(new File[0]);
	}
	
	@Override
	public File[] listFiles(FilenameFilter filter) {
		return listFiles( (FileFilter) FileFilterUtils.asFileFilter(filter) );
	}
	
	@Override
	public File[] listFiles() {
		return listFiles( (FileFilter) TrueFileFilter.INSTANCE);
	}
	
	
	
	
	@Override
	public String[] list(FilenameFilter filter) {
		List<String> listedNames = new ArrayList<>();
		List<File> filesAndDirs = filesAndDirs();
		for (File file : filesAndDirs) {
			if (file.isDirectory() && filter.accept(file.getParentFile(), file.getName())) {
				listedNames.add(file.getName());
			}
		}
		return listedNames.toArray(new String[0]);
	}
	
	@Override
	public String[] list() {
		return list( TrueFileFilter.INSTANCE );
	}	
	
	public List<File> filesAndDirs() {
		return filesAndDirs(TrueFileFilter.INSTANCE);
	}
	
	public List<File> files() {
		return filesAndDirs(FileFileFilter.FILE);
	}
	
	public List<File> dirs() {
		return filesAndDirs(DirectoryFileFilter.DIRECTORY);
	}
	
	public List<File> nestedFilesAndDirs() {
		List<File> nestedFilesAndDirs = new ArrayList<>();
		populateNestedFilesAndDirs(this, nestedFilesAndDirs);
		return nestedFilesAndDirs;
	}
	
	public List<File> nestedFiles() {
		List<File> nestedFiles = new ArrayList<>();
		for(File file : nestedFilesAndDirs()) {
			if(!file.isDirectory()) {
				nestedFiles.add(file);
			}
		}
		return nestedFiles;
	}
	
	public List<File> nestedDirs() {
		List<File> nestedDirs = new ArrayList<>();
		for(File file : nestedFilesAndDirs()) {
			if(file.isDirectory()) {
				nestedDirs.add(file);
			}
		}
		return nestedDirs;
	}
	
	
	
	private void populateNestedFilesAndDirs(MemoizedFile file, List<File> nestedFilesAndDirs) {
		nestedFilesAndDirs.addAll(file.filesAndDirs());
		
		for(File dir : file.dirs()) {
			MemoizedFile memoizedFile = rootNode.getMemoizedFile(dir);
			populateNestedFilesAndDirs(memoizedFile, nestedFilesAndDirs);
		}
	}
	
}
