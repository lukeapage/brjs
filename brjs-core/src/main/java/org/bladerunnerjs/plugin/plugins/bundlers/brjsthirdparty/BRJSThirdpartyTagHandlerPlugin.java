package org.bladerunnerjs.plugin.plugins.bundlers.brjsthirdparty;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.exception.request.BundlerProcessingException;
import org.bladerunnerjs.plugin.ContentPlugin;
import org.bladerunnerjs.plugin.base.AbstractTagHandlerPlugin;

public class BRJSThirdpartyTagHandlerPlugin extends AbstractTagHandlerPlugin {
	private ContentPlugin thirdpartyTagHandlerPlugin;
	
	@Override
	public void setBRJS(BRJS brjs) {
		thirdpartyTagHandlerPlugin = brjs.plugins().contentProvider("thirdparty");
	}

	@Override
	public String getGroupName() {
		return "text/javascript";
	}
	
	@Override
	public String getTagName()
	{
		return "thirdparty.bundle";
	}
	
	@Override
	public void writeDevTagContent(Map<String, String> tagAttributes, BundleSet bundleSet, String locale, Writer writer) throws IOException
	{
		try {
			for (String requestPath : thirdpartyTagHandlerPlugin.getValidDevContentPaths(bundleSet, locale))
			{
				writer.write("<script type='text/javascript' src='" + requestPath + "'></script>\n");
			}
		}
		catch(BundlerProcessingException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writeProdTagContent(Map<String, String> tagAttributes, BundleSet bundleSet, String locale, Writer writer) throws IOException
	{
		try {
			for (String requestPath : thirdpartyTagHandlerPlugin.getValidProdContentPaths(bundleSet, locale))
			{
				writer.write("<script type='text/javascript' src='" + requestPath + "'></script>\n");
			}
		}
		catch(BundlerProcessingException e) {
			throw new IOException(e);
		}
	}
}