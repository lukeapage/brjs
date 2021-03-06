package org.bladerunnerjs.utility;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.bladerunnerjs.memoization.MemoizedFile;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BRJSNode;
import org.bladerunnerjs.model.exception.template.TemplateDirectoryAlreadyExistsException;
import org.bladerunnerjs.model.exception.template.TemplateInstallationException;
import org.bladerunnerjs.plugin.plugins.bundlers.commonjs.CommonJsSourceModule;


public class TemplateUtility
{
	public static void installTemplate(BRJSNode node, String templateName, Map<String, String> transformations) throws TemplateInstallationException {
		installTemplate(node, templateName, transformations, false);
	}
	
	public static void installTemplate(BRJSNode node, String templateName, Map<String, String> transformations, boolean allowNonEmptyDirectories) throws TemplateInstallationException {
		File tempDir = null; 
		try {
			tempDir = FileUtils.createTemporaryDirectory( TemplateUtility.class, templateName );
			
			if(node.dirExists() && !(node instanceof BRJS)) {
				List<MemoizedFile> dirContents = node.root().getMemoizedFile(node.dir()).filesAndDirs();
				
				if((dirContents.size() != 0) && !allowNonEmptyDirectories) {
					throw new TemplateDirectoryAlreadyExistsException(node);
				}
			}
			
			File templateDir = node.root().template(templateName).dir();
			
			if(templateDir.exists()) {
				IOFileFilter hiddenFilesFilter = FileFilterUtils.or( 
						FileFilterUtils.notFileFilter(new PrefixFileFilter(".")), new NameFileFilter(".gitignore") );
				IOFileFilter fileFilter = FileFilterUtils.and( new FileDoesntAlreadyExistFileFilter(templateDir, node.dir()), hiddenFilesFilter );
				FileUtils.copyDirectory(node.root(), templateDir, tempDir, fileFilter);
			}
			
			if(!transformations.isEmpty()) {
				transformDir(node.root(), tempDir, transformations);
			}
			
			FileUtils.moveDirectoryContents(node.root(), tempDir, node.dir());
			
			if(!JsStyleUtility.getJsStyle(node.root(), node.dir()).equals(CommonJsSourceModule.JS_STYLE)) {
				JsStyleUtility.setJsStyle(node.root(), node.dir(), CommonJsSourceModule.JS_STYLE);
			}
		}
		catch(IOException e) {
			throw new TemplateInstallationException(e);
		}
		finally {
			if (tempDir != null) {
				FileUtils.deleteQuietly(node.root(), tempDir);
			}
		}
	}
	
	private static void transformDir(BRJS brjs, File dir, Map<String, String> transformations) throws TemplateInstallationException
	{
		for(String transformKey : transformations.keySet())
		{
			String findText = "@" + transformKey;
			String replaceText = transformations.get(transformKey);
			
			dir = transformFileName(dir, findText, replaceText);
		}
		
		for(File file : dir.listFiles())
		{
			if(!file.isDirectory())
			{
				if(file.getName().matches("^.*\\.(txt|js|xml|properties|bundle|conf|css|htm|html|jsp|java)$"))
				{
					transformFile(brjs, file, transformations);
				}
			}
			else
			{
				transformDir(brjs, file, transformations);
			}
		}
	}
	
	private static void transformFile(BRJS brjs, File file, Map<String, String> transformations) throws TemplateInstallationException
	{
		EncodedFileUtil fileUtil = new EncodedFileUtil(brjs, "UTF-8");
		
		try {
			String fileContents = fileUtil.readFileToString(file);
			
			for(String transformationKey : transformations.keySet()) {
				String findText = "@" + transformationKey;
				String replaceText = transformations.get(transformationKey);
				
				file = transformFileName(file, findText, replaceText);
				fileContents = fileContents.replaceAll(findText, replaceText);
			}
			
			fileUtil.writeStringToFile(file, fileContents);
		}
		catch(IOException e) {
			throw new TemplateInstallationException(e);
		}
	}
	
	private static File transformFileName(File file, String findText, String replaceText) throws TemplateInstallationException
	{
		if(file.getName().contains(findText))
		{
			File renamedFile = new File(file.getParent(), file.getName().replaceAll(findText, replaceText));
			
			if(file.renameTo(renamedFile))
			{
				file = renamedFile;
			}
			else
			{
				throw new TemplateInstallationException("unable to rename '" + file.getPath() + "' directory to '" + renamedFile.getPath() + "'.");
			}
		}
		
		return file;
	}
	
	static class FileDoesntAlreadyExistFileFilter implements IOFileFilter 
	{
		File destDir;
		File srcDir;
		FileDoesntAlreadyExistFileFilter(File srcDir, MemoizedFile destDir)
		{
			this.destDir = destDir;
			this.srcDir = srcDir;
		}
		@Override
		public boolean accept(File pathname)
		{
			String relativePath = StringUtils.substringAfter(pathname.getAbsolutePath(), srcDir.getAbsolutePath());
			return accept(destDir, relativePath);
		}
		@Override
		public boolean accept(File dir, String name)
		{
			File destFile = new File(destDir, name);
			if (destFile.isDirectory()) {
				return true;
			}
			return !(destFile.exists());
		}
	}
}
