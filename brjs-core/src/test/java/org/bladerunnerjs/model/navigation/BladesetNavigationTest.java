package org.bladerunnerjs.model.navigation;

import java.io.File;

import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.Blade;
import org.bladerunnerjs.model.Bladeset;
import org.bladerunnerjs.model.NodeTesterFactory;
import org.bladerunnerjs.model.TestModelAccessor;
import org.bladerunnerjs.model.TypedTestPack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BladesetNavigationTest extends TestModelAccessor
{
	private NodeTesterFactory<Bladeset> nodeTesterFactory;
	private BRJS brjs;
	private Bladeset bladeset;
	
	@Before
	public void setup() throws Exception
	{
		brjs = createModel(new File("src/test/resources/BRJSTest"));
		bladeset = brjs.app("a1").bladeset("bs1");
		nodeTesterFactory = new NodeTesterFactory<>(bladeset, Bladeset.class);
	}
	
	@After
	public void teardown()
	{
		brjs.close();
	}
	

	@Test
	public void blades()
	{
		nodeTesterFactory.createSetTester(Blade.class, "blades", "blade")
			.addChild("b1", "blades/b1")
			.addChild("b2", "blades/b2")
			.assertModelIsOK();
	}
	
	@Test
	public void testTypes()
	{
		nodeTesterFactory.createSetTester(TypedTestPack.class, "testTypes", "testType")
			.addChild("type1", "tests/test-type1")
			.addChild("type2", "tests/test-type2")
			.assertModelIsOK();
	}
}
