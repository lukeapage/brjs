package org.bladerunnerjs.plugin.plugins.bundlers.namespacedjs;

import org.bladerunnerjs.memoization.MemoizedFile;
import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.AssetFileInstantationException;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.plugin.base.AbstractAssetPlugin;

public class NamespacedJsAssetPlugin extends AbstractAssetPlugin {
	
	@Override
	public void setBRJS(BRJS brjs) {
	}
	
	@Override
	public boolean canHandleAsset(MemoizedFile assetFile, AssetLocation assetLocation) {
		return (assetLocation.jsStyle().equals(NamespacedJsSourceModule.JS_STYLE) && assetFile.getName().endsWith(".js"));
	}
	
	@Override
	public Asset createAsset(MemoizedFile assetFile, AssetLocation assetLocation) throws AssetFileInstantationException {
		return new NamespacedJsSourceModule(assetFile, assetLocation);
	}
}
