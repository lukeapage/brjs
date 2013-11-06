package org.bladerunnerjs.model.utility;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


public class TrieTest
{

	Trie<TestObject> trie;
	
	TestObject test_object_1;
	TestObject test_object_2;
	TestObject test_object_3;
	TestObject test_object_4;
	TestObject test_object_1_extraStuff;
	
	
	@Before
	public void setup()
	{
		trie = new Trie<TestObject>();
		test_object_1 = new TestObject("test_object_1");
		test_object_2 = new TestObject("test_object_2");
		test_object_3 = new TestObject("test_object_3");
		test_object_4 = new TestObject("test_object_4");
		test_object_1_extraStuff = new TestObject("test_object_1_extraStuff");
	}
	
	@Test
	public void testAddingToTrie()
	{
		trie.add("1234-abc#;;a", test_object_1);
		assertEquals(test_object_1, trie.get("1234-abc#;;a"));
	}
	
	@Test
	public void testCorrectObjectsReturnedFromUsingReader() throws IOException
	{
		trie.add("test_object_1", test_object_1);
		trie.add("test_object_2", test_object_2);
		trie.add("test_object_3", test_object_3);
		trie.add("test_object_4", test_object_4);
		
		StringReader reader = new StringReader("here is some text, test_object_1 is here too.\n"+
				"and also test_object_2\n"+
				"more stuff. 138t912109\n"+
				"\n"+
				"test_object 3 isnt here, its not spelt correctly\n"+
				"and finally test_object_4");
		
		List<TestObject> foundObjects = trie.getMatches(reader);
		assertEquals(3, foundObjects.size());
		assertEquals(test_object_1, foundObjects.get(0));
		assertEquals(test_object_2, foundObjects.get(1));
		assertEquals(test_object_4, foundObjects.get(2));
	}
	
	@Test
	public void testOccurancesWithPrefixDontMatch() throws IOException
	{
		trie.add("test_object_1", test_object_1);
		
		StringReader reader = new StringReader("abcd testtest_object_1 1234");
		
		List<TestObject> foundObjects = trie.getMatches(reader);
		assertEquals(0, foundObjects.size());
	}
	
	@Test
	public void testOccurancesWithSuffixDontMatch() throws IOException
	{
		trie.add("test_object_1", test_object_1);
		
		StringReader reader = new StringReader("abcd test_object_123 1234");
		
		List<TestObject> foundObjects = trie.getMatches(reader);
		assertEquals(0, foundObjects.size());
	}
	
	@Test
	public void testOccurancesWithSuffixAndPrefixDontMatch() throws IOException
	{
		trie.add("test_object_1", test_object_1);
		
		StringReader reader = new StringReader("abcd testtest_object_123 1234");
		
		List<TestObject> foundObjects = trie.getMatches(reader);
		assertEquals(0, foundObjects.size());
	}
	
	@Test
	public void testSubstringAtTheStartDoNotMatch() throws IOException
	{
		trie.add("test_object_1", test_object_1);
		
		StringReader reader = new StringReader("abcd test_object_123 1234");
		
		List<TestObject> foundObjects = trie.getMatches(reader);
		assertEquals(0, foundObjects.size());
	}
	
	@Test
	public void testMatcherIsGreedy() throws IOException
	{
		trie.add("test_object_1", test_object_1);
		trie.add("test_object_1_extraStuff", test_object_1_extraStuff);
		
		StringReader reader = new StringReader("abcd test_object_1_extraStuff 1234");
		
		List<TestObject> foundObjects = trie.getMatches(reader);
		assertEquals(1, foundObjects.size());
		assertEquals(test_object_1_extraStuff, foundObjects.get(0));
	}
	
	
	
	/* TestObject so the Trie is using Objects to ensure the same object instance is returned, but with a toString() that returns a name to help debugging */
	class TestObject {
		String name;
		public TestObject(String name) { this.name = name; }
		public String toString() { return name; }
	}
	
}
