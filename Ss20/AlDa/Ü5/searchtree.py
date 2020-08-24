#---------------------------------------------------------------------------------------------------------------------#
#                                                                                                                     #
# IAD Übungsblatt 5, Aufgabe 1 - Bernhard, Daas                                                                       #
#                                                                                                                     #
#---------------------------------------------------------------------------------------------------------------------#

import pytest
import random

#---------------------------------------------------------------------------------------------------------------------#

class SearchTree:
    

    class Node:
        def __init__(self, key, value):
            self._key = key
            self._value = value
            self._left = self._right = None

    
    def __init__(self):
        self._root = None
        self._size = 0
        
    
    def __len__(self):
        return self._size
        
    
    def __getitem__(self, key):          # implements 'value = tree[key]'
        if self._size == 0:
            raise KeyError("Getitem on empty tree.")
        
        node = self._tree_find(self._root, key)
        return node._value

    
    def __setitem__(self, key, value):   # implements 'tree[key] = value'
        if self._size == 0:
            self._root = SearchTree.Node(key, value)
            self._size += 1 

        elif self._tree_insert(self._root, key, value):
            self._size += 1

    
    def __delitem__(self, key):          # implements 'del tree[key] '   
        if self._size == 0:
            raise KeyError("Delete on empty tree.")
        
        if self._size == 1:
            if self._root._key != key:
                raise KeyError("Key not found.")
            
            self._root = None
        
        else:
            self._root = self._tree_remove(self._root, key)
        
        self._size -= 1


    def depth(self):
        return SearchTree._tree_depth(self._root)


    @staticmethod
    def _tree_depth(node, depth=0):
        if node == None:
            return depth

        return max([SearchTree._tree_depth(node._left, depth + 1), SearchTree._tree_depth(node._right, depth + 1)])

    
    @staticmethod
    def _tree_find(node, key):           # internal implementation
        if node == None:
            raise KeyError("Key not found.")
        
        if node._key == key:
            return node
        
        elif node._key < key:
            return SearchTree._tree_find(node._right, key)
        
        else:
            return SearchTree._tree_find(node._left, key)

    
    @staticmethod
    def _tree_insert(node, key, value):  # internal implementation
        if node._key == key:
            node._value = value
            return False

        if node._key < key:
            if node._right == None:
                node._right = SearchTree.Node(key, value)
                return True
            return SearchTree._tree_insert(node._right, key, value)
        
        else:
            if node._left == None:
                node._left = SearchTree.Node(key, value)
                return True
            return SearchTree._tree_insert(node._left, key, value)

    
    @staticmethod
    def _tree_remove(node, key):         # internal implementation
        
        def __find_leftmost_node(node):
            while node._left != None:
                node = node._left
            return node
        
        if node == None:
            raise KeyError("Key not found.")
        
        if node._key < key:
            node._right = SearchTree._tree_remove(node._right, key)
        
        elif node._key > key: 
            node._left = SearchTree._tree_remove(node._left, key)
        
        else:
            if node._left == None:
                return node._right
            
            elif node._right == None:
                return node._left
            
            else:
                node_to_copy = __find_leftmost_node(node._right)
                node._key = node_to_copy._key
                node._value = node_to_copy._value
                node._right = SearchTree._tree_remove(node._right, node_to_copy._key)
        
        return node

#---------------------------------------------------------------------------------------------------------------------#

def test_search_tree():
    t = SearchTree()
    assert len(t) == 0
    
    # Test random insert on empty tree
    shuffled_range = list(range(100))
    random.shuffle(shuffled_range)
    
    for i in shuffled_range:
        previous_length = len(t)
        
        t[i] = i
        
        assert len(t) == previous_length + 1
        assert t[i] == i
    
    # Test delete on random tree
    for i in list(range(100)):
        previous_length = len(t)

        del t[i]
        
        assert len(t) == previous_length - 1
        with pytest.raises(KeyError):
            t[i]
        with pytest.raises(KeyError):
            del t[i]

    # Test delete on leaf
    t = SearchTree()
    assert len(t) == 0
    
    t[0] = 0
    t[1] = 1
    
    assert len(t) == 2
    assert t.depth() == 2

    del t[1]

    assert len(t) == 1
    assert t.depth() == 1
    with pytest.raises(KeyError):
        t[1]
    with pytest.raises(KeyError):
        del t[1]
    
    t[0]

    # Test delete on node with one child
    t = SearchTree()
    assert len(t) == 0
    
    t[0] = 0
    t[5] = 1
    t[-1] = -1
    t[4] = 4

    assert len(t) == 4
    assert t.depth() == 3

    del t[5]
    assert len(t) == 3
    assert t.depth() == 2
    with pytest.raises(KeyError):
        t[5]
    with pytest.raises(KeyError):
        del t[5]
    
    # Check that all other keys still exist
    t[0]
    t[-1]
    t[4]

    # Test delete on node with two children
    t = SearchTree()
    assert len(t) == 0
    
    t[20] = 20
    t[13] = 13
    t[25] = 25
    t[7]= 7
    t[14] = 14
    t[13.9] = 13.9
    t[13.5] = 13.5
    t[19] = 19
    t[13.6] = 13.6
    t[13.7] = 13.7
    
    assert len(t) == 10
    assert t.depth() == 7

    del t[13]
    
    assert len(t) == 9
    assert t.depth() == 6
    with pytest.raises(KeyError):
        t[13]
    with pytest.raises(KeyError):
        del t[13]
    
    # Check that all other keys still exist
    t[20]
    t[25]
    t[7]
    t[14]
    t[13.9]
    t[13.5]
    t[19]
    t[13.6]
    t[13.7]