package org.bladerunnerjs.plugin.plugins.require;

import java.util.HashMap;
import java.util.Map;

import org.bladerunnerjs.aliasing.AliasDefinition;
import org.bladerunnerjs.aliasing.AliasException;
import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundlableNode;
import org.bladerunnerjs.model.exception.RequirePathException;
import org.bladerunnerjs.model.exception.UnresolvableRequirePathException;
import org.bladerunnerjs.model.exception.request.ContentFileProcessingException;
import org.bladerunnerjs.plugin.RequirePlugin;
import org.bladerunnerjs.plugin.base.AbstractRequirePlugin;

public class AliasRequirePlugin extends AbstractRequirePlugin implements RequirePlugin {
	private final Map<BundlableNode, Map<String, AliasCommonJsSourceModule>> bundlableNodeSourceModules = new HashMap<>();
	private AssetLocation assetLocation;
	
	@Override
	public void setBRJS(BRJS brjs) {
		assetLocation = new NullAssetLocation(brjs);
	}

	@Override
	public String getPluginName() {
		return "alias";
	}

	@Override
	public Asset getAsset(BundlableNode bundlableNode, String requirePathSuffix) throws RequirePathException {
		try {
			return getSourceModule(bundlableNode, requirePathSuffix);
		}
		catch (ContentFileProcessingException | AliasException e) {
			throw new UnresolvableRequirePathException("alias!" + requirePathSuffix, e);
		}
	}
	
	private AliasCommonJsSourceModule getSourceModule(BundlableNode bundlableNode, String requirePathSuffix) throws ContentFileProcessingException, AliasException {
		if(!bundlableNodeSourceModules.containsKey(bundlableNode)) {
			bundlableNodeSourceModules.put(bundlableNode, new HashMap<>());
		}
		
		Map<String, AliasCommonJsSourceModule> sourceModules = bundlableNodeSourceModules.get(bundlableNode);
		AliasDefinition aliasDefinition = bundlableNode.getAlias(requirePathSuffix);
		
		if(!sourceModules.containsKey(requirePathSuffix)) {
			sourceModules.put(requirePathSuffix, new AliasCommonJsSourceModule(assetLocation, aliasDefinition));
		}
		else {
			sourceModules.get(requirePathSuffix).setAlias(aliasDefinition);
		}
		
		return sourceModules.get(requirePathSuffix);
	}
}
