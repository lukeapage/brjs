package org.bladerunnerjs.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;


public class ZipUtility
{

	public static void zipFolder(File srcFolder, File destZipFile, boolean zipOnlySrcFolderContentsAndNotSrcFolder) throws IOException
	{
		destZipFile.getParentFile().mkdirs();
		
		FileOutputStream fileWriter = new FileOutputStream(destZipFile);
		ZipOutputStream zip = new ZipOutputStream(fileWriter);
		
		if(zipOnlySrcFolderContentsAndNotSrcFolder)
		{
			for (File file : srcFolder.listFiles())
			{
				addFileToZip("", file, zip, false);
			}
		}
		else
		{
			addFolderToZip("", srcFolder, zip);
		}
		
		zip.flush();
		zip.close();
	}
	
	private static void addFileToZip(String path, File srcFile, ZipOutputStream zip, boolean flag) throws IOException
	{
		if (flag == true)
		{
			zip.putNextEntry(new ZipEntry(path + "/" + srcFile.getName() + "/"));
		}
		else
		{
			if (srcFile.isDirectory())
			{
				addFolderToZip(path, srcFile, zip);
			}
			else
			{
				int len;
				byte[] buf = new byte[1024];
				FileInputStream in = new FileInputStream(srcFile);
				String pathPrefix = (!path.equals("")) ? path+"/" : "";
				zip.putNextEntry(new ZipEntry(pathPrefix + srcFile.getName()));
				
				while ((len = in.read(buf)) > 0)
				{
					zip.write(buf, 0, len);
				}
				
				in.close();
			}
		}
	}

	private static void addFolderToZip(String path, File srcFolder, ZipOutputStream zip) throws IOException
	{
		if (srcFolder.list().length == 0)
		{
			addFileToZip(path, srcFolder, zip, true);
		} 
		else 
		{
			for (File file : srcFolder.listFiles())
			{
				if (path.equals(""))
				{
					addFileToZip(srcFolder.getName(), file, zip, false);
				}
				else
				{
					addFileToZip(path + "/" + srcFolder.getName(), file, zip, false);
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void unzip(ZipFile zipFile, File targetLocation) throws IOException {			 
		Enumeration files = zipFile.entries();
		File f = null;
		FileOutputStream fos = null;
		
		while (files.hasMoreElements()) {
			try {
				ZipEntry entry = (ZipEntry) files.nextElement();
				InputStream eis = zipFile.getInputStream(entry);
				
				f = new File(targetLocation, entry.getName());
				
				if (entry.isDirectory()) 
				{
					f.mkdirs();
					continue;
				} 
				else {
					f.getParentFile().mkdirs();
					f.createNewFile();
				}
			
				fos = new FileOutputStream(f);
				IOUtils.copy(eis, fos);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				continue;		  
			} 
			finally {
				if (fos != null) 
				{
					try {
						fos.close();
					}
					catch (IOException e) {
						// ignore
					}
				}
			}
		}
		zipFile.close();
	}
	
}
