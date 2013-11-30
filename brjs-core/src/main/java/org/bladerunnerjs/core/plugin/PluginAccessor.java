package org.bladerunnerjs.core.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bladerunnerjs.core.plugin.bundler.BundlerPlugin;
import org.bladerunnerjs.core.plugin.command.CommandList;
import org.bladerunnerjs.core.plugin.command.CommandPlugin;
import org.bladerunnerjs.core.plugin.content.ContentPlugin;
import org.bladerunnerjs.core.plugin.minifier.MinifierPlugin;
import org.bladerunnerjs.core.plugin.taghandler.TagHandlerPlugin;
import org.bladerunnerjs.model.BRJS;

public class PluginAccessor {
	private final PluginLocator pluginLocator;
	private final CommandList commandList;
	private final Map<String, BundlerPlugin> bundlers = new HashMap<String, BundlerPlugin>();
	
	public PluginAccessor(BRJS brjs, PluginLocator pluginLocator) {
		this.pluginLocator = pluginLocator;
		
		commandList = new CommandList(brjs, pluginLocator.getCommandPlugins());
		
		for (BundlerPlugin bundlerPlugin : pluginLocator.getBundlerPlugins()) {
			bundlers.put(bundlerPlugin.getRequestPrefix(), bundlerPlugin);
		}
	}
	
	public CommandList commandList() {
		return commandList;
	}
	
	public List<CommandPlugin> commands() {
		return commandList.getPluginCommands();
	}
	
	public List<ContentPlugin> contentProviders() {
		Set<ContentPlugin> contentPlugins = new HashSet<ContentPlugin>();
		contentPlugins.addAll(pluginLocator.getContentPlugins());
		contentPlugins.addAll(bundlers());
		return new ArrayList<ContentPlugin>(contentPlugins);
	}
	
	public BundlerPlugin bundler(String bundlerName) {
		return bundlers.get(bundlerName);
	}
	
	public List<BundlerPlugin> bundlers() {
		return new ArrayList<BundlerPlugin>(bundlers.values());
	}
	
	public List<BundlerPlugin> bundlers(String mimeType) {
		List<BundlerPlugin> bundlerPlugins = new ArrayList<>();
		
		for (BundlerPlugin bundlerPlugin : bundlers()) {
			if (bundlerPlugin.getMimeType().equals(mimeType)) {
				bundlerPlugins.add(bundlerPlugin);
			}
		}
		
		return bundlerPlugins;
	}
	
	public List<TagHandlerPlugin> tagHandlers() {
		Set<TagHandlerPlugin> tagHandlers = new HashSet<TagHandlerPlugin>();
		tagHandlers.addAll(pluginLocator.getTagHandlerPlugins());
		tagHandlers.addAll(bundlers());
		return new ArrayList<TagHandlerPlugin>(tagHandlers);
	}
	
	public List<MinifierPlugin> minifiers() {
		return pluginLocator.getMinifierPlugins();
	}
	
	public MinifierPlugin minifier(String minifierSetting) {
		List<String> validMinificationSettings = new ArrayList<String>();
		MinifierPlugin pluginForMinifierSetting = null;
		
		for (MinifierPlugin minifierPlugin : minifiers()) {
			for (String setting : minifierPlugin.getSettingNames()) {
				validMinificationSettings.add(setting);
				
				if (setting.equals(minifierSetting)) {
					pluginForMinifierSetting = (pluginForMinifierSetting == null) ? minifierPlugin : pluginForMinifierSetting;
				}
			}
		}
		
		if (pluginForMinifierSetting != null) {
			return pluginForMinifierSetting;
		}
		
		throw new RuntimeException("No minifier plugin for minifier setting '" + minifierSetting + "'. Valid settings are: "
			+ StringUtils.join(validMinificationSettings, ", "));
	}
	
	public List<ModelObserverPlugin> modelObservers() {
		return pluginLocator.getModelObserverPlugins();
	}
}
