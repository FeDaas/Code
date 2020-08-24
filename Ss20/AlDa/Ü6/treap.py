#---------------------------------------------------------------------------------------------------------------------#
#                                                                                                                     #
# IAD Übungsblatt 6, Aufgabe 1 - Bernhard, Daas                                                                       #
#                                                                                                                     #
#---------------------------------------------------------------------------------------------------------------------#

import pytest
import random
#---------------------------------------------------------------------------------------------------------------------#

class TreapBase:


    class Node:
        def __init__(self, key, value):
            self._key = key
            self._value = value
            self._left = self._right = None
            self._priority = 0


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
            self._root = TreapBase.Node(key, value)
            self._size += 1

        elif self._tree_insert(self._root, key, value, self._is_dynamic_treap):
            self._size += 1
        TreapBase.checkTree(self)

    def __delitem__(self, key):          # implements 'del tree[key] '
        if self._size == 0:
            raise KeyError("Delete on empty tree.")

        self._root = self._tree_remove(self._root, key)
        self._size -= 1

    def depth(self):
        return Treap._tree_depth(self._root)

    def getPrio(self,key):
        if(self._root == None):
            return none
        return self._tree_find(self._root, key)._priority

    def checkTree(self, node = None, nodeP = None, direction = None):
        if(node == None):
            node = self._root
        if(node._right != None):
            if(node._right._priority > node._priority):
                if(direction == "right"):
                    nodeP._right = self._tree_rotate_left(node)
                elif(direction == "left"):
                    nodeP._left = self._tree_rotate_left(node)
                else:
                    self._tree_rotate_left(node)
                self.checkTree()
            else:
                self.checkTree(node._right,node, "right")
        if(node._left != None):
            if(node._left._priority > node._priority):
                if(direction == "right"):
                    nodeP._right = self._tree_rotate_right(node)
                elif(direction == "left"):
                    nodeP._left = self._tree_rotate_right(node)
                else:
                    self._tree_rotate_right(node)
                self.checkTree()
            else:
                self.checkTree(node._left,node, "left")





    @staticmethod
    def _tree_depth(node, depth=0):
        if node == None:
            return depth

        return max([Treap._tree_depth(node._left, depth + 1), Treap._tree_depth(node._right, depth + 1)])

    @staticmethod
    def _tree_rotate_right(old_root):
        newRoot = old_root._left
        old_root._left = newRoot._right
        newRoot._right = old_root
        return newRoot

    @staticmethod
    def _tree_rotate_left(old_root):
        newRoot = old_root._right
        old_root._right = newRoot._left
        newRoot._left = old_root
        return newRoot


    @staticmethod
    def _tree_find(node, key, rek = 0):           # internal implementation
        if node == None:
            raise KeyError("Key not found.")

        if node._key == key:
            return node

        elif node._key < key:
            return TreapBase._tree_find(node._right, key, rek+1)

        else:
            return TreapBase._tree_find(node._left, key, rek+1)

    @staticmethod
    def _tree_insert(node, key, value, is_dynamic_treap):  # internal implementation

        if node._key == key:
            node._value = value
            if(is_dynamic_treap):
                node._priority +=1
            return False

        if node._key < key:
            if node._right == None:
                node._right = TreapBase.Node(key, value)
                return True
            return TreapBase._tree_insert(node._right, key, value, is_dynamic_treap)

        else:
            if node._left == None:
                node._left = TreapBase.Node(key, value)
                return True
            return TreapBase._tree_insert(node._left, key, value, is_dynamic_treap)

    @staticmethod
    def _tree_remove(node, key):         # internal implementation

        def __find_leftmost_node(node):
            while node._left != None:
                node = node._left
            return node

        if node == None:
            raise KeyError("Key not found.")

        if node._key < key:
            node._right = TreapBase._tree_remove(node._right, key)

        elif node._key > key:
            node._left = TreapBase._tree_remove(node._left, key)

        else:
            if node._left == None:
                return node._right

            elif node._right == None:
                return node._left

            else:
                node_to_copy = __find_leftmost_node(node._right)
                node._key = node_to_copy._key
                node._value = node_to_copy._value
                node._right = TreapBase._tree_remove(node._right, node_to_copy._key)

        return node

class DynamicTreap (TreapBase):

    def __init__(self):
        self._root = None
        self._size = 0
        self._is_dynamic_treap = True

class RandomTreap (TreapBase):

    def __init__(self):
        self._root = None
        self._size = 0
        self._is_dynamic_treap = False

def test_search_tree():
    t = DynamicTreap()
    assert len(t) == 0

    # Test random insert on empty tree
    shuffled_range = list(range(100))
    random.shuffle(shuffled_range)

    for i in shuffled_range:
        previous_length = len(t)
        t[i] = i
        assert len(t) == previous_length + 1
        assert t[i] == i

    # Test priority on random tree
    for i in range(100):
        n = random.choice(shuffled_range)
        nPrioOld = t.getPrio(n)
        t[n] = t[n]
        assert nPrioOld +1 == t.getPrio(n)

    # Test delete on random tree
    for i in shuffled_range:
        previous_length = len(t)
        del t[i]
        assert len(t) == previous_length - 1
        with pytest.raises(KeyError):
            t[i]
        with pytest.raises(KeyError):
            del t[i]

    # Test delete on leaf
    t = DynamicTreap()
    assert len(t) == 0

    t[0] = 0
    t[1] = 1

    assert len(t) == 2
    del t[1]
    assert len(t) == 1
    with pytest.raises(KeyError):
        t[1]
    with pytest.raises(KeyError):
        del t[1]

    t[0]

    # Test delete on node with one child
    t = DynamicTreap()
    assert len(t) == 0

    t[0] = 0
    t[5] = 1
    t[-1] = -1
    t[4] = 4

    assert len(t) == 4
    del t[5]
    assert len(t) == 3
    with pytest.raises(KeyError):
        t[5]
    with pytest.raises(KeyError):
        del t[5]

    t[0]
    t[-1]
    t[4]

    # Test delete on node with two children
    t = DynamicTreap()
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
    del t[13]
    with pytest.raises(KeyError):
        t[13]
    with pytest.raises(KeyError):
        del t[13]

    t[20]
    t[25]
    t[7]
    t[14]
    t[13.9]
    t[13.5]
    t[19]
    t[13.6]
    t[13.7]
