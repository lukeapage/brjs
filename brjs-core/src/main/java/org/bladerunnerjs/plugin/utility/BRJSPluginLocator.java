package org.bladerunnerjs.plugin.utility;

import java.util.List;

import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.plugin.AssetLocationPlugin;
import org.bladerunnerjs.plugin.AssetPlugin;
import org.bladerunnerjs.plugin.BundlesetObserverPlugin;
import org.bladerunnerjs.plugin.CommandPlugin;
import org.bladerunnerjs.plugin.ContentPlugin;
import org.bladerunnerjs.plugin.MinifierPlugin;
import org.bladerunnerjs.plugin.ModelObserverPlugin;
import org.bladerunnerjs.plugin.PluginLocator;
import org.bladerunnerjs.plugin.TagHandlerPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyAssetLocationPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyAssetPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyBundlesetObserverPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyCommandPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyContentPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyMinifierPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyModelObserverPlugin;
import org.bladerunnerjs.plugin.proxy.VirtualProxyTagHandlerPlugin;


public class BRJSPluginLocator implements PluginLocator
{
	private List<ModelObserverPlugin> modelObserverPlugins;
	private List<CommandPlugin> commandPlugins;
	private List<MinifierPlugin> minifierPlugins;
	private List<ContentPlugin> contentPlugins;
	private List<TagHandlerPlugin> tagHandlerPlugins;
	private List<AssetPlugin> assetPlugins;
	private List<AssetLocationPlugin> assetLocationPlugins;
	private List<BundlesetObserverPlugin> bundlesetObserverPlugins;
	
	@Override
	public void createPlugins(BRJS brjs) {
		modelObserverPlugins = PluginLoader.createPluginsOfType(brjs, ModelObserverPlugin.class, VirtualProxyModelObserverPlugin.class);
		commandPlugins = PluginLoader.createPluginsOfType(brjs, CommandPlugin.class, VirtualProxyCommandPlugin.class);
		minifierPlugins = PluginLoader.createPluginsOfType(brjs, MinifierPlugin.class, VirtualProxyMinifierPlugin.class);
		contentPlugins = PluginLoader.createPluginsOfType(brjs, ContentPlugin.class, VirtualProxyContentPlugin.class);
		tagHandlerPlugins = PluginLoader.createPluginsOfType(brjs, TagHandlerPlugin.class, VirtualProxyTagHandlerPlugin.class);
		assetPlugins = PluginLoader.createPluginsOfType(brjs, AssetPlugin.class, VirtualProxyAssetPlugin.class);
		assetLocationPlugins = PluginLoader.createPluginsOfType(brjs, AssetLocationPlugin.class, VirtualProxyAssetLocationPlugin.class);
		bundlesetObserverPlugins = PluginLoader.createPluginsOfType(brjs, BundlesetObserverPlugin.class, VirtualProxyBundlesetObserverPlugin.class);
	}

	@Override
	public List<CommandPlugin> getCommandPlugins()
	{
		return commandPlugins;
	}
	
	@Override
	public List<ModelObserverPlugin> getModelObserverPlugins()
	{
		return modelObserverPlugins;
	}
	
	@Override
	public List<MinifierPlugin> getMinifierPlugins() {
		return minifierPlugins;
	}
	
	@Override
	public List<ContentPlugin> getContentPlugins() {
		return contentPlugins;
	}
	
	@Override
	public List<TagHandlerPlugin> getTagHandlerPlugins() {
		return tagHandlerPlugins;
	}
	
	@Override
	public List<AssetPlugin> getAssetPlugins() {
		return assetPlugins;
	}
	
	@Override
	public List<AssetLocationPlugin> getAssetLocationPlugins() {
		return assetLocationPlugins;
	}

	@Override
	public List<BundlesetObserverPlugin> getBundlesetObserverPlugins()
	{
		return bundlesetObserverPlugins;
	}
}
