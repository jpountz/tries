package net.jpountz.trie;

public interface TrieFactory {

	<V >Trie<V> newTrie();

}
