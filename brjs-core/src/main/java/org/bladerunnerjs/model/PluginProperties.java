package org.bladerunnerjs.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;
import java.util.Map;
import java.util.Properties;

import org.bladerunnerjs.memoization.MemoizedFile;
import org.bladerunnerjs.model.engine.Node;
import org.bladerunnerjs.model.engine.NodeProperties;
import org.bladerunnerjs.model.exception.PropertiesException;



public class PluginProperties implements NodeProperties
{
	private static final String PROPERTIES_FILE_NAME = "plugin.properties";
	
	private final Node node;
	private final String pluginName;

	private Map<String, Object> transientProperties = new TreeMap<String, Object>();
	
	public PluginProperties(Node node, String pluginName)
	{
		this.node = node;
		this.pluginName = pluginName;
	}

	@Override
	public void setPersisentProperty(String name, String value) throws PropertiesException
	{
		try {
			Properties properties = getProperties();
			properties.setProperty(name, value);
			saveProperties(properties);
		} catch (Exception ex)
		{
			throw new PropertiesException(PropertiesException.ERROR_SETTING_PROPERTIES_EXCEPTION, ex);
		}
	}

	@Override
	public String getPersisentProperty(String name) throws PropertiesException
	{
		try {
			Properties properties = getProperties();
			return properties.getProperty(name);
		} catch (Exception ex)
		{
			throw new PropertiesException(PropertiesException.ERROR_GETTING_PROPERTIES_EXCEPTION, ex);
		}
	}
	
	@Override
	public void setTransientProperty(String propertyName, Object propertyValue)
	{
		transientProperties.put(propertyName, propertyValue);
	}

	@Override
	public Object getTransientProperty(String propertyName)
	{
		return transientProperties.get(propertyName);
	}
	
	

	private MemoizedFile getPropertiesFile() throws IOException
	{
		MemoizedFile storageDir = node.storageDir(pluginName);
		MemoizedFile propertiesFile = storageDir.file(PROPERTIES_FILE_NAME);
		if (!propertiesFile.exists())
		{
			 storageDir.mkdirs();
			propertiesFile.createNewFile();
		}
		return propertiesFile;
	}
	
	private Properties getProperties() throws PropertiesException, FileNotFoundException, IOException
	{
		File propertiesFile = getPropertiesFile();
		Properties properties = new Properties();
		if (propertiesFile.exists())
		{
			try(InputStream propertiesInputStream = new FileInputStream(propertiesFile)) {
				properties.load(propertiesInputStream);
			}
		}
		return properties;
	}
	
	private void saveProperties(Properties properties) throws FileNotFoundException, IOException
	{
		MemoizedFile propertiesFile = getPropertiesFile();
		try(OutputStream propertiesOutputStream = new FileOutputStream(propertiesFile)) {
			properties.store(propertiesOutputStream, null);
		}
		propertiesFile.incrementFileVersion();
	}
	
}
